package com.lms.gow

import com.lms.gow.model.{Game, Rules}
import org.scalajs.dom

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object App extends js.JSApp {

  def main(): Unit = {

    val boardView = dom.document.createElement("div")
    boardView.innerHTML = s"<strong>The board view is loading...</strong>"
    dom.document.getElementById("bootstrap").appendChild(boardView)

    Rules.load() onSuccess {
      case _ => {
        val game = new Game
        boardView.innerHTML = s"<strong>The board view => ${game.blueTurn}</strong>"
      }
    }
  }

}
