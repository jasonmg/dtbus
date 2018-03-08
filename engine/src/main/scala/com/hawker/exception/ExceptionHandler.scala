package com.hawker.exception

import com.hawker.utils.Log

/**
  * @author mingjiang.ji on 2017/9/4
  */
trait ExceptionHandler extends Serializable {
  def handler(line: String, ex: Throwable): Unit
}

class ExceptionHandlerLogging(logger: Log) extends ExceptionHandler {
  override def handler(line: String, ex: Throwable): Unit =
    logger.log.error(s"Line: ${line.toString} \n Err: ${ex.getMessage}")
}



