package com.github.meckesl.ws

import org.scalajs.dom.{Event, HTMLDivElement, HTMLSpanElement, MessageEvent, WebSocket}

class LobbyClient(div : HTMLDivElement) {

  private val connection = new WebSocket("ws://localhost:8080/lobby")

  connection.onopen = (event: Event) => {
    output("WebSocket connection established")
  }

  connection.onerror = (event: Event) => {
    output(s"WebSocket error observed: ${event.toString}")
  }

  connection.onmessage = (messageEvent: MessageEvent) => {
    output(s"Received from server: ${messageEvent.data.toString}")
  }

  connection.onclose = (event: Event) => {
    output("WebSocket connection closed")
  }

  def sendMessage(message: String, connection: WebSocket): Unit = {
    connection.send(message)
  }

  private def output(s: String): Unit = div.innerHTML += s"$s<br/>"

}

