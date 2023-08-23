ThisBuild / scalaVersion := "2.13.11"
ThisBuild / version := "1.0-SNAPSHOT"

import com.typesafe.sbt.packager.docker.DockerChmodType
dockerChmodType := DockerChmodType.UserGroupWriteExecute

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """sacrisapp-api""",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
