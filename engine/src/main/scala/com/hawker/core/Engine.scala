package com.hawker.core

import com.hawker.exception.{ExceptionHandler, ExceptionHandlerHDFS, ExceptionHandlerKafka, ExceptionHandlerLogging}
import com.hawker.utils.Log
import kafka.serializer.StringDecoder
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.kafka.common.serialization.StringDeserializer

/**
  * 此类为 "dtbus" spark唯一的入口类，实现步骤请参考以下：
  *
  * NOTE:
  * 1： 所有的实现都应该继承此类，并在实现类顶部导入 {{import com.hawker.core._}}，引入必要的 implicit method。
  * 2： 配置 application.conf 文件，配置详情参见 [[EngineConfigure]]。
  * 3： 实现 [[streamHandler()]] 方法。
  * 4： 如果需要结果写入HBase，请实现 [[ToHBase.streamToPut]] 方法。
  *
  * @author mingjiang.ji on 2017/8/29
  */
abstract class Engine[O](conf: EngineConfigure) extends Log with
  ToHBase[O] with ToKafka[O] with ToHDFS[O] with Serializable {

  private lazy val ss: SparkSession = {
    val ssb = SparkSession.builder().appName(conf.appName)
    conf.master.map(m => ssb.master(m)).getOrElse(ssb).getOrCreate()
  }

  private def createStreamContext(): StreamingContext = {
    val ssc = new StreamingContext(ss.sparkContext, Seconds(conf.sec))
    conf.checkPoint.foreach(cp => ssc.checkpoint(cp))
    ssc
  }

  def start(): Unit = {
    log.info(s"start spark kafka engine. \n$conf")

    val ssc = conf.checkPoint match {
      case Some(cp) => StreamingContext.getOrCreate(cp, createStreamContext)
      case None => createStreamContext()
    }

    // //latest, earliest
    val offset = conf.fromBeginning.map(a => if (a) "earliest" else "latest").getOrElse("latest")
//    val offset = conf.fromBeginning.map(a => if (a) "smallest" else "largest").getOrElse("largest")

    val map = Map(
      "bootstrap.servers" -> conf.brokers,
      "group.id" -> conf.consumerGroup,
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "auto.offset.reset" -> offset // largest/smallest 设置使用最开始的offset偏移量为该group.id的最早。如果不设置，则会是latest即该topic最新一个消息的offset
    )

//    val lines = KafkaUtils
//      .createDirectStream[String, String, StringDecoder, StringDecoder](ssc, map, conf.topicSet).map(_._2)

    val lines = KafkaUtils
      .createDirectStream[String, String](ssc,PreferConsistent ,
      Subscribe[String, String](conf.topicSet, map)).map(record => record.value)

    val res = streamHandler(lines)

    conf.print.foreach(p => if (p) res.print(200))
    conf.hdfsPath.foreach(path => saveToHDFS(res, path)(ss))
    conf.hbaseConf.foreach(hbc => saveToHBase(hbc, res)(ss))
    conf.kafkaConf.foreach(kc => saveToKafka(kc, res))

    ssc.start()

    sys.ShutdownHookThread {
      log.info("Gracefully stopping Spark Streaming Application")
      ssc.stop(true, true)
      log.info("Application stopped")
    }

    ssc.awaitTermination()
  }

  def streamHandler(dStream: DStream[String]): DStream[O]

  implicit val exceptionHandler: ExceptionHandler = {
    conf.exceptionHandler.map(
      eh => {
        if (eh.hdfsPath.isDefined) new ExceptionHandlerHDFS(eh.hdfsPath.get)
        else if (eh.kafka.isDefined) new ExceptionHandlerKafka(eh.kafka.get)
        else new ExceptionHandlerLogging(this)
      })
      .getOrElse(new ExceptionHandlerLogging(this))
  }

}



