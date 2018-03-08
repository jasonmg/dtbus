package com.hawker.utils


import scala.reflect.ClassTag
import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe._

/**
  * @author mingjiang.ji on 2017/11/24
  *         http://docs.scala-lang.org/overviews/reflection/environment-universes-mirrors.html
  */
object Reflect4SUtil {

  private lazy val rm = ru.runtimeMirror(getClass.getClassLoader)

  /**
    * convert instance of case class to map,
    * for instance:
    * {{
    *   case class Foo(name: String, age: Int, address: String)
    *   val f = Foo("jason", 30, "hz")
    *
    *   Reflect4SUtil.instance2Map(f) -->  Map("name" -> "jason", "age" -> 30, "address" -> "hz")
    * }}
    *
    * there is also have Java version, see [[ReflectUtil]]
    *
    * @param o
    * @tparam T
    * @return
    */
  def instance2Map[T: ru.TypeTag : ClassTag](o: T): Map[String, Any] = {

    val tpe = typeOf[T]

    val im = rm.reflect(o)
    val ctor = tpe.decl(ru.termNames.CONSTRUCTOR).asMethod

    val res = ctor.paramLists.flatten.map((param: Symbol) => {
      val paramName = param.name.toString

      val field = tpe.decl(ru.TermName(paramName)).asTerm.accessed.asTerm
      val fmX = im.reflectField(field)

      (paramName, fmX.get)
    }).toMap

    res
  }

}
