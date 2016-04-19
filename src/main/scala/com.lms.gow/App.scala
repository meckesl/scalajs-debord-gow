package com.lms.gow

import com.lms.gow.model.{Game, Rules}
import com.lms.gow.ui.{Point, Ui}
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

    Rules.load() onSuccess {
      case _ =>

        val game = new Game
        val ui = new Ui(game, gameCanvas, overlayCanvas)

        import scala.scalajs.js.timers._
        var handle: SetTimeoutHandle = null
        ui.resize(new Point(dom.window.innerWidth, dom.window.innerHeight))
        dom.window.onresize = (e: dom.Event) => {
          clearTimeout(handle)
          handle = setTimeout(200) {
            ui.resize(new Point(dom.window.innerWidth, dom.window.innerHeight))
          }
        }

        overlayCanvas.onmousemove = (e: dom.MouseEvent) => {
          ui.redrawMouseOverlay(new Point(e.clientX, e.clientY))
        }
    }
  }

}
