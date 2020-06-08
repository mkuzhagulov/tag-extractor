package ru.solar.rt

import scala.collection.mutable.{Map => MMap}

object StatsFormer {
  case class Items(tags: Seq[String], is_answered: Boolean)
  case class QuestionInfo(items: Seq[Items])
  case class Result(tag: String, total: Int, answered: Int)

  def formResult(questions: Seq[Items]): Seq[Result] = {
    val resMap: MMap[String, (Int, Int)] = MMap().withDefaultValue((0, 0))

    questions.foreach { item =>
        if (item.is_answered) item.tags.map(tag => resMap.update(tag, (resMap(tag)._1 + 1, resMap(tag)._2 + 1)))
        else item.tags.map(tag => resMap.update(tag, (resMap(tag)._1 + 1, resMap(tag)._2)))
    }

    resMap.map { case (tag, (total, ans)) => Result(tag, total, ans) }.toSeq
  }
}
