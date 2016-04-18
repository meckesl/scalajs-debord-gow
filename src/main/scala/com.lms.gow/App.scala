package com.lms.gow

import com.lms.gow.model.Tile.Tile
import com.lms.gow.model.{Game, Rules}
import org.scalajs.dom
import org.scalajs.dom.document._
import org.scalajs.dom.{CanvasRenderingContext2D, html}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object App extends js.JSApp {

  def main(): Unit = {

    Rules.load() onSuccess {
      case _ => {

        val game = new Game
        val canvas = getElementById("canvas").asInstanceOf[html.Canvas]
        val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

        def drawTile(tile: (Tile, Int), ctx: CanvasRenderingContext2D): Unit = {

          val pos = tile._2
          val alt = pos % 2 == 0
          val x = pos % Rules.terrainWidth
          val y = pos / Rules.terrainWidth
          val w = canvas.width / Rules.terrainWidth
          val h = canvas.height / Rules.terrainHeight

          if (alt)
            ctx.fillStyle = "rgb(255,0,0)"
          else
            ctx.fillStyle = "rgb(0,0,255)"
          ctx.fillRect(x * w, y * h, w, h)

        }

        game.board.terrainLayer.zipWithIndex.foreach { t =>
          drawTile(t, ctx)
        }

      }
    }
  }

}
