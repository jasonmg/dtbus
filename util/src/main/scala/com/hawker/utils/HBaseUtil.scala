package com.hawker.utils

import com.hawker.utils.AutoClose._
import com.hawker.utils.MapUtil._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase._
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.protobuf.ProtobufUtil
import org.apache.hadoop.hbase.util.{Base64, Bytes}

import scala.collection.IterableView
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.reflect.ClassTag
import scala.reflect.runtime.{universe => ru}

/**
  * @author mingjiang.ji on 2017/9/27
  */
object HBaseUtil extends Log {


  /**
    * NOTE: 调用该方法，确保调用完之后关闭连接释放资源。
    * 正确的调用方法为:
    * {{{
    *   usingTable(zookQuorum, table){t =>
    *      ...
    *      getValue(t, rowId, columnFamily, columnKey)
    *      ...
    *   }
    * }}}
    */
  def getValue(table: Table, rowId: Array[Byte], columnFamily: Array[Byte], columnKey: Array[Byte]): Option[String] = {
    getValues(table, rowId, columnFamily, columnKey, 1).flatMap(_.headOption)
  }


  def getValues(table: Table, rowId: Array[Byte], columnFamily: Array[Byte], columnKey: Array[Byte], maxVersions: Int): Option[Seq[String]] = {
    val get = new Get(rowId).addColumn(columnFamily, columnKey)
    get.setMaxVersions(maxVersions)
    val result = table.get(get)

    val cells = result.getColumnCells(columnFamily, columnKey)

    val r = cells.map(c => Bytes.toString(CellUtil.cloneValue(c)))
    Option(r)
  }

  /**
    * NOTE: 调用该方法，确保调用完之后关闭连接释放资源。
    * 正确的调用方法为:
    * {{{
    *   usingTable(zookQuorum, table){t =>
    *      ...
    *      getValue(t, rowId, columnFamily, columnKey)
    *      ...
    *   }
    * }}}
    */
  def getValue(table: Table, rowId: Array[Byte], columnFamily: Array[Byte] = "result".getBytes): Map[String, String] = {
    getValue(table, rowId, columnFamily, 1).head
  }

  def getValue(table: Table, rowId: Array[Byte], columnFamily: Array[Byte], maxVersions: Int): Seq[Map[String, String]] = {
    val get = new Get(rowId).addFamily(columnFamily)
    get.setMaxVersions(maxVersions)
    val result = table.get(get)

    parseResults(result)
  }

  def getAllValue[T: ru.TypeTag : ClassTag](zookQuorum: String, tableName: String, columnFamily: String): Seq[T] =
    getAllValueMap(zookQuorum, tableName, columnFamily).map(m => m.convert[T]())


  def getAllValueMap(zookQuorum: String, table: String, columnFamily: String): Seq[Map[String, String]] = {
    usingTable(zookQuorum, table) { table =>
      val scan = new Scan().addFamily(columnFamily.getBytes)
      val resultScanner = table.getScanner(scan)

      resultScanner.map(r => parseResult(r)).toSeq
    }
  }

  def getAllValueMapStream(zookQuorum: String, table: String, columnFamily: String): Stream[Map[String, String]] = {
    usingTable(zookQuorum, table) { table =>
      val scan = new Scan().addFamily(columnFamily.getBytes)
      val resultScanner = table.getScanner(scan)
      resultScanner.map(r => parseResult(r))
    }.toStream
  }

  /**
    * 拉取得到hbase表中的全部数据，构建成map返回，
    * 由于hbase数据量巨大，一次性拉取全部数据可能导致executor内存爆掉，所以此方法返回 view which is lazy evaluation.
    *
    * @return IterableView
    */
  def getAllValueMapView(table: Table, columnFamily: String): IterableView[Map[String, String], Iterable[_]] = {
    val scan = new Scan().addFamily(columnFamily.getBytes)
    val resultScanner = table.getScanner(scan)
    resultScanner.view.map(r => parseResult(r))
  }

  def parseResult(result: Result): Map[String, String] = {
    val cells = result.rawCells()

    val res = cells.map(cell => {
      
      val col_name = Bytes.toString(CellUtil.cloneQualifier(cell))
      val col_value = Bytes.toString(CellUtil.cloneValue(cell))

      (col_name, col_value)

    })

    res.toMap.withDefaultValue("")
  }

  def parseResults(result: Result): Seq[Map[String, String]] = {
    val cells = result.rawCells()

    val res = cells.groupBy(_.getTimestamp).values.map(cellArray => {

      val m = cellArray.map(cell => {
        val col_name = Bytes.toString(CellUtil.cloneQualifier(cell))
        val col_value = Bytes.toString(CellUtil.cloneValue(cell))

        (col_name, col_value)
      }).toMap

      m.withDefaultValue("")
    }).toSeq

    res
  }

