import sbt._

object Dependencies {
  private val pekkoVersion = "1.0.3"
  private val playVersion = "3.0.5"
  private val scalatestVersion = "3.2.19"

  val test = Seq(
    "org.scalatest" %% "scalatest" % scalatestVersion % Test,
    "com.vladsch.flexmark" % "flexmark-all" % "0.62.2" % Test,
    "com.typesafe" % "config" % "1.4.3" % Test,
    "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion % Test,
    "org.apache.pekko" %% "pekko-serialization-jackson" % pekkoVersion % Test,
    "org.apache.pekko" %% "pekko-slf4j" % pekkoVersion % Test,
    "org.playframework" %% "play-ahc-ws-standalone" % playVersion % Test,
    "org.playframework" %% "play-ws-standalone-json" % playVersion % Test,
    "org.slf4j" % "slf4j-simple" % "2.0.13" % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % "2.2.0" % Test
  )
}
