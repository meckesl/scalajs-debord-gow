enablePlugins(ScalaJSPlugin)

scalacOptions ++= Seq("-feature", "-deprecation", "-P:scalajs:nowarnGlobalExecutionContext")
scalaJSUseMainModuleInitializer := true
testFrameworks += new TestFramework("utest.runner.Framework")

organization := "com.github.meckesl"
name := "gow-client"
version := "0.1.0-SNAPSHOT"
scalaVersion := "2.13.14"
scalacOptions ++= Seq("-feature", "-deprecation")

resolvers += ("Typesafe" at "https://repo.typesafe.com/typesafe/releases/")
resolvers += ("Sonatype OSS" at "https://oss.sonatype.org/content/repositories/releases/")
libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "2.8.0",
  "com.lihaoyi" %%% "utest" % "0.8.3" % "test"
)