package com.funny.utils

import com.typesafe.scalalogging.LazyLogging
import pureconfig.generic.ProductHint
import pureconfig.{CamelCase, ConfigFieldMapping, SnakeCase}
import pureconfig.generic.auto._

case class Config() extends LazyLogging {

  import Config._
  implicit def hint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, SnakeCase))

  lazy val prefixRoot = "funny"

  lazy val gcpConf: GCPConf = pureconfig.loadConfigOrThrow[GCPConf](s"$prefixRoot.gcpConfig")
}

object Config {
  val theConf: Config = Config()
  case class GCPConf(
                      pubsubSubscription: String,
                      pubsubTopic: String,
                      batchMessageSize: Int,
                      googleServiceAccountEmail: String
                    )
}