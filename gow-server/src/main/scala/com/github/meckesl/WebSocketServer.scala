package com.github.meckesl

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow
import akka.stream.ActorMaterializer

import scala.io.StdIn

object WebSocketServer {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val requestHandler: Flow[Message, Message, _] = Flow[Message].map {
      case TextMessage.Strict(txt) => TextMessage("ECHO: " + txt)
      case _ => TextMessage("Message type unsupported")
    }

    val route =
      path("ws-echo") {
        handleWebSocketMessages(requestHandler)
      } ~
      path("/") {
        getFromResource("index.html")
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

    StdIn.readLine()

  }
}