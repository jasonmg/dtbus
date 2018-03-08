package com.hawker.recommend

import com.google.gson.Gson
import com.hawker.canal.KafkaMessage
import com.hawker.core._
import com.hawker.utils.ConfigUtil._
import com.hawker.utils.HBaseUtil
import com.hawker.utils.HBaseUtil.usingTable
import com.hawker.utils.StringUtil._
import org.apache.hadoop.hbase.client.{Delete, Put}
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream

import scala.collection.JavaConversions._


/**
  * v_label 同步
  *
  * @author mingjiang.ji on 2017/10/9
  */
class YouXiangLabelSync(conf: EngineConfigure) extends Engine[YouXiangData](conf) {

  private val tableColumnMap = Map(
    "v_label" -> List("id", "label_group_id", "label_name")
  )

  override def streamHandler(dStream: DStream[String]): DStream[YouXiangData] = {

    dStream.tryMap(line => new Gson().fromJson(line, classOf[KafkaMessage]))
      .tryFlatMap(m => {
        val event = m.eventType
        val table = m.tableName

        val res = tableColumnMap.get(table).map(columns => {
          val row = if (event.equals("DELETE")) m.beforeRow.toMap else m.afterRow.toMap
          val data = row.filterKeys(k => columns.contains(k)).map(identity)

          YouXiangData(table, event, data)
        })

        res
      })

  }

  override def saveToHBase(cf: HBaseConf, dStream: DStream[YouXiangData])(implicit ss: SparkSession): Unit = {
    createTable(cf)

    dStream.foreachRDD(_.handleFailAction("you xiang Label save to HBase error", _.foreachPartition(res => {
      if (res.isEmpty) {
        log.info("================ No data event need save to HBase================")
      } else {
        usingTable(cf.zooQuorum, cf.table) { table =>
          res.foreach(yd => {
            val labelId = yd.data.getOrElse("id", "")
            val rowId = labelId.getBytes

            if (yd.event.equals("INSERT") || yd.event.equals("UPDATE")) {

              var put = new Put(rowId)
              yd.data.foreach { case (k, v) =>
                log.info(s"insert into: ${yd.tableName}, key: $k, value: $v")
                put = put.addColumn(cf.columnFamily.getBytes, k.getBytes, v.getBytes)
              }
              table.put(put)
            }

            if (yd.event.equals("DELETE")) {
              val del = new Delete(rowId)
              table.delete(del)
              log.info(s"delete table: ${yd.tableName}, labelId: $labelId")
            }
          })
        }
      }
    })))
  }
}

case class YouXiangLabel(id: String, labelGroupId: String, labelName: String)


object YouXiangLabelSync {

  def main(args: Array[String]): Unit = {
    val p = config.getConfig("canal").convert[EngineConfigure]

    val tableName: String = config.getString("youxiangrt.label.tableName")
    val columnFamily: String = config.getString("youxiangrt.label.columnFamily")

    val hbase = p.hbaseConf.map(c => c.copy(table = tableName, columnFamily = columnFamily))
    val newConfig = p.copy(hbaseConf = hbase)

    val handler = new YouXiangLabelSync(newConfig)

    handler.start()
  }

}
