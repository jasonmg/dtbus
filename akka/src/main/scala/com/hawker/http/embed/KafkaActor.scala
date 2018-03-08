package com.hawker.http.embed

import java.util.Properties

import akka.actor.{Actor, ActorRef}
import com.hawker.http.KafkaConf
import com.hawker.utils.Log
import com.hawker.utils.kafka.serializer.Producer
import org.apache.kafka.clients.consumer.ConsumerConfig

/**
  * @author mingjiang.ji on 2017/11/28
  */
class KafkaActor(kafkaConf: KafkaConf, hbaseActor: ActorRef, jcyEmbedActor: ActorRef) extends Actor with Log {

  private val getProducerProps: Properties = {
    val props = new Properties
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConf.bootstrapServers)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConf.groupId)
    props
  }

  private val producer = new Producer[JcyEmbed](getProducerProps)


  override def receive = {
    case StartKafka =>
      sender() ! KafkaStarted

    case SendToKafka(topic, data) =>
      producer.sendData(topic, data)
      log.info(s"Kafka Sending, topic: $topic, data: $data")
      hbaseActor ! SaveToHBase(data)
  }

}


case object StartKafka

case object KafkaStarted

case class SendToKafka(topic: String, embed: JcyEmbed)
case object BatchEnd

case object SendKafkaDone
