package com.lms.gow

import com.lms.gow.io.Loader
import com.lms.gow.model.{Game, Point}
import com.lms.gow.ui.UiController
import org.scalajs.dom
import org.scalajs.dom.document._
import org.scalajs.dom.html

import scala.concurrent.ExecutionContext.Implicits.global

object App {

  def main(args: Array[String]): Unit = {

    def getCanvas(c: String) = getElementById(c)
      .asInstanceOf[html.Canvas]
    val backgroundCanvas = getCanvas("backgroundCanvas")
    val comCanvas = getCanvas("comCanvas")
    val terrainCanvas = getCanvas("terrainCanvas")
    val unitCanvas = getCanvas("unitCanvas")
    val overlayCanvas = getCanvas("overlayCanvas")
    val interfaceCanvas = getCanvas("interfaceCanvas")

    Loader.getStartingGamePosition("init.board","init.units", 25).foreach {
      _ =>
        val game = new Game
        val ui = UiController(game, backgroundCanvas, comCanvas, terrainCanvas, unitCanvas, overlayCanvas, interfaceCanvas)
        import scala.scalajs.js.timers._
        ui.onResize(new Point(dom.window.innerWidth, dom.window.innerHeight))
        var handle: SetTimeoutHandle = null
        dom.window.onresize = (_: dom.Event) => {
          clearTimeout(handle)
          handle = setTimeout(200) {
            ui.onResize(new Point(dom.window.innerWidth, dom.window.innerHeight))
          }
        }
        overlayCanvas.onmousemove = (e: dom.MouseEvent) => { ui onMousemove e }
        overlayCanvas.onclick = (e: dom.MouseEvent) => { ui onClick e }
        overlayCanvas.onmouseup = (e: dom.MouseEvent) => { ui onMouseup e }
        overlayCanvas.onmousedown = (e: dom.MouseEvent) => { ui onMousedown e }

        dom.window.onkeydown = (e: dom.KeyboardEvent) => { ui onKeydown e }


    }
  }

}
