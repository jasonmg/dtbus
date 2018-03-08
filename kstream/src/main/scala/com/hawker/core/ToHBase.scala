package com.hawker.core

import com.hawker.utils.{HBaseUtil, ReflectUtil}
import org.apache.kafka.streams.kstream.{ForeachAction, KTable, Windowed}

import scala.collection.JavaConversions._

/**
  * @author mingjiang.ji on 2017/11/29
  */
trait ToHBase {

  val hbaseConfig: HBaseConfig

  def saveToHBase[K, E](ks: KTable[Windowed[K], Seq[E]])(implicit keyed: KeyedObject[E]): Unit = {

    ks.toStream.foreach(new ForeachAction[Windowed[K], Seq[E]]() {
      override def apply(key: Windowed[K], value: Seq[E]) = {

        val res = value.map(data => {
          val instanceMap = ReflectUtil.instance2Map(data).toMap
          (keyed.key(data), hbaseConfig.columnFamily, instanceMap)
        })

        HBaseUtil.saveRows(hbaseConfig.zooQuorum, hbaseConfig.table, res)
      }
    })
  }
}


/**
  * object 存入hbase中rowkey的选取
  */
trait KeyedObject[O] {
  def key(o: O): String
}
