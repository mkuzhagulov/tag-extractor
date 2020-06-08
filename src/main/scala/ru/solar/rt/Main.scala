package ru.solar.rt

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("main-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val log =  Logging(system.eventStream, "DataExchangeApi")

  val client = new HttpClient()
  val restApi = new RestApi(client)

  val bindingFuture = Http().bindAndHandle(restApi.routes, "localhost", 8080)

  bindingFuture.map { serverBinding =>
    log.info(s"RestApi bound to ${serverBinding.localAddress} ")
  }.failed.foreach {
    case ex: Exception =>
      log.error(ex, "Failed to bind to localhost")
      system.terminate()
  }
}
