import sbt._

object Dependencies {

  val test = Seq(
    "com.typesafe" % "config" % "1.4.3" % Test,
    "com.typesafe.play" %% "play-ahc-ws-standalone" % "2.2.9" % Test,
    "com.typesafe.play" %% "play-ws-standalone-json" % "2.2.9" % Test,
    "org.slf4j" % "slf4j-simple" % "2.0.13" % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % "2.2.0" % Test
  )
}
