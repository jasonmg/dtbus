package com.hawker.exception

import java.util.Properties

import com.hawker.core.KafkaConf
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

/**
  * @author mingjiang.ji on 2017/9/6
  */
class ExceptionHandlerKafka(kc: KafkaConf) extends ExceptionHandler {

  private val props = {
    val props = new Properties()
    props.put("bootstrap.servers", kc.bootstrapServers)
    props.put("client.id", kc.clientId)
    props.put("key.serializer", kc.keySerializer.getOrElse("org.apache.kafka.common.serialization.StringSerializer"))
    props.put("value.serializer", kc.valueSerializer.getOrElse("org.apache.kafka.common.serialization.StringSerializer"))
    props
  }

  override def handler(line: String, ex: Throwable): Unit = {
    val msg = s"Line: $line, Error: ${ex.getMessage}"
    val producer = new KafkaProducer[String, String](props)
    val data = new ProducerRecord[String, String](kc.topic, msg)
    producer.send(data)
  }
}
