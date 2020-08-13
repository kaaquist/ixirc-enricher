package com.funny

import com.spotify.scio.io.TextIO
import com.spotify.scio.testing._

import org.joda.time.{DateTimeConstants, Duration, Instant}


class WordCountTest extends PipelineSpec {

  val inData = Seq("a b c d e", "a b a b")
  val expected = Seq("a: 3", "b: 3", "c: 1", "d: 1", "e: 1")

  "WordCount" should "work" in {
    JobTest[com.funny.Enricher.type]
      .args("--input=in.txt", "--output=out.txt")
      .input(TextIO("in.txt"), inData)
      .output(TextIO("out.txt"))(_ should containInAnyOrder (expected))
      .run()
  }

  it should "support withSlidingWindows()" in {
    runWithContext { sc =>
      val p =
        sc.parallelizeTimestamped(Seq("a", "b", "c", "d", "e", "f", "g"), (0 to 5).map(new Instant(_)))
      val r = p
        .withSlidingWindows(Duration.millis(2), Duration.millis(2))
        .top(10)
        .map(_.toSet)
      r should containInAnyOrder(Seq(Set("a", "b"), Set("c", "d"), Set("e", "f")))
    }
  }

}
