package com.hawker.http.embed

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.http.scaladsl.server.Route
import com.hawker.http.{JsonSupport, Result1}
import akka.http.scaladsl.server.Directives._
import com.hawker.exception.HttpException
import akka.pattern.ask
import akka.util.Timeout

import scala.util.{Failure, Success}

/**
  * @author mingjiang.ji on 2017/11/23
  */
object JcyEmbedRoute extends JsonSupport {

  implicit val jcyEmbedFormat = new JcyEmbedJsonFormat
  implicit val embedApiFormat = jsonFormat1(EmbedApi)

  implicit val timeout = Timeout(50, TimeUnit.SECONDS)

  def apply(topic: String, service: ActorRef): Route = {
    path("jcyEmbed" / "evtBatch") {
      post {
        entity(as[EmbedApi]) { embed =>

          val res = service ? Start(topic, embed.data)

          onComplete(res) {
            case Success(_) => complete(Result1.success)
            case Failure(e) => throw new HttpException(500, e)
          }
        }
      }
    }
  }
}

case class EmbedApi(data: List[JcyEmbed])
