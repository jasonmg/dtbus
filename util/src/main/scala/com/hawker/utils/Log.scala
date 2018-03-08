package com.hawker.utils

import org.slf4j.LoggerFactory


/**
  * @author mingjiang.ji on 2017/9/1
  */
trait Log {
  val log = LoggerFactory.getLogger(this.getClass)
}
