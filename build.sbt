lazy val root = project.in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "scalajs-debord-gow",
    version := "0.6",
      inConfig(Test)(Seq(
      scalacOptions += "scalajs:nowarnGlobalExecutionContext" // disables warning The global execution context in Scala.js is based on JS Promises (microtasks).
    ))
  )

scalaVersion := "2.13.14"
ThisBuild / scalacOptions ++= Seq("-feature")
scalaJSUseMainModuleInitializer := true

testFrameworks += new TestFramework("utest.runner.Framework")

resolvers += ("Typesafe" at "https://repo.typesafe.com/typesafe/releases/")
resolvers += ("Sonatype OSS" at "https://oss.sonatype.org/content/repositories/releases/")
libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "1.2.0",
  "be.doeraene" %%% "scalajs-jquery" % "1.0.0",
  "com.lihaoyi" %%% "utest" % "0.8.3" % "test"
)
