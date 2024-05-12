package com.lms.gow

import com.lms.gow.io.Loader
import com.lms.gow.model.{Game, Point}
import com.lms.gow.ui.UiController
import org.scalajs.dom._
import org.scalajs.dom.document._
import org.scalajs.dom.html

import scala.concurrent.ExecutionContext.Implicits.global

object App {

  def main(args: Array[String]): Unit = {

    def getCanvas(c: String) = getElementById(c).asInstanceOf[html.Canvas]
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
        ui.onResize(new Point(window.innerWidth, window.innerHeight))
        var handle: SetTimeoutHandle = null
        window.onresize = (_: Event) => {
          clearTimeout(handle)
          handle = setTimeout(200) {
            ui.onResize(new Point(window.innerWidth, window.innerHeight))
          }
        }
        overlayCanvas.onmousemove = (e: MouseEvent) => { ui onMousemove e }
        overlayCanvas.onclick = (e: MouseEvent) => { ui onClick e }
        overlayCanvas.onmouseup = (e: MouseEvent) => { ui onMouseup e }
        overlayCanvas.onmousedown = (e: MouseEvent) => { ui onMousedown e }
        window.onkeydown = (e: KeyboardEvent) => { ui onKeydown e }

    }
  }

}
