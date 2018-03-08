package com.hawker.recommend


import java.sql.DriverManager

import com.google.gson.Gson
import com.hawker.canal.KafkaMessage
import com.hawker.core.{Engine, EngineConfigure, _}
import com.hawker.recommend.YouXiangRecommendAlgorithm.startRealTimeCompute
import com.hawker.utils.ConfigUtil._
import com.hawker.utils.HBaseUtil._
import com.hawker.utils.MapUtil._
import com.hawker.utils.{DateUtil, MysqlConfig, RDBMUtil}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp
import org.apache.hadoop.hbase.filter.{FilterList, SingleColumnValueFilter}
import org.apache.hadoop.hbase.mapreduce.TableInputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.dstream.DStream

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}


/**
  * 有象视频实时推荐系统
  *
  * @author mingjiang.ji on 2017/9/25
  */
class YouXiangRecommend(conf: EngineConfigure) extends Engine[YouXiangData](conf) {

  import YouXiangRecommend._

  private val tableColumnMap = Map(
    "v_video_base" -> List("video_base_id", "name", "status", "keyword"),
    "v_video_label" -> List("video_base_id", "label_id"),
    "v_video_append" -> List("video_base_id", "actor", "director"),
    "v_video_ext" -> List("video_base_id", "property", "value"),
    "v_video_source" -> List("video_base_id", "video_source")
  )

  private var _firstRun: Boolean = true

  // 单个视频对应推荐个数
  private val RECOMMEND_SIZE: Int = 30

  private lazy val hbaseConf: HBaseConf = conf.hbaseConf.get

  override def streamHandler(dStream: DStream[String]): DStream[YouXiangData] = {

    dStream.tryMap(line => new Gson().fromJson(line, classOf[KafkaMessage]))
      .mapPartitions(messages => {

        if (messages.isEmpty) {
          log.info("================= No data event need transform =================")
          Seq.empty[YouXiangData].toIterator
        } else {

          val labels: Seq[YouXiangLabel] =
            getAllValue[YouXiangLabel](hbaseConf.zooQuorum, labelTableName, labelColumnFamily)

          val categoryLabels = labels.filter(l => l.labelGroupId == "1").map(l => l.id)
          val yearLabels = labels.filter(l => l.labelGroupId == "2").map(l => l.id)
          val styleLabels = labels.filter(l => l.labelGroupId == "3").map(l => l.id)
          val areaLabels = labels.filter(l => l.labelGroupId == "4").map(l => l.id)

          messages.flatMap(m => {
            val event = m.eventType
            val table = m.tableName
            val row = if (event.equals("DELETE")) m.beforeRow.toMap else m.afterRow.toMap

            val res = tableColumnMap.get(table).flatMap(columns => {
              lazy val b = m.beforeRow.toMap.filterKeys(k => columns.contains(k)).map(identity)
              lazy val a = m.afterRow.toMap.filterKeys(k => columns.contains(k)).map(identity)

              if (event == "UPDATE" && a == b) {
                None
              } else if (table.equals("v_video_source") && row.getOrElse("video_source", "") != "0") {
                // 如果数据来源非自有视频 则直接抛弃，避免多剧集视频来源相互覆盖
                None
              } else {

                val data = if (table.equals("v_video_ext")) {
                  val key = row.getOrElse("property", "")
                  val value = row.getOrElse("value", "")
                  Map("video_base_id" -> row("video_base_id"), key -> value)
                } else if (table.equals("v_video_label")) {
                  labelTransfer(categoryLabels, styleLabels, areaLabels, yearLabels, row)
                } else {
                  row.filterKeys(k => columns.contains(k)).map(identity)
                }

                Some(YouXiangData(table, event, data))
              }
            })

            res
          })
        }

      })

  }

  def labelTransfer(categoryLabels: Seq[String],
                    styleLabels: Seq[String],
                    areaLabels: Seq[String],
                    yearLabels: Seq[String],
                    m: Map[String, String]): Map[String, String] = {

    val label = m.getOrElse("label_id", "")

    val mm = if (categoryLabels.contains(label))
      Map("category" -> label)
    else if (styleLabels.contains(label))
      Map("styles" -> label)
    else if (areaLabels.contains(label))
      Map("areas" -> label)
    else if (yearLabels.contains(label))
      Map("year" -> label)
    else
      Map.empty[String, String]

    mm ++ Map("video_base_id" -> m.getOrElse("video_base_id", ""))
  }


