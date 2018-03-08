package com.hawker.http.embed

import akka.actor.Actor
import com.hawker.utils.Log

/**
  * @author mingjiang.ji on 2017/11/29
  */

class AccumulatorActor extends Actor with Log{

  var num = 0

  override def receive = {
    case Num(n) =>
      num += n
      log.info(s"============= accumulator number: $num")
  }


}

case class Num(n: Int)
