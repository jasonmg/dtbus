package com.hawker.utils

/**
  * @author mingjiang.ji on 2017/10/10
  */
object StringUtil {


  /**
    * 变量命名格式转换  v_video_base -> vVideoBase
    *
    * @param para
    * @return
    */
  def toCamelCase(para: String): String = {
    val sb = new StringBuilder()
    var isUnderScore = false

    for (s <- para) {
      if ('_'.equals(s)) {
        isUnderScore = true
      } else {
        val ss = if (isUnderScore) {
          isUnderScore = false
          s.toUpper
        } else s

        sb.append(ss)
      }
    }

    sb.toString
  }
}

