package com.hawker.order.entity

import java.sql.Timestamp

import org.squeryl.annotations.Column

/**
  * @author mingjiang.ji on 2017/11/16
  */
case class JcyOrder(id: java.lang.Long,
                    pid: String,
                    @Column("order_no") orderNo: String,
                    @Column("user_no") userNo: String,
                    @Column("pay_user_no") payUserNo: String,
                    @Column("merchnat_user_no") merchnatUserNo: String,
                    @Column("merchant_user_name") merchantUserName: String,
                    @Column("merchnat_user_logo") merchnatUserLogo: String,
                    amount: Double,
                    @Column("order_amount") orderAmount: Double,
                    @Column("del_flag") delFlag: Integer,
                    platform: Integer,
                    @Column("order_status") orderStatus: Integer,
                    @Column("order_type") orderType: Integer,
                    @Column("order_desc") orderDesc: String,
                    @Column("pay_no") payNo: String,
                    @Column("pay_status") payStatus: Integer,
                    @Column("pay_type") payType: String,
                    @Column("pay_time") payTime: Timestamp,
                    @Column("sent_status") sentStatus: Integer,
                    @Column("sent_time") sentTime: Timestamp,
                    @Column("receive_status") receiveStatus: Integer,
                    @Column("receive_time") receiveTime: Timestamp,
                    @Column("goods_amount") goodsAmount: Double,
                    @Column("freight_amount") freightAmount: Double,
                    @Column("packing_amount") packingAmount: Double,
                    @Column("date_created") dateCreated: Timestamp,
                    @Column("last_updated") lastUpdated: Timestamp,
                    @Column("proxy_pay_no") proxyPayNo: String,
                    @Column("proxy_pay_status") proxyPayStatus: Integer,
                    @Column("merchnat_tel") merchnatTel: String,
                    @Column("refund_amount") refundAmount: Double,
                    @Column("event_type") eventType: String,
                    @Column("timestamp") timeStamp: Timestamp)

//  extends DbObject
