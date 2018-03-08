package com.hawker.core

import com.hawker.utils.DateUtil
import com.hawker.utils.MapUtil._
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.{KStream, KStreamBuilder, ValueMapper}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.reflect.{ClassTag, classTag}
import org.squeryl.PrimitiveTypeMode._

/**
  * @author mingjiang.ji on 2017/11/29
  */
abstract class CanalCommonTransfer[T: ClassTag](config: KStreamConfig) extends CommonTransfer[T](config) {

  protected[CanalCommonTransfer] def kafkaMessageStream(topic: String, sb: KStreamBuilder): KStream[String, KafkaMessage] = {
    val orderStream: KStream[String, String] = sb.stream(Serdes.String, Serdes.String, topic)

    orderStream.mapValues(new ValueMapper[String, KafkaMessage]() {
      override def apply(value: String): KafkaMessage =
        gson.fromJson(value, classOf[KafkaMessage])
    })
  }


  /**
    * 检测字段对应的值是否有变动，如果设置值，
    * 且对应的字段值没有变动，则从返回对象中剔除该属性值。
    */
  protected val monitorKey = Seq.empty[String]

  /**
    * kafka流消息值类型转换
    *
    * @return
    */
  override def streamTransfer(streamBuilder: KStreamBuilder): KStream[String, T] = {
    val ks = kafkaMessageStream(config.topic, streamBuilder)
    config.print.foreach(a => if (a) ks.print())

    ks.mapValues(new ValueMapper[KafkaMessage, T]() {

      override def apply(value: KafkaMessage): T = {
        lazy val b = value.beforeRow.toMap.camelCaseKey
        lazy val a = value.afterRow.toMap.camelCaseKey

        val map = if (value.eventType == "UPDATE") {
          var a1 = a

          monitorKey.foreach(k => {
            if (a1.get(k) == b.get(k)) a1 = a1.-(k)
          })
          a1
        } else if (value.eventType == "DELETE") {
          b
        } else {
          a
        }

        val m1 = map ++ Map(
          "eventType" -> value.eventType,
          "timeStamp" -> DateUtil.nowStr)

        val json = gson.toJson(m1.asJava)
        gson.fromJson(json, classTag[T].runtimeClass)
      }
    })
  }

}
