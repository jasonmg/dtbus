package com.hawker.http

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.hawker.http.embed.JcyEmbedActor
import com.hawker.http.kafka.KafkaService
import com.hawker.utils.Log
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}


@SpringBootApplication
//@EnableDiscoveryClient
class Application extends Log {

  implicit val system: ActorSystem = ActorSystem("my-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher


  def start(): Unit = {
    val p = new Props(system)
    val route = ApiRoute(p)

    val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", p.port)

    bindingFuture.onComplete {
      case Success(binding) => log.info(binding.toString)
      case Failure(e) => log.error(e.getLocalizedMessage, e)
    }
  }

  start()
}

class Props(actorSystem: ActorSystem) {

  import com.hawker.utils.ConfigUtil._

  val port = config.getInt("jcyEmbed.port")

  val kafka = config.getConfig("jcyEmbed.kafka").convert[KafkaConf]
  val hbase = config.getConfig("jcyEmbed.hbase").convert[HBaseConf]

  val kafkaServiceActor = actorSystem.actorOf(Props(new KafkaService(actorSystem, kafka.bootstrapServers)), "kafkaService")
  val jcyEmbedServiceActor = actorSystem.actorOf(Props(JcyEmbedActor(kafka, hbase)), "jcyEmbedService")
}


object SampleWebApplication extends App {
  SpringApplication.run(classOf[Application])
}
