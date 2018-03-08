package com.hawker.utils

import scala.util.control.NonFatal

/**
  * @author mingjiang.ji on 2017/9/4
  */
object AutoClose extends Log {

  def using[T <: {def close() : Unit}, O](closeable: T)(fn: T => O): O = {
    def closeQuality(closeable: T): Unit = {
      try {
        closeable.close()
      } catch {
        case NonFatal(exc) => log.error(s"try close resource with exception: $exc")
      }
    }

    try {
      fn(closeable)
    } finally {
      closeQuality(closeable)
    }
  }

}
