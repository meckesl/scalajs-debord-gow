lazy val root = project.in(file(".")).enablePlugins(ScalaJSPlugin)

name := "scalajs-debord-gow"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.8"

persistLauncher in Compile := true

persistLauncher in Test := false

testFrameworks += new TestFramework("utest.runner.Framework")

resolvers += "Typesafe" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "Sonatype OSS" at "https://oss.sonatype.org/"

libraryDependencies ++= Seq(
    "org.scala-js" % "scalajs-dom_sjs0.6_2.11" % "0.9.0",
    "be.doeraene" %%% "scalajs-jquery" % "0.9.0",
    "com.lihaoyi" %%% "utest" % "0.3.0" % "test"
)
