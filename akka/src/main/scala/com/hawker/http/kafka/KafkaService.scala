package com.hawker.http.kafka

import java.util.concurrent.{Future => JFuture}

import akka.actor.{Actor, ActorSystem}
import akka.kafka.ProducerSettings

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}


/**
  * @author mingjiang.ji on 2017/10/26
  */
class KafkaService(system: ActorSystem, brokers: String) extends Actor {

  val producerSettings: ProducerSettings[Array[Byte], String] = ProducerSettings(system, new ByteArraySerializer, new StringSerializer)
    .withBootstrapServers(brokers)

  val kafkaProducer: KafkaProducer[Array[Byte], String] = producerSettings.createKafkaProducer()

  def send(msg: KafkaMessage): JFuture[RecordMetadata] = {
    val r = new ProducerRecord[Array[Byte], String](msg.topic, msg.message)
    kafkaProducer.send(r)
  }


  override def receive: Receive = {
    case Send(msg) =>
      send(msg).get
      sender() ! "success"
  }
}


case class Send(msg: KafkaMessage)
