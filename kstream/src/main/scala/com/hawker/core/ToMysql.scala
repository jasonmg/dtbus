package com.hawker.core

import com.hawker.utils.SquerylUtil
import org.apache.kafka.streams.kstream.{ForeachAction, KStream}
import org.squeryl.{KeyedEntityDef, Schema, Table}

/**
  * import org.squeryl.PrimitiveTypeMode._    is mandatory
  *
  * @author mingjiang.ji on 2017/11/29
  */
trait ToMysql extends Schema {

  protected def dbConfig: DBConfig = ???

  protected lazy val squerylDb = new SquerylUtil(dbConfig.driver, dbConfig.url, dbConfig.user, dbConfig.password)

  //  /**
  //    * Squeryl table类型, 用于插入数据库
  //    * T: case class 对应于数据库中表的映射类
  //    */
  //  def table[T](): Table[T] = ???
  //
  //  protected def insert[A](ks: KStream[String, A]): Unit = insert(ks, table())

  protected def insert[A](ks: KStream[String, A], table: Table[A]): Unit = {
    ks.foreach(new ForeachAction[String, A]() {
      override def apply(key: String, value: A) = {
        squerylDb.insert(table, value)
      }
    })
  }

  protected def insertSeq[A](ks: KStream[String, Seq[A]], table: Table[A]): Unit = {
    ks.foreach(new ForeachAction[String, Seq[A]]() {
      override def apply(key: String, value: Seq[A]) = {
        squerylDb.insert(table, value)
      }
    })
  }

  protected def insertOrUpdate[A](ks: KStream[String, A], table: Table[A])(implicit ked: KeyedEntityDef[A, _]): Unit = {
    ks.foreach(new ForeachAction[String, A]() {
      override def apply(key: String, value: A) = {
        squerylDb.insertOrUpdate(table, value)
      }
    })
  }
}
