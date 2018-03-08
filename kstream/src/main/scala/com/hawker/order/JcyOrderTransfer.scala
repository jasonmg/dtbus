package com.hawker.order

import com.hawker.core
import com.hawker.core.{CanalCommonTransfer, KStreamConfig}
import com.hawker.order.entity.JcyOrder
import com.hawker.utils.ConfigUtil._
import com.hawker.utils.Log
import org.apache.kafka.streams.kstream.KStream
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Table

/**
  * @author mingjiang.ji on 2017/11/16
  */
class JcyOrderTransfer(config: KStreamConfig) extends CanalCommonTransfer[JcyOrder](config) with Log {
  require(config.dBConfig.isDefined, "dbConfig should be defined first")

  override val dbConfig: core.DBConfig = config.dBConfig.get

  val table: Table[JcyOrder] = table[JcyOrder](dbConfig.table)

  override def postProcess(ks: KStream[String, JcyOrder]): Unit = {
    insert(ks, table)
  }


}

object JcyOrderTransfer extends App {
  val p = config.getConfig("jcyOrder").convert[KStreamConfig]
  val jt = new JcyOrderTransfer(p)
  jt.start()
}
