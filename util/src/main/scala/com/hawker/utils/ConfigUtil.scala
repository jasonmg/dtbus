package com.hawker.utils

import com.typesafe.config.{Config, ConfigException, ConfigFactory}

import scala.reflect.ClassTag
import scala.reflect.runtime.{universe => ru}
import scala.reflect.runtime.universe._
import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

/**
  * @author mingjiang.ji on 2017/8/30
  */
object ConfigUtil extends App {
  lazy val env = System.getProperty("appEnv", "")

  lazy val appFile = {
    //    if (env == "dev") "application-dev"
    //    else if (env == "pre") "application-pre"
    //    else if (env == "prod") "application-prod"
    if (env == "") "application" else "application-" + env
  }

  lazy val config = ConfigFactory.load(appFile)

  implicit def configEnhance(cfg: Config): ConfigEnhance = new ConfigEnhance(cfg)
}


class ConfigEnhance(cfg: Config) {

  import ConfigUtil._

  lazy val rm = ru.runtimeMirror(getClass.getClassLoader)

  def convert[T: ru.TypeTag : ClassTag]: T = as(typeOf[T])

  def as[T: ru.TypeTag : ClassTag](tpe: Type): T = {
    val classT = tpe.typeSymbol.asClass
    val cm = rm.reflectClass(classT)
    val ctor = tpe.decl(ru.termNames.CONSTRUCTOR).asMethod
    val ctorm = cm.reflectConstructor(ctor)

    val constructorArgs = ctor.paramLists.flatten.map((param: Symbol) => {
      val paramName = param.name.toString
      val tpe = param.typeSignature

      if (param.typeSignature <:< typeOf[Option[_]]) {
        val v = configValue(paramName, tpe.dealias.typeArgs.head)
        Option(v)
      } else {
        configValue(paramName, tpe)
      }
    })

    val res = ctorm(constructorArgs: _*)
    res.asInstanceOf[T]
  }


  private def configValue(name: String, tpe: Type): Any = {
    if (tpe <:< typeOf[String])
      configSaveGet(name, cfg.getString(name))
    else if (tpe <:< typeOf[Boolean])
      configSaveGet(name, cfg.getBoolean(name))
    else if (tpe <:< typeOf[Int])
      configSaveGet(name, cfg.getInt(name))
    else if (tpe <:< typeOf[Byte])
      configSaveGet(name, cfg.getBytes(name))
    else if (tpe <:< typeOf[Float] || tpe <:< typeOf[Double])
      configSaveGet(name, cfg.getDouble(name))
    else if (tpe <:< typeOf[List[String]])
      configSaveGet(name, cfg.getStringList(name).toList)
    else if (tpe <:< typeOf[List[Boolean]])
      configSaveGet(name, cfg.getBooleanList(name).toList)
    else if (tpe <:< typeOf[List[Int]])
      configSaveGet(name, cfg.getIntList(name).toList)
    else if (tpe <:< typeOf[List[Byte]])
      configSaveGet(name, cfg.getBytesList(name).toList)
    else if (tpe <:< typeOf[List[Float]] || tpe <:< typeOf[List[Double]])
      configSaveGet(name, cfg.getDoubleList(name).toList)
    else if (tpe <:< typeOf[List[_]]) {
      if (cfg.hasPath(name)) cfg.getConfigList(name).toSeq.map(_.as(tpe.dealias.typeArgs.head))
      else List.empty
    } else {
      if (cfg.hasPath(name)) cfg.getConfig(name).as(tpe) else null
    }
  }


  def configSaveGet(name: String, get: => Any) = {
    Try(get) match {
      case Success(s) => s
      case Failure(e) =>
        e match {
          case _: ConfigException.Missing => null
          case _ => throw new IllegalArgumentException("error happening when read config: " + e.getMessage)
        }
    }
  }

  private def getElementSymbol[T: ru.TypeTag](tpe: Type): Seq[Symbol] = {
    //    val theType = ru.typeTag[T].tpe
    val theType = tpe
    val res = for (
      f <- theType.decls if !f.isMethod
    ) yield f
    res.toSeq
  }

  private def getElementNameType[T: ru.TypeTag](tpe: Type): List[(String, Type)] = {
    getElementSymbol(tpe) map { s => (s.name.decodedName.toString.trim, s.typeSignature) } toList
  }

}
