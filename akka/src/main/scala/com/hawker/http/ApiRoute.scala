package com.hawker.http


import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.Materializer
import com.hawker.exception.HttpException
import com.hawker.http.embed.JcyEmbedRoute
import com.hawker.http.kafka.KafkaRoute
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContextExecutor


object ApiRoute extends StrictLogging with JsonSupport {

  val healthCheck: Route =
    path("health_check") {
      get { ctx =>
        ctx.complete(HttpEntity.Empty)
      }
    }

  // 自定义异常处理
  val customExceptionHandler = ExceptionHandler {

    case e: HttpException =>
      extractRequest { req =>
        val msg =
          s"""method: ${req.method}
             |uri: ${req.uri}
             |headers:
             |\t${req.headers.mkString("\n\t")}
             |errorMsg: ${e.throwable.getMessage}""".stripMargin

        logger.error(msg, e.throwable)
        complete(e.errorCode, Result1.failure(e.errorCode, msg))
      }

    case e: Exception =>
      extractRequest { req =>
        logger.error(req.toString, e)
        complete(StatusCodes.InternalServerError, Result1.failure(500, e.getLocalizedMessage))
      }
  }


  def apply(p: Props)(implicit ec: ExecutionContextExecutor, mat: Materializer): Route =

    handleExceptions(customExceptionHandler) {
      pathPrefix("api") {
        healthCheck ~
          KafkaRoute(p.kafkaServiceActor) ~
          JcyEmbedRoute(p.kafka.topic, p.jcyEmbedServiceActor)
      }
    }

}
