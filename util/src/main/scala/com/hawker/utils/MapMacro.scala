package com.hawker.utils

/**
  * @author mingjiang.ji on 2017/11/16
  */
object MapMacro {

  import Mappable.materializeMappable

  // ==============  scala macro ==============
  //  How to use it:
  //  import MapMacro._
  //  case class Foo(name: String, age: Int)
  //  val m = Map("name" -> "jason", "age" -> 1)
  //  val foo = materialize[Foo](m)
  //  val map = mapify(foo)


  // case class to map
  def mapify[T: Mappable](t: T) = implicitly[Mappable[T]].toMap(t)

  // map to case class
  def materialize[T: Mappable](map: Map[String, Any]) = implicitly[Mappable[T]].fromMap(map)
}
