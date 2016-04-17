package com.lms.gow

import com.lms.gow.model.Game
import org.scalajs.dom

import scala.scalajs.js

object App extends js.JSApp {

  val game = new Game

  def main(): Unit = {

    val boardView = dom.document.createElement("div")

    boardView.innerHTML = s"<strong>The board view! ${game.blueTurn}</strong>"
    dom.document.getElementById("bootstrap").appendChild(boardView)
  }

}
