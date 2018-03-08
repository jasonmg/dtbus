package com.hawker.core

import java.io.Serializable

import scala.collection.JavaConversions._

/**
  * @author mingjiang.ji on 2017/12/12
  */
case class KafkaMessage(sourceType: String,
                        port: Int,
                        dbName: String,
                        ip: String,
                        afterRow: java.util.Map[String, String],
                        beforeRow: java.util.Map[String, String],
                        eventType: String,
                        tableName: String,
                        executeTime: Long) extends Serializable



object KafkaMessageUtil {


  def toSql(msg: KafkaMessage): String = {


    if (msg.eventType == "INSERT") {
      val (keys, values) = insertStatement(msg.afterRow.toMap)

      s"INSERT INTO ${msg.tableName}($keys) VALUES ($values);"
    } else if (msg.eventType == "UPDATE") {
      val (idStr, update) = updateStatement(msg.afterRow.toMap)

      s"UPDATE ${msg.tableName} SET $update WHERE id=$idStr;"
    } else
      ""
  }

  def insertStatement(m: Map[String, String]): (String, String) = {
    val s = m.toSeq
    (s.map(_._1).mkString(","), s.map(_._2).mkString(","))
  }

  def updateStatement(m: Map[String, String]): (String, String) = {
    val id = m.getOrElse("id", throw new RuntimeException("对于更新语句，应该提供id作为主键，以便构建update语句"))

    (id, m.toSeq.map { case (k, v) => s"$k=$v" }.mkString(","))
  }
}
