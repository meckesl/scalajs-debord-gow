

lazy val root = project.in(file(".")).enablePlugins(ScalaJSPlugin)

/*lazy val jvm = project.in(file(".")).settings(
  libraryDependencies --= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.0",
    "be.doeraene" %%% "scalajs-jquery" % "0.9.0"
  )
)*/

name := "scalajs-debord-gow"
version := "0.5-SNAPSHOT"
//javacOptions ++= Seq("-source", "11", "-target", "11")
scalaVersion := "2.13.14"
scalacOptions in ThisBuild ++= Seq("-feature")
scalaJSUseMainModuleInitializer := true
//scalaJSUseRhino in Global := false
//persistLauncher in Compile := true

testFrameworks += new TestFramework("utest.runner.Framework")
//persistLauncher in Test := false

resolvers += ("Typesafe" at "http://repo.typesafe.com/typesafe/releases/").withAllowInsecureProtocol(true)
resolvers += ("Sonatype OSS" at "https://oss.sonatype.org/content/repositories/releases/").withAllowInsecureProtocol(true)
libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "1.2.0",
  "be.doeraene" %%% "scalajs-jquery" % "1.0.0",
  "com.lihaoyi" %%% "utest" % "0.8.3" % "test"
)
