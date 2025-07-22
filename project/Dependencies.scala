import sbt._

object Dependencies {

  val test: Seq[ModuleID] = Seq(
    "com.typesafe"       % "config"                   % "1.4.4"   % Test,
    "com.typesafe.play" %% "play-ahc-ws-standalone"   % "2.2.11"  % Test,
    "com.typesafe.play" %% "play-ws-standalone-json"  % "2.2.11"  % Test,
    "org.slf4j"          % "slf4j-simple"             % "2.0.17"  % Test,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"  % "2.7.0"   % Test
  )
}
