package com.funny

import java.util.Base64

import com.funny.release.Releases
import com.funny.utils.Config
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.Duration
/*
sbt "runMain [PACKAGE].Enricher
  --project=[PROJECT] --runner=DataflowRunner --zone=[ZONE]
  --input=gs://dataflow-samples/shakespeare/kinglear.txt
  --output=gs://[BUCKET]/[PATH]/wordcount"
*/

import com.spotify.scio._
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO
import org.apache.beam.sdk.options.{
  Description,
  PipelineOptions,
  PipelineOptionsFactory,
  StreamingOptions,
  ValueProvider
}
import org.apache.beam.sdk.options.Validation.Required

object Enricher extends LazyLogging {
  trait Options extends PipelineOptions with StreamingOptions {
    @Description("The Cloud Pub/Sub subscription to read from")
    @Required
    def getInputSubscription: ValueProvider[String]
    def setInputSubscription(value: ValueProvider[String]): Unit

    @Description("The Cloud Pub/Sub topic to write to")
    //@Required
    def getOutputTopic: ValueProvider[String]
    def setOutputTopic(value: ValueProvider[String]): Unit
  }

  def main(cmdlineArgs: Array[String]): Unit = {
    PipelineOptionsFactory.register(classOf[Options])
    val options = PipelineOptionsFactory
      .fromArgs(cmdlineArgs: _*)
      .withValidation
      .as(classOf[Options])
    options.setStreaming(true)
    run(options)
  }

  def run(options: Options): Unit = {
    val config = Config.theConf.gcpConf
    val sc = ScioContext(options)
    val subscription = config.pubsubSubscription

    val inputIO = PubsubIO.readStrings().fromSubscription(subscription)
    val outputIO = PubsubIO.writeStrings().to(options.getOutputTopic)
    sc.customInput("input", inputIO).withFixedWindows(Duration.standardMinutes(10)).withGlobalWindow()
      .distinctBy(releases => {
        val byteArr = Base64.getDecoder.decode(releases)
        val yy = Releases.parseFrom(byteArr)
        yy.releases.map(_.name)
      }).map(filteredReleases => {
      Releases.parseFrom(Base64.getDecoder.decode(filteredReleases)).releases.foreach(x => logger.info(x.name))
    })
      //.saveAsCustomOutput("output", outputIO)
    sc.run()
    ()
  }
}
