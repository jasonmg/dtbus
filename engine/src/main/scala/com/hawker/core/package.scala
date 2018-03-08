package com.hawker

import com.hawker.exception.ExceptionHandler
import com.hawker.utils.Mappable
import com.hawker.utils.Mappable._
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream

import scala.language.experimental.macros
import scala.reflect.runtime.{universe => ru}
import scala.util.{Failure, Success, Try}

/**
  * @author mingjiang.ji on 2017/9/6
  */
package object core {

  implicit def dStream2TryDStream[T](ds: DStream[T]): TryDStreamFunctions[T] = new TryDStreamFunctions[T](ds)
  
  implicit class FailHandlerRDD[T](rdd: RDD[T]) extends Serializable {
    def handleFailAction(head: String, fn: RDD[T] => Unit)(implicit exh: ExceptionHandler): Unit =
      Try(fn(rdd)) match {
        case Success(_) => Unit
        case Failure(ex) => exh.handler(head, ex)
      }
  }

  case class EngineConfigure(appName: String,
                             brokers: String,
                             consumerGroup: String,
                             topics: String,
                             fromBeginning: Option[Boolean],
                             master: Option[String],
                             hdfsPath: Option[String],
                             hbaseConf: Option[HBaseConf],
                             second: Option[Int],
                             checkPoint: Option[String],
                             print: Option[Boolean],
                             kafkaConf: Option[KafkaConf],
                             exceptionHandler: Option[ExceptionHandlerConf]) {

    val topicSet: Set[String] = topics.split(",").toSet
    val sec: Int = second.getOrElse(10) // stream slice 10s by default
  }


  case class KafkaConf(bootstrapServers: String,
                       topic: String,
                       clientId: String,
                       keySerializer: Option[String],
                       valueSerializer: Option[String])

  case class HBaseConf(zooQuorum: String,
                       table: String,
                       columnFamily: String) {

    require(zooQuorum.nonEmpty, "HBase zookeeper address should be provide first")
    require(table.nonEmpty, "Table name should be provide first")
    require(columnFamily.nonEmpty, "ColumnFamily should be provide first")
  }


  case class ExceptionHandlerConf(hdfsPath: Option[String],
                                  kafka: Option[KafkaConf])


}
