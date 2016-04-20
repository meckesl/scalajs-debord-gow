package com.lms.gow

import com.lms.gow.io.Loader
import com.lms.gow.model.{Game, Point}
import com.lms.gow.ui.Ui
import org.scalajs.dom
import org.scalajs.dom.document._
import org.scalajs.dom.html

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object App extends js.JSApp {

  def main(): Unit = {

    val gameCanvas = getElementById("gameCanvas")
      .asInstanceOf[html.Canvas]
    val overlayCanvas = getElementById("overlayCanvas")
      .asInstanceOf[html.Canvas]
    val statusCanvas = getElementById("statusCanvas")
      .asInstanceOf[html.Canvas]

    Loader.loadStartingGamePosition() onSuccess {
      case _ =>
        val game = new Game
        val ui = new Ui(game, gameCanvas, overlayCanvas, statusCanvas)

        import scala.scalajs.js.timers._
        var handle: SetTimeoutHandle = null
        ui.onResize(new Point(dom.window.innerWidth, dom.window.innerHeight))
        dom.window.onresize = (e: dom.Event) => {
          clearTimeout(handle)
          handle = setTimeout(200) {
            ui.onResize(new Point(dom.window.innerWidth, dom.window.innerHeight))
          }
        }

        overlayCanvas.onmousemove = (e: dom.MouseEvent) => {
          ui.onHover(new Point(e.clientX, e.clientY))
        }

        overlayCanvas.onclick = (e: dom.MouseEvent) => {
          ui.onClick(new Point(e.clientX, e.clientY))
        }
    }
  }

}
