package com.hawker.embed

import java.util
import java.util.concurrent.TimeUnit
import java.util.{List => JList}

import com.hawker.core.{CommonTransfer, KStreamConfig}
import com.hawker.entity.embed.JcyEmbed
import com.hawker.utils.ConfigUtil.{config, _}
import com.hawker.utils.kafka.serializer.{AvroListSerdes, AvroSerdes}
import com.hawker.utils.{HBaseUtil, ReflectUtil}
import org.apache.avro.Schema
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream._

import scala.collection.JavaConversions._

/**
  * @author mingjiang.ji on 2017/11/29
  */
class JcyEmbedTransfer(config: KStreamConfig) extends CommonTransfer[JcyEmbed](config) {

  require(config.hbaseConfig.isDefined, "hbaseConfig should be defined first")

  val hbaseConfig = config.hbaseConfig.get

  class JcyEmbedSerdes extends AvroSerdes[JcyEmbed] {
    override def getAvroSchema: Schema = new JcyEmbed().getSchema
  }

  class SeqJcyEmbedSerdes extends AvroListSerdes[JcyEmbed] {
    override def getAvroSchema: Schema = new JcyEmbed().getSchema
  }

  // 创建表
  HBaseUtil.createTable(hbaseConfig.zooQuorum, hbaseConfig.table, hbaseConfig.columnFamily)

  override def streamTransfer(sb: KStreamBuilder): KStream[String, JcyEmbed] = {
    sb.stream(Serdes.String, new JcyEmbedSerdes, config.topic)
  }

  override def postProcess(ks: KStream[String, JcyEmbed]): Unit = {


    val groupStream = ks.groupBy(new KeyValueMapper[String, JcyEmbed, String]() {
      override def apply(key: String, value: JcyEmbed) = value.getSourceApp
    }, Serdes.String(), new JcyEmbedSerdes)

    val res = groupStream
      .aggregate(new Initializer[JList[JcyEmbed]]() {
        override def apply() = new util.ArrayList[JcyEmbed]()
      },
        new Aggregator[String, JcyEmbed, JList[JcyEmbed]]() {
          override def apply(key: String, value: JcyEmbed, aggregate: JList[JcyEmbed]) = {
            aggregate.add(value)
            aggregate
          }
        },
        TimeWindows.of(TimeUnit.SECONDS.toMillis(10)),
        new SeqJcyEmbedSerdes)

    res.print()


    res.toStream.foreach(new ForeachAction[Windowed[String], JList[JcyEmbed]]() {
      override def apply(key: Windowed[String], value: JList[JcyEmbed]) = {

        System.out.println(s"value size is: ${value.size()}")

        val res = value.map(data => {

          val instanceMap = ReflectUtil.instance2Map(data).toMap.filterKeys(_ != "params")
          val paramMap = gson.fromJson(data.getParams, classOf[java.util.Map[String, Object]]).toMap
            .map { case (k, v) => "params_" + k -> v }

          (data.toString.hashCode + "", hbaseConfig.columnFamily, instanceMap ++ paramMap)
        })

        HBaseUtil.saveRows(hbaseConfig.zooQuorum, hbaseConfig.table, res)
      }
    })

  }
}


object JcyEmbedTransfer extends App {
  val p = config.getConfig("jcyEmbed").convert[KStreamConfig]
  val jt = new JcyEmbedTransfer(p)
  jt.start()
}

