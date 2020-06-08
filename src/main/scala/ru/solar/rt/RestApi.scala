package ru.solar.rt

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import StatsFormer._
import spray.json._

import scala.concurrent.Future
import scala.util.{Failure, Success}

class RestApi(client: HttpClient)(implicit val actorSystem: ActorSystem, val materializer: ActorMaterializer) {
  import ResultJsonProtocol._
  import actorSystem.dispatcher

  private def fullReq(tags: Seq[String]): Future[Seq[Result]] =
    client.requestsToStackExchange(tags)
      .map(x => StatsFormer.formResult(x.flatMap(_.items)))

  def searchRoute: Route =
    pathPrefix("search") {
      pathEndOrSingleSlash {
        get {
          parameters('tag.*) { tags =>
            onComplete(fullReq(tags.toSeq)) {
              case Success(value) => complete(value.toJson)
              case Failure(_) => complete(StatusCodes.ServiceUnavailable)
            }
          }
        }
      }
    }

  def routes: Route = searchRoute
}
