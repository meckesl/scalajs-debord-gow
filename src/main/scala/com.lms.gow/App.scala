package com.lms.gow

import com.lms.gow.model.{Game, Rules}
import com.lms.gow.ui.Ui._
import org.scalajs.dom
import org.scalajs.dom.document._
import org.scalajs.dom.html

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object App extends js.JSApp {

  def main(): Unit = {
    Rules.load() onSuccess {
      case _ =>

        val game = new Game

        val gameCanvas = getElementById("gameCanvas")
          .asInstanceOf[html.Canvas]
        val overlayCanvas = getElementById("overlayCanvas")
          .asInstanceOf[html.Canvas]

        redrawGame(gameCanvas, game)
        redrawOverlay(overlayCanvas, game)

        dom.window.onresize = (e: dom.Event) => {
          redrawGame(gameCanvas, game)
          redrawOverlay(overlayCanvas, game)
        }
        overlayCanvas.onmousemove = (e: dom.MouseEvent) => {
          dom.console.log(s"${e.clientX}/${e.clientY}")
          redrawMouseOverlay(overlayCanvas, game, e.clientX, e.clientY)
        }
    }
  }

}
