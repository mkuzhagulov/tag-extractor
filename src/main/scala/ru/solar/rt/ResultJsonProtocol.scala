package ru.solar.rt

import spray.json._
import StatsFormer._

object ResultJsonProtocol extends DefaultJsonProtocol {
  implicit val resultJsonFormat: RootJsonFormat[Result] = new RootJsonFormat[Result] {
    def write(res: Result): JsObject =
      JsObject(res.tag -> JsObject("total" -> JsNumber(res.total), "answered" -> JsNumber(res.answered)))

    // reader is unnecessary
    def read(value: JsValue): Result = ???
  }
}
