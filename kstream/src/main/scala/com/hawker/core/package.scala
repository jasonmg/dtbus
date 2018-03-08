package com.hawker

import java.util.UUID

/**
  * @author mingjiang.ji on 2017/12/4
  */
package object core {

  case class DBConfig(driver: String, url: String, table: String, user: String, password: String)

  case class HBaseConfig(zooQuorum: String,
                       table: String,
                       columnFamily: String,
                       maxVersions: Option[Int]) {

    require(zooQuorum.nonEmpty, "HBase zookeeper address should be provide first")
    require(table.nonEmpty, "Table name should be provide first")
    require(columnFamily.nonEmpty, "ColumnFamily should be provide first")
  }


  case class KStreamConfig(applicationId: String,
                           topic: String,
                           brokers: String,
                           fromBeginning: Option[Boolean],
                           groupId: Option[String],
                           clientId: Option[String],
                           print: Option[Boolean],
                           dryRun: Option[Boolean], // do not save to database
                           dBConfig: Option[DBConfig],
                           hbaseConfig: Option[HBaseConfig]) {

    private val randomStr = applicationId + "-" + UUID.randomUUID().toString

    val groupIdStr: String = groupId.getOrElse(randomStr)
    val clientIdStr: String = clientId.getOrElse(randomStr)
  }

}