  override def saveToHBase(cf: HBaseConf, dStream: DStream[YouXiangData])(implicit ss: SparkSession): Unit = {
    createTable(cf)

    dStream.foreachRDD(_.handleFailAction("you xiang Recommend save to HBase error", _.foreachPartition(res => {
      if (res.isEmpty) {
        log.info("================ No data event need save to HBase================")
      } else {
        usingTable(cf.zooQuorum, cf.table) { table =>
          res.foreach(yd => {
            val videoBaseId = yd.data.getOrElse("video_base_id", "")

            if (videoBaseId == "") {
              log.error(s"video_base_id is null, youxiang data is $yd")
            } else {
              val rowId = videoBaseId.getBytes
              var put = new Put(rowId)
              yd.data.foreach {
                case (k, v) =>
                  val keyByte = k.getBytes

                  val value = {
                    if ("v_video_label".equals(yd.tableName) && k != "video_base_id") {
                      val originalValue = getValue(table, rowId, cf.columnFamily.getBytes, keyByte)
                      log.debug(s"v_video_label, origin value: $originalValue, key: $videoBaseId, value: $v")
                      if (yd.event.equals("INSERT") || yd.event.equals("UPDATE"))
                        originalValue.map(s => s + "," + v).getOrElse(v)
                      else
                        originalValue.map { s => s.split(",").filterNot(e => e.equals(v)).mkString(",") }.getOrElse("")

                    } else {
                      if (yd.event.equals("INSERT") || yd.event.equals("UPDATE")) v else ""
                    }
                  }

                  put = put.addColumn(cf.columnFamily.getBytes, keyByte, value.getBytes)
              }
              table.put(put)

              // 删除 video
              val d = yd.event.equals("DELETE") && yd.tableName == "v_video_base"
              // 下架 video , 下架之后再上架 所以该条记录不应该被删除
              //              lazy val d1 = yd.event.equals("UPDATE") && yd.tableName == "v_video_base" && yd.data.getOrElse("status", "") == "3"

              if (d) {
                val del = new Delete(rowId)
                table.delete(del)
                log.info(s"delete table: ${yd.tableName}, labelId: $videoBaseId")
              }
            }
          })
        }
      }
    })))

    val s = conf.sec * 2
    dStream.window(Seconds(s), Seconds(s)).foreachRDD(r => {
      if (_firstRun || readToStartRealTimeCompute(r.collect().toSeq)) {
        _firstRun = false
        postProcess(ss)
      } else {
        log.info("没有上架／下架 视频的更新 事件")
      }
    })

  }

  def readToStartRealTimeCompute(data: Seq[YouXiangData]): Boolean = {
    val onShelveOffShelve = data.filter(_.tableName == "v_video_base").find(d => {
      val status = d.data.getOrElse("status", "")

      // 只有上架 或者 下架操作  才触发实时计算
      status == "2" || status == "3"
    })

    data.nonEmpty && onShelveOffShelve.isDefined
  }


  //========================================================================

  def map2VideoInfo(m: Map[String, String]): Option[YouXiangVideoInfo] = {
    Try(m.convert[YouXiangVideoInfo]()) match {
      case Success(v) => Some(v)
      case Failure(e) =>
        log.warn(e.getMessage)
        None
    }
  }

  def postProcess(ss: SparkSession): Unit = {
    log.info("start post process(real time recommend compute) =====================")
    require(conf.hbaseConf.isDefined, "HBase configuration should be defined first")

    val sc = ss.sparkContext

    val con: Configuration = HBaseConfiguration.create()
    con.set("hbase.zookeeper.quorum", hbaseConf.zooQuorum)
    con.set(TableInputFormat.INPUT_TABLE, hbaseConf.table) // 设置查询的表名

    val a = new SingleColumnValueFilter("result".getBytes, "status".getBytes, CompareOp.EQUAL, Bytes.toBytes("2"))
    val b = new SingleColumnValueFilter("result".getBytes, "video_source".getBytes, CompareOp.EQUAL, Bytes.toBytes("0"))
    val c = new SingleColumnValueFilter("result".getBytes, "category".getBytes, CompareOp.NOT_EQUAL, Bytes.toBytes(""))
    val scan = new Scan().setFilter(new FilterList(a, b, c))

    con.set(TableInputFormat.SCAN, convertScanToString(scan))

    //    con.set(TableInputFormat.SCAN_COLUMN_FAMILY, hbaseConf.columnFamily.head)

    val userRDD = sc.newAPIHadoopRDD(con, classOf[TableInputFormat],
      classOf[org.apache.hadoop.hbase.io.ImmutableBytesWritable],
      classOf[org.apache.hadoop.hbase.client.Result])

    log.info("############################################")
    log.info(s"all eligible video size: ${userRDD.count()}")
    log.info("############################################")

    val allVideo = getAllValueMap(hbaseConf.zooQuorum, hbaseConf.table, hbaseConf.columnFamily)
      .flatMap(m => map2VideoInfo(m))
      .filter(y => y.eligibleForCompute)

    val res = userRDD.map { case (_, result) => parseResult(result).convert[YouXiangVideoInfo]() }
      .repartition(10)
      .mapPartitions {
        _.flatMap(videoInfo =>
          if (videoInfo.eligibleForCompute) {
            val all = allVideo
              .filter(y => (y.categorySet & videoInfo.categorySet).nonEmpty)
              .filter(_.videoBaseId != videoInfo.videoBaseId)

            val recommend = startRealTimeCompute(videoInfo, all)
            // 单个视频对比结果，取前50条记录存入数据库。因为全部入库没有太大意义。
            val recommendSortTruncate = recommend.sortWith((a, b) => a.score > b.score).take(RECOMMEND_SIZE)

            log.info(s"Recommend result: videoBaseId: ${videoInfo.videoBaseId}, size: ${recommendSortTruncate.size}")
            Option(recommendSortTruncate)

          } else {
            log.info(s"[WARNING] Video not eligible for compute: ${videoInfo.videoBaseId}")
            None
          })
      }.flatMap(identity)

    //    saveToRDMB(ss, res)
    saveToJDBC(recommendTable, mysqlConfig, res)
  }

}

