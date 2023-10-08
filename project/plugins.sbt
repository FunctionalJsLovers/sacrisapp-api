
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.20")

// for autoplugins
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.16")

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
