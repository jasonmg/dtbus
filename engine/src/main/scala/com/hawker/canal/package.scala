package com.hawker

/**
  * @author mingjiang.ji on 2017/9/25
  */
package object canal {

  case class KafkaMessage(sourceType: String,
                          port: Int,
                          dbName: String,
                          ip: String,
                          afterRow: java.util.Map[String, String],
                          beforeRow: java.util.Map[String, String],
                          eventType: String,
                          tableName: String,
                          executeTime: Long)

}
