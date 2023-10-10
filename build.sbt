import com.typesafe.sbt.packager.docker.DockerPermissionStrategy
import com.typesafe.sbt.packager.docker.DockerVersion
name := """sacris-api"""
organization := "io.sacrisdev"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
enablePlugins(JavaAppPackaging)
enablePlugins(LinuxPlugin)

scalaVersion := "2.13.7"
scalacOptions ++= Seq(
  "-Xlint",
  "-Xmaxwarns",
  "1000"
)
libraryDependencies += guice

//Slick
libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "5.1.0",
  "org.postgresql" % "postgresql" % "42.2.8",
)
//Slick
libraryDependencies ++= Seq("slick-pg", "slick-pg_play-json", "slick-pg_jts_lt").map { slickPg =>
  "com.github.tminglei" %% slickPg % "0.21.1"
}

// https://typelevel.org/cats/
libraryDependencies += "org.typelevel" %% "cats-core" % "2.9.0"


//Auth0
libraryDependencies ++= Seq(
  "com.github.jwt-scala" % "jwt-core_2.13" % "9.4.4",
  "com.github.jwt-scala" % "jwt-play_2.13" % "9.4.4",
  "com.github.jwt-scala" % "jwt-play-json_2.13" % "9.4.4",
  "com.auth0" % "jwks-rsa" % "0.6.1"
)

//Packaging
name := "SacrisAPI"
version := "1.0"
maintainer := "gojideth@sacrisapp.org"
packageSummary := "API for SacrisApp"
packageDescription := """This is the API that will serve as backend for SacrisAPI"""
name in Linux := name.value