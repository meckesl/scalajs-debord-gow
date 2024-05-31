package com.github.meckesl

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.Flow

import scala.concurrent.duration.DurationInt
import scala.io.StdIn

object Server {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("ActorSystem")

    val lobbyHandler: Flow[Message, Message, _] = Flow[Message]
      .idleTimeout(10.minutes)
      .map {
        case TextMessage.Strict(txt) => TextMessage("ECHO: " + txt)
        case _ => TextMessage("Message type unsupported")
      }

    val route = {
      respondWithHeaders(
        RawHeader("Cache-Control", "no-cache, no-store, must-revalidate"), // HTTP 1.1.
        RawHeader("Pragma", "no-cache"), // HTTP 1.0.
        RawHeader("Expires", "0") // Proxies.
      ) {
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
    }

    Http().newServerAt("localhost", 8080).bind(route)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()

  }
}