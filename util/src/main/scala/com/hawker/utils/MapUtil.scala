package com.hawker.utils

import scala.reflect.ClassTag
import scala.reflect.runtime.{universe => ru}
import scala.reflect._
import scala.reflect.runtime.universe._


/**
  * @author mingjiang.ji on 2017/9/26
  */
object MapUtil {

  implicit def map2RichMap(m: Map[String, _]): RichMap = new RichMap(m)

  class RichMap(m: Map[String, _]) {
    def convert[T: ru.TypeTag : ClassTag](camelCaseKey: Boolean = true): T = MapUtil.fromMap[T](m, camelCaseKey)

    // convert key to camelCaseStyle
    def camelCaseKey = m.map { case (k, v) => StringUtil.toCamelCase(k) -> v }
  }

  def fromMap[T: ru.TypeTag : ClassTag](m: Map[String, _], camelCaseKey: Boolean): T = {

    val m1 = if (camelCaseKey) {
      m.map { case (k, v) => StringUtil.toCamelCase(k) -> v }
    } else m

    val rm = ru.runtimeMirror(classTag[T].runtimeClass.getClassLoader)
    val classTest = typeOf[T].typeSymbol.asClass
    val classMirror = rm.reflectClass(classTest)
    val constructor = typeOf[T].decl(ru.termNames.CONSTRUCTOR).asMethod
    val constructorMirror = classMirror.reflectConstructor(constructor)

    val constructorArgs = constructor.paramLists.flatten.map((param: Symbol) => {
      val paramName = param.name.toString

      if (param.typeSignature <:< typeOf[Option[Any]])
        m1.get(paramName)
      else {
        m1.getOrElse(paramName,
          throw new IllegalArgumentException("Map is missing required parameter named " + paramName + ",\n" +
            "map is: "+ m1))
      }
    })

    constructorMirror(constructorArgs: _*).asInstanceOf[T]
  }
}

// ========================== scala macro =======================================================
// http://blog.echo.sh/2013/11/04/exploring-scala-macros-map-to-case-class-conversion.html

trait Mappable[T] {
  def toMap(t: T): Map[String, Any]

  def fromMap(map: Map[String, Any]): T
}


object Mappable {

  import scala.reflect.macros.blackbox.Context
  import scala.language.experimental.macros

  implicit def materializeMappable[T]: Mappable[T] = macro materializeMappableImpl[T]

  def materializeMappableImpl[T: c.WeakTypeTag](c: Context): c.Expr[Mappable[T]] = {
    import c.universe._

    val tpe = weakTypeOf[T]
    val companion = tpe.typeSymbol.companion

    val fields = tpe.decls.collectFirst {
      case m: MethodSymbol if m.isPrimaryConstructor => m
    }.get.paramLists.head

    val (toMapParams, fromMapParams) = fields.map { field =>
      val name = field.name
      val tname = name.toTermName
      val decoded = name.decodedName.toString
      val returnType = tpe.decl(name).typeSignature

      (q"$decoded -> t.$tname", q"map($decoded).asInstanceOf[$returnType]")
    }.unzip

    c.Expr[Mappable[T]] {
      q"""
      new Mappable[$tpe] {
        def toMap(t: $tpe): Map[String, Any] = Map(..$toMapParams)
        def fromMap(map: Map[String, Any]): $tpe = $companion(..$fromMapParams)
      }
    """
    }
  }
}
