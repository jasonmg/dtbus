package com.hawker.http.embed

import java.util
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.{Actor, ActorRef}
import com.google.gson.GsonBuilder
import com.hawker.http.HBaseConf
import com.hawker.http.embed.JcyEmbedActor.genRowKey
import com.hawker.utils.HBaseUtil.HBaseRow
import com.hawker.utils.{HBaseUtil, Log, ReflectUtil}

import scala.collection.JavaConversions._

/**
  * @author mingjiang.ji on 2017/11/28
  */
class HBaseActor(hbaseConf: HBaseConf, acc: ActorRef) extends Actor with Log {

  private val gson = new GsonBuilder().create()

  private val record = new ArrayBlockingQueue[HBaseRow](400)
  private val num = new AtomicInteger()

  override def receive = {

    case StartHBase =>
      sender() ! HBaseStarted

    case SaveToHBase(data) =>
      log.info("save to HBase ......")
      val instanceMap = ReflectUtil.instance2Map(data).toMap.filterKeys(_ != "params")
      val paramMap = gson.fromJson(data.getParams, classOf[java.util.Map[String, Object]]).toMap
        .map { case (k, v) => "params_" + k -> v }

      val res = (genRowKey(data), hbaseConf.columnFamily, instanceMap ++ paramMap)

      record.add(res)
      log.info(s"Save to memory, current num: ${num.get()}")

      if (num.incrementAndGet() >= 300) {
        flush()
      }


    case Flush =>
      flush()
  }

  override def postStop(): Unit = {
    flush()
    log.info("HBaseActor postStop, kick off `flush()` after Stop")
  }


  private def flush(): Unit = {
    log.info("flush memory data")

    val l = new util.ArrayList[HBaseRow]
    record.drainTo(l)

    if (l.size() > 0) {
      acc ! Num(num.intValue())
      num.set(0)
      HBaseUtil.saveRows(hbaseConf.zooQuorum, hbaseConf.table, l.toSeq)
    }
    log.info(s"save hbase row into HBase, size: ${l.size()}")
  }
}


case object StartHBase

case object HBaseStarted

case class SaveToHBase(embed: JcyEmbed)

case object SaveHBaseDone

case object Flush



