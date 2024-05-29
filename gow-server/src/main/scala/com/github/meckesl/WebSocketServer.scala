package com.github.meckesl

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow
import akka.stream.{Materializer, SystemMaterializer}

import scala.io.StdIn

object WebSocketServer {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("ActorSystem")
    implicit val materializer: Materializer = SystemMaterializer(system).materializer

    val requestHandler: Flow[Message, Message, _] = Flow[Message].map {
      case TextMessage.Strict(txt) => TextMessage("ECHO: " + txt)
      case _ => TextMessage("Message type unsupported")
    }

    val route =
      path("ws-echo") {
        handleWebSocketMessages(requestHandler)
      } ~
      path("/") {
        getFromResourceDirectory("/web")
      }

    Http().newServerAt("localhost", 8080).bind(route)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()

  }
}