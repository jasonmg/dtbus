package com.hawker.core

import java.sql.Timestamp
import java.util.Properties

import com.google.gson.{Gson, GsonBuilder}
import com.hawker.utils.gson.{IntegerTypeAdapter, LongTypeAdapter, TimestampTypeAdapter}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.{KStream, KStreamBuilder}
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

import scala.reflect.ClassTag
import org.squeryl.PrimitiveTypeMode._

/**
  * @author mingjiang.ji on 2017/11/14
  */
abstract class CommonTransfer[T: ClassTag](config: KStreamConfig) extends ToMysql {

//  override protected val dbConfig: DBConfig = config.dBConfig

  private val props: Properties = {
    val props = new Properties

    val offset = config.fromBeginning.map(a => if (a) "earliest" else "latest").getOrElse("latest")

    props.put(StreamsConfig.APPLICATION_ID_CONFIG, config.applicationId)
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.brokers)
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass)
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass)
    props.put(ConsumerConfig.GROUP_ID_CONFIG, config.groupIdStr)
    props.put(ConsumerConfig.CLIENT_ID_CONFIG, config.clientIdStr)
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset) //  latest earliest
    //    props.put(StreamsConfig.TIMESTAMP_EXTRACTOR_CLASS_CONFIG, classOf[Nothing])
    props
  }

  protected val gson: Gson = new GsonBuilder()
    .registerTypeAdapter(classOf[java.lang.Integer], new IntegerTypeAdapter())
    .registerTypeAdapter(classOf[java.lang.Long], new LongTypeAdapter())
    .registerTypeAdapter(classOf[Timestamp], new TimestampTypeAdapter())
    .create()


  def streamTransfer(streamBuilder: KStreamBuilder): KStream[String, T]

  protected def postProcess(ks: KStream[String, T]): Unit = {}

  def start(): Unit = {
    implicit val sb: KStreamBuilder = new KStreamBuilder

    val ksTransfer: KStream[String, T] = streamTransfer(sb)
    config.print.foreach(a => if (a) ksTransfer.print())

    postProcess(ksTransfer)

    val kafkaStreams = new KafkaStreams(sb, props)

    kafkaStreams.cleanUp()
    kafkaStreams.start()
  }

}





