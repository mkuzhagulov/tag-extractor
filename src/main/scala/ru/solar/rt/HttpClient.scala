package ru.solar.rt

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.http.scaladsl.coding.{Deflate, Gzip, NoCoding}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.headers.HttpEncodings
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri, _}
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.{ClientTransport, Http}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import ru.solar.rt.StatsFormer._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

import scala.concurrent.Future

object HttpClient extends DefaultJsonProtocol {

  implicit val itemsFormat: RootJsonFormat[Items] = jsonFormat2(Items)
  implicit val questionInfo: RootJsonFormat[QuestionInfo] = jsonFormat1(QuestionInfo)
}

class HttpClient(implicit system: ActorSystem, materializer: ActorMaterializer) {

  import HttpClient._
  import system.dispatcher

  private val maxConnections = AppGlobals.config.getInt("max-connections")
  private val proxyEnable = AppGlobals.config.getBoolean("proxy-enable")
  private val proxyHost = AppGlobals.config.getString("proxy-host")
  private val proxyPort = AppGlobals.config.getInt("proxy-port")
  private val uri = AppGlobals.config.getString("stack-exchange-url")

  private val httpsProxyTransport = ClientTransport.httpsProxy(InetSocketAddress.createUnresolved(proxyHost, proxyPort))

  private val connectionSettings = if (proxyEnable)
    ConnectionPoolSettings(system).withMaxConnections(maxConnections).withTransport(httpsProxyTransport)
  else
    ConnectionPoolSettings(system).withMaxConnections(maxConnections)

  private val httpPool = Http().superPool[String](settings = connectionSettings)
  
  private def decodeEntity(response: HttpResponse): HttpResponse = {
    val decoder = response.encoding match {
      case HttpEncodings.gzip ⇒
        Gzip
      case HttpEncodings.deflate ⇒
        Deflate
      case HttpEncodings.identity ⇒
        NoCoding
    }
    decoder.decodeMessage(response)
  }

  def requestsToStackExchange(tags: Seq[String]): Future[Seq[QuestionInfo]] = {

    val futSeq: Future[Seq[HttpResponse]] = Source(tags)
      .map(t => (HttpRequest(uri = Uri(uri + s"&tagged=$t")), t))
      .via(httpPool)
      .map { case (resp, _) => resp.get }
      .runWith(Sink.seq)

    futSeq.flatMap(x => Future.sequence(x.map(resp => Unmarshal(decodeEntity(resp).entity).to[QuestionInfo])))
  }
}
