package com.hawker.utils

import java.sql.DriverManager

import org.squeryl.PrimitiveTypeMode.inTransaction
import org.squeryl.{KeyedEntityDef, Session, SessionFactory, Table}
import org.squeryl.adapters.MySQLInnoDBAdapter

/**
  * @author mingjiang.ji on 2017/11/17
  */
class SquerylUtil(val driver: String, val url: String, val user: String, val password: String) {

  Class.forName(driver)

  SessionFactory.concreteFactory = Some(() =>
    Session.create(DriverManager.getConnection(url, user, password),
      new MySQLInnoDBAdapter))


  def insert[T](t: Table[T], entity: T): Unit = {
    inTransaction {
      t.insert(entity)
    }
  }

  def insert[T](t: Table[T], entities: Seq[T]): Unit = {
    inTransaction {
      t.insert(entities)
    }
  }

  def update[T](t: Table[T], entity: T)(implicit ked: KeyedEntityDef[T, _]): Unit = {
    inTransaction {
      t.update(entity)
    }
  }

  def update[T](t: Table[T], entities: Seq[T])(implicit ked: KeyedEntityDef[T, _]): Unit = {
    inTransaction {
      t.update(entities)
    }
  }

  def insertOrUpdate[T](t: Table[T], entity: T)(implicit ked: KeyedEntityDef[T, _]): Unit = {
    inTransaction {
      t.insertOrUpdate(entity)
    }
  }

}