object YouXiangRecommend {

  lazy val mysqlConfig: MysqlConfig = config.getConfig("youxiangrt.mysql").convert[MysqlConfig]
  lazy val recommendTable: String = config.getString("youxiangrt.recommendTable")

  lazy val labelTableName: String = config.getString("youxiangrt.label.tableName")
  lazy val labelColumnFamily: String = config.getString("youxiangrt.label.columnFamily")

  def main(args: Array[String]): Unit = {
    val p = config.getConfig("canal").convert[EngineConfigure]
    val handler = new YouXiangRecommend(p)

    handler.start()
  }


  def saveToRDMB(spark: SparkSession, res: RDD[YouXiangVideoRecommend]): Unit = {
    import spark.implicits._

    val df = res.toDF()

    RDBMUtil.saveJDBC(df, recommendTable, mysqlConfig)
  }

  import com.hawker.utils.AutoClose.using

  def saveToJDBC(table: String, conf: MysqlConfig, res: RDD[YouXiangVideoRecommend]): Unit = {
    val truncateTable = s"TRUNCATE TABLE $table"
    val insertTable = s"INSERT INTO $table(videoBaseId,recommendVideoId,score,categoryId,generateTime) values (?,?,?,?,?)"

    Class.forName(conf.driver)

    // truncate table first
    using(DriverManager.getConnection(conf.url, conf.user, conf.password)) { conn =>
      using(conn.prepareCall(truncateTable))(_.execute())
    }

    res.foreachPartition(rs =>
      using(DriverManager.getConnection(conf.batchUrl, conf.user, conf.password)) { conn =>
        conn.setAutoCommit(false) // 手动提交

        using(conn.prepareStatement(insertTable)) { ps =>
          rs.foreach(r => {
            ps.setString(1, r.videoBaseId)
            ps.setString(2, r.recommendVideoId)
            ps.setDouble(3, r.score)
            ps.setString(4, r.categoryId)
            ps.setString(5, r.generateTime)
            ps.addBatch()
          })

          ps.executeBatch()
          conn.commit()
        }
      }
    )
  }


}


case class YouXiangData(tableName: String, event: String, data: Map[String, String]) extends Serializable

case class YouXiangVideoInfo(videoBaseId: String,
                             name: Option[String],
                             category: Option[String], // 分类
                             actor: Option[String],
                             director: Option[String],
                             styles: Option[String],
                             areas: Option[String],
                             year: Option[String],
                             status: Option[String],
                             sign: Option[String] = None,
                             keyword: Option[String],
                             videoSource: Option[String]) {


  // 是否所有的属性都有值
  // 如果都有值，则可以触发推荐计算，否则无须触发
  def eligibleForCompute: Boolean = {
    status.isDefined && status.get == "2" && // 上架
      videoSource.isDefined && videoSource.get == "0" && // 自有视频
      category.isDefined
  }

  def categorySet: Set[String] = toList(category).toSet

  def actorList: Array[String] = toList(actor)

  def styleList: Array[String] = toList(styles)

  def areaList: Array[String] = toList(areas)

  def directorList: Array[String] = toList(director)

  val keyWordList: Array[String] = toList(keyword)


  private def toList(para: Option[String]) = {
    def cleanParameter(a: String): Array[String] = {
      if (a.contains(",")) a.split(",")
      else if (a.contains("/")) a.split("/")
      else if (a.contains("、")) a.split("、")
      else if (a.contains("，")) a.split("，")
      else if (a.contains(" ")) a.split(" ")
      else Array(a)
    }

    para.map(a => cleanParameter(a).filter(_ != "")).getOrElse(Array.empty)
  }

}

case class YouXiangVideoRecommend(videoBaseId: String,
                                  recommendVideoId: String,
                                  score: Double,
                                  categoryId: String,
                                  generateTime: String = DateUtil.nowStr)



