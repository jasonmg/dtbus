package com.hawker.order.entity

import java.sql.Timestamp

import org.apache.commons.lang3.StringUtils
import org.squeryl.annotations.Column

/**
  * @author mingjiang.ji on 2017/11/17
  */
case class JcyOrderSnapShot(id: java.lang.Long,
                            orderNo: String,
                            goodsSnapshot: String,
                            addressSnapshot: String,
                            dateCreated: Timestamp,
                            lastUpdated: Timestamp,
                            eventType: String,
                            timeStamp: Timestamp)


// ========================================================================


case class AddressSnapShot(id: java.lang.Long,
                           @Column("order_no") orderNo: String,
                           company: String,
                           @Column("date_created") dateCreated: Timestamp,
                           depart: String,
                           floor: String,
                           member: String,
                           name: String,
                           phone: String,
                           @Column("user_no") userNo: String,
                           area: String,
                           city: String,
                           detail: String,
                           mobile: String,
                           province: String,
                           @Column("user_name") userName: String)

// ========================================================================

case class OrderGoodsSnapShot(id: java.lang.Long,
                              orderNo: String,
                              goodsNo: String,
                              img: String,
                              name: String,
                              num: Integer,
                              price: Double,
                              scale: String,
                              snapshot: SnapShot,
                              dateCreated: Timestamp,
                              lastUpdated: Timestamp,
                              eventType: String,
                              timeStamp: Timestamp)


case class SnapShot(amount: Double,
                    count: Integer,
                    goodsId: String,
                    orderType: Integer,
                    platform: Integer,
                    userNo: String,
                    bookDates: java.util.ArrayList[String],
                    cost: Double,
                    defaultFlag: Integer,
                    jcbNum: Double,
                    rechargeId: String)


case class GoodsSnapShot(id: java.lang.Long,
                         @Column("order_no") orderNo: String,
                         @Column("goods_no") goodsNo: String,
                         img: String,
                         name: String,
                         num: Integer,
                         price: Double,
                         scale: String,
                         @Column("snapshot_amount") snapshotAmount: Double,
                         @Column("snapshot_count") snapshotCount: Integer,
                         @Column("snapshot_goods_id") snapshotGoodsId: String,
                         @Column("snapshot_order_type") snapshotOrderType: Integer,
                         @Column("snapshot_platform") snapshotPlatform: Integer,
                         @Column("snapshot_user_no") snapshotUserNo: String,
                         @Column("snapshot_book_dates") snapshotBookDates: String,
                         @Column("snapshot_cost") snapshotCost: Double,
                         @Column("snapshot_default_flag") snapshotDefaultFlag: Integer,
                         @Column("snapshot_jcb_num") snapshotJcbNum: Double,
                         @Column("snapshot_recharge_id") snapshotRechargeId: String,
                         @Column("date_created") dateCreated: Timestamp,
                         @Column("last_updated") lastUpdated: Timestamp,
                         @Column("event_type") eventType: String,
                         @Column("timestamp") timeStamp: Timestamp
                        )


object GoodsSnapShotMap {

  def toGoodsSnapShot(o: OrderGoodsSnapShot): GoodsSnapShot = {
    val s = o.snapshot

    if (s != null) {
      GoodsSnapShot(id = o.id,
        orderNo = o.orderNo,
        goodsNo = o.goodsNo,
        img = o.img,
        name = o.name,
        num = o.num,
        price = o.price,
        scale = o.scale,
        snapshotAmount = s.amount,
        snapshotCount = s.count,
        snapshotGoodsId = s.goodsId,
        snapshotOrderType = s.orderType,
        snapshotPlatform = s.platform,
        snapshotUserNo = s.userNo,
        snapshotBookDates = if (s.bookDates != null && !s.bookDates.isEmpty) s.bookDates.toArray().mkString(",") else "",
        snapshotCost = s.cost,
        snapshotDefaultFlag = s.defaultFlag,
        snapshotJcbNum = s.jcbNum,
        snapshotRechargeId = s.rechargeId,
        dateCreated = o.dateCreated,
        lastUpdated = o.lastUpdated,
        eventType = o.eventType,
        timeStamp = o.timeStamp)
    } else {
      GoodsSnapShot(id = o.id,
        orderNo = o.orderNo,
        goodsNo = o.goodsNo,
        img = o.img,
        name = o.name,
        num = o.num,
        price = o.price,
        scale = o.scale,
        snapshotAmount = 0.0,
        snapshotCount = null,
        snapshotGoodsId = null,
        snapshotOrderType = null,
        snapshotPlatform = null,
        snapshotUserNo = null,
        snapshotBookDates = null,
        snapshotCost = 0.0,
        snapshotDefaultFlag = null,
        snapshotJcbNum = 0.0,
        snapshotRechargeId = null,
        dateCreated = o.dateCreated,
        lastUpdated = o.lastUpdated,
        eventType = o.eventType,
        timeStamp = o.timeStamp)
    }


  }
}
