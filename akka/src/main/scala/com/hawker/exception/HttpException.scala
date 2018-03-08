package com.hawker.exception

/**
  * @author mingjiang.ji on 2017/10/26
  */
class HttpException(val errorCode: Int, val throwable: Throwable) extends RuntimeException
