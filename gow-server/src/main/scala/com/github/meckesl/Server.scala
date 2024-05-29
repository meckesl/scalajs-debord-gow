package com.github.meckesl

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow

import scala.io.StdIn

object Server {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("ActorSystem")

    val lobbyHandler: Flow[Message, Message, _] = Flow[Message].map {
      case TextMessage.Strict(txt) => TextMessage("ECHO: " + txt)
      case _ => TextMessage("Message type unsupported")
    }

    val route = {
      pathPrefix("lobby") {
        handleWebSocketMessages(lobbyHandler)
      } ~
        get {
          extractUnmatchedPath { path =>
            if (path.toString == "/" || path.toString.isEmpty)
              getFromResource("web/index.html")
            else
              getFromResourceDirectory("web")
          }
        }
    }

    Http().newServerAt("localhost", 8080).bind(route)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()

  }
}