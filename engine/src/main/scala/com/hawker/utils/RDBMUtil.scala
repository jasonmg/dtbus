package com.hawker.utils

import org.apache.spark.sql.{DataFrame, SaveMode}

/**
  * @author mingjiang.ji on 2017/9/27
  */
object RDBMUtil {

  def saveJDBC(jdbcDF: DataFrame, table: String, conf: MysqlConfig, saveMode: SaveMode = SaveMode.Overwrite): Unit = {
    // Saving data to a JDBC source
    jdbcDF.write.mode(saveMode)
      .format("jdbc")
      .option("url", conf.url)
      .option("driver", conf.driver)
      .option("dbtable", table)
      .option("user", conf.user)
      .option("password", conf.password)
      .save()
  }

}

case class MysqlConfig(host: String, db: String, user: String, password: String, port: String = "3306") {
  val url: String = s"jdbc:mysql://$host:$port/$db"
  val batchUrl: String = url + "?rewriteBatchedStatements=true"
  val driver: String = "com.mysql.jdbc.Driver"
}
