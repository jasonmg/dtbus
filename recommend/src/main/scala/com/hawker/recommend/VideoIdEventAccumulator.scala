package com.hawker.recommend

import com.hawker.utils.Log
import org.apache.spark.util.AccumulatorV2

/**
  * @author mingjiang.ji on 2017/9/27
  */
class VideoIdEventAccumulator extends AccumulatorV2[VideoIdEvent, VideoIdEvent] with Log {
  private var _videoIdEvent: VideoIdEvent = _

  override def isZero: Boolean = _videoIdEvent == null

  override def copy(): AccumulatorV2[VideoIdEvent, VideoIdEvent] = {
    val newAcc = new VideoIdEventAccumulator()
    newAcc._videoIdEvent = this._videoIdEvent
    newAcc
  }

  override def reset(): Unit = _videoIdEvent = null

  override def add(v: VideoIdEvent): Unit = {
    log.info(s"添加到累加器: $v")
    _videoIdEvent = v
  }

  override def merge(other: AccumulatorV2[VideoIdEvent, VideoIdEvent]): Unit = {
    other match {
      case o: VideoIdEventAccumulator =>
        _videoIdEvent = o._videoIdEvent
      case _ =>
        throw new UnsupportedOperationException(
          s"Cannot merge ${this.getClass.getName} with ${other.getClass.getName}")
    }
  }

  override def value: VideoIdEvent = _videoIdEvent
}


case class VideoIdEvent(videoBaseId: String, eventType: String)
