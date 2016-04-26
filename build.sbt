

lazy val root = project.in(file(".")).enablePlugins(ScalaJSPlugin)

/*lazy val jvm = project.in(file(".")).settings(
  libraryDependencies --= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.0",
    "be.doeraene" %%% "scalajs-jquery" % "0.9.0"
  )
)*/

name := "scalajs-debord-gow"
version := "0.5-SNAPSHOT"

scalaVersion := "2.11.8"
scalacOptions in ThisBuild ++= Seq("-feature")
scalaJSUseRhino in Global := false
persistLauncher in Compile := true

testFrameworks += new TestFramework("utest.runner.Framework")
persistLauncher in Test := false
//jsDependencies in Test += RuntimeDOM

resolvers += "Typesafe" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "Sonatype OSS" at "https://oss.sonatype.org/"
libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.0",
  "be.doeraene" %%% "scalajs-jquery" % "0.9.0",
  "com.lihaoyi" %%% "utest" % "0.4.3" % "test"
)
