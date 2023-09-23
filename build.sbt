import com.typesafe.sbt.packager.docker.DockerPermissionStrategy
import com.typesafe.sbt.packager.docker.DockerVersion
name := """sacris-api"""
organization := "io.sacrisdev"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.7"
scalacOptions ++= Seq(
  "-Xlint",
  "-Xmaxwarns",
  "1000"
)
libraryDependencies += guice

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.1.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "org.postgresql" % "postgresql" % "42.2.8",
)

libraryDependencies ++= Seq("slick-pg", "slick-pg_play-json", "slick-pg_jts_lt").map { slickPg =>
  "com.github.tminglei" %% slickPg % "0.21.1"
}
