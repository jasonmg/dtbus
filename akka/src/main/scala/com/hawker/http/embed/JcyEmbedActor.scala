package com.hawker.http.embed

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, Props}
import akka.routing.{Broadcast, RoundRobinPool}
import com.hawker.http.{HBaseConf, KafkaConf}
import com.hawker.utils.{HBaseUtil, Log, MD5}

import scala.concurrent.duration._
import scala.util.Success

/**
  * @author mingjiang.ji on 2017/11/23
  */
class JcyEmbedActor(kafkaConf: KafkaConf, hbaseConf: HBaseConf) extends Actor with Log {


  val router = RoundRobinPool


  val acc = context.actorOf(Props(new AccumulatorActor), "accumulator-actor")
  context.watch(acc)

  val hbase = context.actorOf(Props(new HBaseActor(hbaseConf, acc)).withDispatcher("hbase-dispatcher").withRouter(router(4)), "jcy-embed-hbase-actor")
  context.watch(hbase)

  val kafka = context.actorOf(Props(new KafkaActor(kafkaConf, hbase, self)), "jcy-embed-kafka-actor")
  context.watch(kafka)


  val hbase_active = new AtomicInteger()

  override def preStart(): Unit = {
    kafka ! StartKafka
    hbase ! Broadcast(StartHBase)
  }

  import scala.concurrent.ExecutionContext.Implicits.global

  context.system.scheduler.schedule(1 second, 15 second, hbase, Broadcast(Flush))

  override def receive: Receive = {

    case Start(topic, data) =>
      data.foreach(d => kafka ! SendToKafka(topic, d))
      sender() ! Success("success")

    case KafkaStarted =>
      log.info("Kafka Actor started...")

    case HBaseStarted =>
      log.info(s"HBase Actor started, active: ${hbase_active.incrementAndGet()}")
  }
}

case class Start(topic: String, data: Seq[JcyEmbed])

case object EmptyData

object JcyEmbedActor {


  def apply(kafkaConf: KafkaConf, hbaseConf: HBaseConf) = {
    val maxVersion = hbaseConf.maxVersions.getOrElse(1)
    // create table first
    HBaseUtil.createTable(hbaseConf.zooQuorum, hbaseConf.table, hbaseConf.columnFamily, maxVersion)

    new JcyEmbedActor(kafkaConf, hbaseConf)
  }

  def genRowKey(j: JcyEmbed): String = MD5.toMD5(j.toString, "utf8")
}