  def getAllValueMap1(zookQuorum: String, table: String, columnFamily: String) = {
    usingTable(zookQuorum, table) { table =>
      val scan = new Scan().addFamily(columnFamily.getBytes)
      scan.setMaxVersions(10)
      val resultScanner = table.getScanner(scan)

      val a = resultScanner.map(r => parseResults(r)).filter(_.size > 1).toSeq
      a
    }
  }


  def convertScanToString(scan: Scan): String = {
    val proto = ProtobufUtil.toScan(scan)
    Base64.encodeBytes(proto.toByteArray)
  }

  def save(zooQuorum: String, table: String, res: Seq[Put]): Unit = usingTable(zooQuorum, table)(_.put(res.asJava))

  // key, columnFamily, value
  type HBaseRow = (String, String, Map[String, Any])

  def saveRows(zooQuorum: String, table: String, data: Seq[HBaseRow]): Unit = {

    val dataPuts = data.flatMap { case (key, columnFamily, value) =>
      val put = new Put(key.getBytes)
      val cfb = columnFamily.getBytes

      value.map { case (k, v) => put.addColumn(cfb, k.getBytes, v.toString.getBytes) }
    }

    save(zooQuorum, table, dataPuts)
  }

  def saveRow(zooQuorum: String, table: String, data: HBaseRow): Unit = saveRows(zooQuorum, table, Seq(data))

  def usingTable[O](zooQuorum: String, table: String)(fn: Table => O): O = {
    using(getHBaseConnection(zooQuorum)) { conn =>
      using(conn.getTable(TableName.valueOf(table)))(fn)
    }
  }

  def createTable(zooQuorum: String, table: String, columnFamilies: String, maxVersions: Int = 1): Unit = {

    using(getHBaseConnection(zooQuorum)) { connection =>
      using(connection.getAdmin) { admin =>

        val userTable = TableName.valueOf(table)
        if (!admin.tableExists(userTable)) {
          var tableDescr = new HTableDescriptor(userTable)
          columnFamilies.split(",").foreach(f => {
            val hcd = new HColumnDescriptor(f.getBytes)
            hcd.setMaxVersions(maxVersions)
            tableDescr = tableDescr.addFamily(hcd)
          })

          log.info(s"Creating table $table, columnFamily: $columnFamilies")

          admin.createTable(tableDescr)
          log.info(s"Create table $table done.")
        } else {
          log.info(s"Table $table already exist.")
        }
      }
    }
  }

  private def getHBaseConnection(zookQuorum: String): Connection = {
    val con: Configuration = HBaseConfiguration.create()
    con.set("hbase.zookeeper.quorum", zookQuorum)
    //    con.set("zookeeper.znode.parent", "/hbase-secure")
    ConnectionFactory.createConnection(con)
  }


  def main(args: Array[String]): Unit = {

    val zoo = "dx03-bigdata-prod:2181,dx04-bigdata-prod:2181,dx05-bigdata-prod:2181"

    val table = "jcy_embed_test_1"
    //    createTable(zoo, table, "test", 10)

    val f = Foo("jason111", 121, "asdfasdf")
    val row = (f.name, "test", Reflect4SUtil.instance2Map(f))
    //    saveRow(zoo, table, row)

    //    usingTable(zoo, table) { t =>
    //      val r = getValue(t, f.name.getBytes, "test".getBytes, 3)
    //      println(r)
    //    }

    val a = getAllValueMap(zoo, "youxiang_rt_recommend_pre", "result")
    println(a)


    //    val zoo = List("dx01-bigdata-test:2181", "dx03-bigdata-test:2181", "dx02-bigdata-test:2181").mkString(",")
    //    val categorySet = Set("11", "12", "13", "14")
    //
    //    val r = getAllValueMapStream(zoo, "youxiang_rt_recommend", "result")
    //
    //    r.take(10).foreach(println)
    //    r.take(10).foreach(println)

    //    usingTable(zoo, "youxiang_rt_recommend") { table =>
    //      val r = getAllValueMapView(table, "result")
    //        .map(m => m.convert[YouXiangVideoInfo]())
    //        .filter(y => y.eligibleForCompute)
    //        .filter(y => (y.categorySet & categorySet).nonEmpty)
    //
    //     r.map(a => {
    //        println(a)
    //        a
    //      }).toSeq
    //    }
  }


  case class Foo(name: String, age: Int, address: String)

}
