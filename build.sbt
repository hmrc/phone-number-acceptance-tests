ThisBuild / scalaVersion := "2.13.14"
ThisBuild / majorVersion := 0

lazy val testSuite = (project in file("."))
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    name := "phone-number-acceptance-tests",
    scalacOptions ++= Seq("-feature"),
    libraryDependencies ++= Dependencies.test
  )
