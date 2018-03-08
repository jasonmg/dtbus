package com.hawker.core

import java.util.Properties

import com.hawker.exception.ExceptionHandler
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.spark.streaming.dstream.DStream

/**
  * @author mingjiang.ji on 2017/9/7
  */
trait ToKafka[O] {

  implicit val exceptionHandler: ExceptionHandler

  private[core] def saveToKafka(kc: KafkaConf, dStream: DStream[O]): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", kc.bootstrapServers)
    props.put("client.id", kc.clientId)
    props.put("key.serializer", kc.keySerializer.getOrElse("org.apache.kafka.common.serialization.StringSerializer"))
    props.put("value.serializer", kc.valueSerializer.getOrElse("org.apache.kafka.common.serialization.StringSerializer"))

    dStream.foreachRDD(_.handleFailAction("save to kafka error", _.foreachPartition(res => {
      val producer = new KafkaProducer[String, O](props)
      res.foreach(o => {
        val data = new ProducerRecord[String, O](kc.topic, o)
        producer.send(data)
      })
    })))

  }
}
