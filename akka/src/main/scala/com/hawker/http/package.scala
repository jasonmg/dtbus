package com.hawker

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
  * @author mingjiang.ji on 2017/10/26
  */
package object http {

  case class Result(code: Int, msg: String)

  object Result1 {
    def success(msg: String) = Result(0, msg)

    def success = Result(0, "success")

    def failure(code: Int, msg: String) = Result(code, msg)
  }


  trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val resultFormat = jsonFormat2(Result)
  }


  case class HBaseConf(zooQuorum: String,
                       table: String,
                       columnFamily: String,
                       maxVersions: Option[Int]) {

    require(zooQuorum.nonEmpty, "HBase zookeeper address should be provide first")
    require(table.nonEmpty, "Table name should be provide first")
    require(columnFamily.nonEmpty, "ColumnFamily should be provide first")
  }

  case class KafkaConf(bootstrapServers: String,
                       topic: String,
                       clientId: String,
                       groupId: String,
                       keySerializer: Option[String],
                       valueSerializer: Option[String])

}
