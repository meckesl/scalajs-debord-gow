//import com.typesafe.sbt.SbtNativePackager.autoImport.NativePackagerHelper._

organization := "org.scalatra.example"
name := "Game of War - Server"
version := "0.1.0-SNAPSHOT"
scalaVersion := "2.12.16"

val ScalatraVersion = "2.8.4"
val jettyVersion = "9.4.7.v20170914"

libraryDependencies ++= Seq(
  "org.json4s"                  %% "json4s-jackson"      % "4.0.7",
  "org.scalatra"                %% "scalatra"            % ScalatraVersion,
  "org.scalatra"                %% "scalatra-scalate"    % ScalatraVersion,
  "org.scalatra"                %% "scalatra-specs2"     % ScalatraVersion   % Test,
  "org.scalatra"                %% "scalatra-atmosphere" % ScalatraVersion,
  "org.scalatra"                %% "scalatra-json"       % ScalatraVersion,
  "org.eclipse.jetty"           %  "jetty-webapp"        % jettyVersion      % Compile,
  "org.eclipse.jetty.websocket" %  "websocket-server"    % jettyVersion      % "compile;provided",
  "ch.qos.logback"              %  "logback-classic"     % "1.5.6",
  "javax.servlet"               %  "javax.servlet-api"   % "4.0.1"
)

enablePlugins(ScalatraPlugin)
