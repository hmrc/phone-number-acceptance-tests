import sbt._

object Dependencies {

  val test = Seq(
    "org.scalatest"       %% "scalatest"               % "3.2.15"  % Test,
    "com.vladsch.flexmark" % "flexmark-all"            % "0.62.2"  % Test,
    "com.typesafe"         % "config"                  % "1.4.2"   % Test,
    "com.typesafe.play"   %% "play-ahc-ws-standalone"  % "2.1.10"   % Test,
    "com.typesafe.play"   %% "play-ws-standalone-json" % "2.1.10"   % Test,
    "org.slf4j"            % "slf4j-simple"            % "2.0.5"  % Test,
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-test-play-28" % "0.71.0"  % Test
  )
}
