organization := "com.github.meckesl"
name := "gow-server"
version := "0.1.0-SNAPSHOT"
scalaVersion := "2.13.14"
scalacOptions ++= Seq("-feature", "-deprecation")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.5.3",
  "com.typesafe.akka" %% "akka-stream" % "2.8.5"
)
