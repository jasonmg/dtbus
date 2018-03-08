package com.hawker.order

import java.util

import com.google.gson.reflect.TypeToken
import com.hawker.core.{CanalCommonTransfer, CommonTransfer, KStreamConfig, ToMysql}
import com.hawker.order.entity._
import com.hawker.utils.ConfigUtil.{config, _}
import org.apache.commons.lang3.StringUtils
import org.apache.kafka.streams.kstream.{KStream, Predicate, ValueMapper}
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Table

import scala.collection.JavaConversions._

/**
  * @author mingjiang.ji on 2017/11/17
  */
class JcyOrderSnapShotTransfer(config: KStreamConfig) extends CanalCommonTransfer[JcyOrderSnapShot](config){

  def addressSnapshot(ks: KStream[String, JcyOrderSnapShot]): KStream[String, AddressSnapShot] = {
    val ks1 = ks.filter(new Predicate[String, JcyOrderSnapShot]() {
      override def test(key: String, value: JcyOrderSnapShot): Boolean = {
        StringUtils.isNotBlank(value.addressSnapshot) &&
          value.addressSnapshot != "null" &&
          value.addressSnapshot != "{}"
      }
    }).mapValues[AddressSnapShot](new ValueMapper[JcyOrderSnapShot, AddressSnapShot]() {
      override def apply(snap: JcyOrderSnapShot): AddressSnapShot = {
        val gs = gson.fromJson(snap.addressSnapshot, classOf[AddressSnapShot])
        gs.copy(orderNo = snap.orderNo, id = snap.id)
      }
    })
    ks1.print()

    ks1
  }

  def goodsSnapshot(ks: KStream[String, JcyOrderSnapShot]): KStream[String, Seq[GoodsSnapShot]] = {
    val ks1 = ks.filter(new Predicate[String, JcyOrderSnapShot]() {
      override def test(key: String, value: JcyOrderSnapShot): Boolean = {
        StringUtils.isNotBlank(value.goodsSnapshot) &&
          value.goodsSnapshot != "null" &&
          value.goodsSnapshot != "[]"
      }
    }).mapValues[Seq[OrderGoodsSnapShot]](new ValueMapper[JcyOrderSnapShot, Seq[OrderGoodsSnapShot]]() {
      override def apply(snap: JcyOrderSnapShot): Seq[OrderGoodsSnapShot] = {

        val gs = gson.fromJson[util.ArrayList[OrderGoodsSnapShot]](snap.goodsSnapshot,
          new TypeToken[util.ArrayList[OrderGoodsSnapShot]]() {}.getType)

        gs.toSeq.map(s => s.copy(id = snap.id,
          orderNo = snap.orderNo,
          dateCreated = snap.dateCreated,
          lastUpdated = snap.lastUpdated,
          eventType = snap.eventType,
          timeStamp = snap.timeStamp))
      }
    })

    val ks2 = ks1.mapValues[Seq[GoodsSnapShot]](new ValueMapper[Seq[OrderGoodsSnapShot], Seq[GoodsSnapShot]]() {
      override def apply(snap: Seq[OrderGoodsSnapShot]): Seq[GoodsSnapShot] = {
        snap map GoodsSnapShotMap.toGoodsSnapShot
      }
    })

    ks1.print()
    ks2.print()

    ks2
  }

  override val monitorKey = Seq("addressSnapshot", "goodsSnapshot")

  override def postProcess(ks: KStream[String, JcyOrderSnapShot]): Unit = {

    val addrks = addressSnapshot(ks)
    val goodsKs = goodsSnapshot(ks)

    val addrTable: Table[AddressSnapShot] = table[AddressSnapShot]("jcy_order_address_snapshot")
    val goodsTable: Table[GoodsSnapShot] = table[GoodsSnapShot]("jcy_order_goods_snapshot")

    insert(addrks, addrTable)
    insertSeq(goodsKs, goodsTable)
  }


}

object JcyOrderSnapShotTransfer extends App {
  val p = config.getConfig("jcyOrderSnapShot").convert[KStreamConfig]
  val jt = new JcyOrderSnapShotTransfer(p)
  jt.start()
}
