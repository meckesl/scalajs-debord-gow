lazy val server = project.in(file("server"))

lazy val root = project.in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(JSDependenciesPlugin)
  .settings(
    name := "Game of War - Client",
    version := "0.7",
    scalaVersion := "2.13.14",
    scalacOptions ++= Seq("-feature", "-deprecation", "-P:scalajs:nowarnGlobalExecutionContext"),
    scalaJSUseMainModuleInitializer := true,
    testFrameworks += new TestFramework("utest.runner.Framework")
  )

resolvers += ("Typesafe" at "https://repo.typesafe.com/typesafe/releases/")
resolvers += ("Sonatype OSS" at "https://oss.sonatype.org/content/repositories/releases/")
libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "2.8.0",
  "com.lihaoyi" %%% "utest" % "0.8.3" % "test"
)
jsDependencies ++= Seq(
  "org.webjars.npm" % "atmosphere.js" % "3.1.3" / "atmosphere.js" % "compile"
)
Test / jsDependencies := Nil
packageJSDependencies / skip := false
Test / packageJSDependencies / skip := true