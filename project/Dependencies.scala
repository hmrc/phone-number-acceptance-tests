import sbt._

object Dependencies {

  val test = Seq(
    "org.scalatest" %% "scalatest" % "3.2.19" % Test,
    "com.vladsch.flexmark" % "flexmark-all" % "0.62.2" % Test,
    "com.typesafe" % "config" % "1.4.3" % Test,
    "org.apache.pekko" %% "pekko-actor-typed" % "1.0.3" % Test,
    "org.apache.pekko" %% "pekko-serialization-jackson" % "1.0.3" % Test,
    "org.apache.pekko" %% "pekko-slf4j" % "1.0.3" % Test,
    "org.playframework" %% "play-ahc-ws-standalone" % "3.0.5" % Test,
    "org.playframework" %% "play-ws-standalone-json" % "3.0.5" % Test,
    "org.slf4j" % "slf4j-simple" % "2.0.13" % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % "2.2.0" % Test
  )
}
