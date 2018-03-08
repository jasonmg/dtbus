package com.hawker.core

import com.hawker.exception.ExceptionHandler
import org.apache.spark.streaming.dstream.DStream

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/**
  * @author mingjiang.ji on 2017/9/6
  *         reference from https://www.nicolaferraro.me/2016/02/18/exception-handling-in-apache-spark/
  */
class TryDStreamFunctions[T](dStream: DStream[T]) extends AnyRef with Serializable {

  def tryMap[U](fn: T => U)(implicit exh: ExceptionHandler, ct: ClassTag[U]): DStream[U] = {
    dStream.flatMap(ele => {

      val re = Try(fn(ele)) match {
        case fail@Failure(ex) =>
          exh.handler(ele.toString, ex)
          fail
        case suc@_ => suc
      }
      re.toOption
    })
  }


  def tryFlatMap[U](fn: T => TraversableOnce[U])(implicit exh: ExceptionHandler, ct: ClassTag[U]): DStream[U] = {
    dStream.flatMap(ele => Try(fn(ele)) match {
      case Failure(ex) =>
        exh.handler(ele.toString, ex)
        Nil
      case Success(s) => s
    })
  }

}
