package com.hawker.recommend


import com.hawker.utils.ConfigUtil.{config, _}

import scala.collection.JavaConversions._


/**
  * 将给定VBID的电影进行全局计算，计算得到除此VBID之外的每个电影对应的分数，按照分数从大到小的顺序推荐24部
  * http://doc.jc/pages/viewpage.action?pageId=7475725
  */
object YouXiangRecommendAlgorithm {

  private val l = config.getConfig("categoryLabel").convert[CategoryLabel]

  def main(args: Array[String]): Unit ={
    val ll = config.getConfig("categoryLabel").convert[CategoryLabel]
    println(ll)
  }

  val MOVIE_LABEL = l.movie // 电影
  val TV_LABEL = l.tv // 电视剧
  val SHOW_LABEL = l.show // 综艺
  val ENTERTAINMENT_LABEL = l.entertainment // 娱乐
  val CHILD_LABEL = l.child // 少儿
  val MUSIC_LABEL = l.music // 音乐
  val NEWS_LABEL = l.news // 资讯
  val NEW_TOWN_LABEL = l.newTown // 新城镇

  val GAME_LABEL = l.game // 游戏
  val ORIGIN_LABEL = l.origin // 原创
  val SPORT_LABEL = l.sport // 体育


  val weightMatrix = Map(
    MOVIE_LABEL -> Map("mark" -> 2, "style" -> 0, "director" -> 1, "actor" -> 2, "area" -> 1, "year" -> 0, "keyword" -> 4),
    TV_LABEL -> Map("mark" -> 2, "style" -> 0, "director" -> 1, "actor" -> 2, "area" -> 1, "year" -> 0, "keyword" -> 4),
    SHOW_LABEL -> Map("mark" -> 3, "style" -> 0, "director" -> 0, "actor" -> 1, "area" -> 1, "year" -> 1, "keyword" -> 4),
    ENTERTAINMENT_LABEL -> Map("mark" -> 2, "style" -> 0, "director" -> 0, "actor" -> 2, "area" -> 1, "year" -> 1, "keyword" -> 4),
    MUSIC_LABEL -> Map("mark" -> 2, "style" -> 0, "director" -> 1, "actor" -> 2, "area" -> 1, "year" -> 0, "keyword" -> 4),
    NEWS_LABEL -> Map("mark" -> 3, "style" -> 0, "director" -> 0, "actor" -> 0, "area" -> 1, "year" -> 2, "keyword" -> 4),
    CHILD_LABEL -> Map("mark" -> 3, "style" -> 0, "director" -> 0, "actor" -> 1, "area" -> 1, "year" -> 1, "keyword" -> 4),
    NEW_TOWN_LABEL -> Map("mark" -> 3, "style" -> 0, "director" -> 0, "actor" -> 0, "area" -> 1, "year" -> 2, "keyword" -> 4),
    GAME_LABEL -> Map("mark" -> 3, "style" -> 0, "director" -> 0, "actor" -> 1, "area" -> 1, "year" -> 0, "keyword" -> 5),
    ORIGIN_LABEL -> Map("mark" -> 3, "style" -> 0, "director" -> 0, "actor" -> 0, "area" -> 1, "year" -> 0, "keyword" -> 6),
    SPORT_LABEL -> Map("mark" -> 3, "style" -> 0, "director" -> 0, "actor" -> 0, "area" -> 1, "year" -> 1, "keyword" -> 5)
  )

  def square(x: Int): Double = x * x

  def compareVideo(a: YouXiangVideoInfo, b: YouXiangVideoInfo): YouXiangVideoRecommend = {

    val category = a.categorySet.head
    if (!weightMatrix.containsKey(category)) {
      YouXiangVideoRecommend(a.videoBaseId, b.videoBaseId, 0.0, category)
    } else {
      val labelMatrix = weightMatrix(category).withDefaultValue(0)

      val actorMatrix = labelMatrix("actor")
      val styleMatrix = labelMatrix("style")
      val areaMatrix = labelMatrix("area")
      val directorMatrix = labelMatrix("director")
      val signMatrix = labelMatrix("mark")
      val yearMatrix = labelMatrix("year")
      val keyWordMatrix = labelMatrix("keyword")

      val denominator = square(signMatrix) + square(actorMatrix) + square(styleMatrix) +
        square(areaMatrix) + square(directorMatrix) + square(yearMatrix) + square(keyWordMatrix)

      // 标记比较
      val signScore = if (b.sign.nonEmpty) 1.0 * signMatrix else 0.0

      // 主演比较
      val actorNum = if (a.actorList.nonEmpty) a.actorList.length else 1.0
      val actorScore = compare(a.actorList, b.actorList) * actorMatrix / actorNum

      // 类型比较
      val styleNum = if (a.styleList.nonEmpty) a.styleList.length else 1.0
      val styleScore = compare(a.styleList, b.styleList) * styleMatrix / styleNum

      // 地区比较
      val areaScore = if (compare(a.areaList, b.areaList) > 0) 1.0 * areaMatrix else 0.0

      // 导演比较
      val directorScore = if (compare(a.directorList, b.directorList) > 0) 1.0 * directorMatrix else 0.0

      // 年代比较
      val yearScore = if (compare(a.year, b.year) > 0) 1.0 * yearMatrix else 0.0

      // 关键字比较
      val keyWordNum = if (a.keyWordList.nonEmpty) a.keyWordList.length else 1.0
      val ketWordScore = compare(a.keyWordList, b.keyWordList) * keyWordMatrix / keyWordNum

      val score = (signScore + actorScore + styleScore + areaScore + directorScore + yearScore + ketWordScore) / math.sqrt(denominator)

      YouXiangVideoRecommend(a.videoBaseId, b.videoBaseId, score, category)
    }
  }

  def startRealTimeCompute(videoInfo: YouXiangVideoInfo, allRecord: Seq[YouXiangVideoInfo]): Seq[YouXiangVideoRecommend] = {
    //    val result = new ConcurrentHashMap[YouXiangVideoRecommend, Unit]()
    //    allRecord.foreach(b => result.put(compareVideo(videoInfo, b), Unit))
    //    result.keySet().toSet

    allRecord.map(b => compareVideo(videoInfo, b))
  }

  def compare(a: Any, b: Any): Int = {

    // 列表数据比较，返回数据重合数
    def compareList(list1: Array[_], list2: Array[_]): Int = list1.intersect(list2).length

    if (a.isInstanceOf[Option[_]] && b.isInstanceOf[Option[_]]) {
      val ao = a.asInstanceOf[Option[_]]
      val bo = b.asInstanceOf[Option[_]]

      if (ao.isDefined && bo.isDefined) compare(ao.get, bo.get) else 0
    } else if (a.isInstanceOf[String] && b.isInstanceOf[String])
      if (a == b) 1 else 0
    else if (a.isInstanceOf[Array[_]] && b.isInstanceOf[Array[_]]) {
      compareList(a.asInstanceOf[Array[_]], b.asInstanceOf[Array[_]])
    } else 0

  }
}

case class CategoryLabel(movie: String, tv: String, show: String, child: String, music: String,
                         entertainment: String, news: String, newTown: String,
                         game: String, origin: String, sport: String)

