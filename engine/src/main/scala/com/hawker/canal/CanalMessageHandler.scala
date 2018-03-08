package com.hawker.canal

import java.util.{Map => JMap}

import com.google.gson.Gson
import com.hawker.core.{Engine, EngineConfigure, _}
import com.hawker.utils.ConfigUtil._
import org.apache.hadoop.hbase.client.Put
import org.apache.spark.streaming.dstream.DStream

import scala.collection.JavaConversions._

/**
  * @author mingjiang.ji on 2017/8/30
  */
class CanalMessageHandler(conf: EngineConfigure) extends Engine[String](conf) {

  override def streamHandler(dStream: DStream[String]): DStream[String] = {
    dStream.tryMap(line => new Gson().fromJson(line, classOf[KafkaMessage]))
      .tryMap(m => {
        val event = m.eventType

        if ("INSERT".equals(event))
          insertSql(m)
        else if ("UPDATE".equals(event))
          updateSql(m)
        else if ("DELETE".equals(event))
          deleteSql(m)
        else ""
      })
  }

  def insertSql(km: KafkaMessage): String = {
    val sql = km.afterRow.values().toSeq.mkString(",")
    s"${km.eventType}@${km.executeTime}@$sql"
  }

  def updateSql(km: KafkaMessage): String = {
    val sql = km.afterRow.values().toSeq.mkString(",")
    s"${km.eventType}@${km.executeTime}@$sql"
  }

  def deleteSql(km: KafkaMessage): String = {
    val sql = km.beforeRow.values().toSeq.mkString(",")
    s"${km.eventType}@${km.executeTime}@$sql"
  }

  override def streamToPut(dStream: DStream[String]): DStream[Put] = {
    dStream.tryMap(c => {
      val a = c.split("@")

      val (evenType, et, sql) = (a(0), a(1), a(2))
      val key = sql.split(",")(0)
      val k = s"${evenType}_$key"

      val put = new Put(k.getBytes)
      put.addColumn("result".getBytes, "sql".getBytes, s"$et@$sql".toString.getBytes)
    })
  }

}


object CanalMessageHandler extends App {
  val p = config.getConfig("canal").convert[EngineConfigure]
  val handler = new CanalMessageHandler(p)

  handler.start()
}



