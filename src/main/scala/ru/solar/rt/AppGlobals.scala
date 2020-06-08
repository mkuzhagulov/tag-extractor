package ru.solar.rt

import com.typesafe.config.{Config, ConfigFactory}

object AppGlobals {
  val config: Config = ConfigFactory.load()
}
