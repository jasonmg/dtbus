package com.hawker.http.kafka

import java.util.concurrent.TimeUnit

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.hawker.exception.HttpException
import com.hawker.http.{JsonSupport, Result1}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

case class KafkaMessage(topic: String, message: String)


object KafkaRoute extends JsonSupport {

  implicit val kafkaMessageFormat = jsonFormat2(KafkaMessage)

  implicit val timeout = Timeout(50, TimeUnit.SECONDS)

  def apply(service: ActorRef)(implicit ex: ExecutionContext): Route =

    (post & path("kafka")) {
      entity(as[KafkaMessage]) { msg =>

        val res = service ? Send(msg)
        onComplete(res) {
          case Success(_) => complete(Result1.success)
          case Failure(e) => throw new HttpException(500, e)
        }
      }
    }
}


