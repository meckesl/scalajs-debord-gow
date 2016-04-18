package com.lms.gow

import com.lms.gow.model.{Game, Rules}
import org.scalajs.dom
import org.scalajs.dom.document._
import org.scalajs.dom.html
import org.scalajs.dom.raw.HTMLImageElement

import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object App extends js.JSApp {

  def main(): Unit = {

    Rules.load() onSuccess {
      case _ => {

        val game = new Game
        val canvas = getElementById("canvas").asInstanceOf[html.Canvas]
        val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

        canvas.height = dom.window.innerHeight
        canvas.width = dom.window.innerWidth

        game.board.terrainLayer.zipWithIndex.foreach { tile =>
          val pos = tile._2
          val alt = pos % 2 == 0
          val x = pos % Rules.terrainWidth
          val y = pos / Rules.terrainWidth
          val w = canvas.width / Rules.terrainWidth
          val h = canvas.height / Rules.terrainHeight
          if (alt)
            ctx.fillStyle = "rgb(240,240,240)"
          else
            ctx.fillStyle = "rgb(255,255,255)"
          ctx.fillRect(x * w, y * h, w, h)
        }

        Rules.terrainTilesRepository.foreach(t => {
          val image: HTMLImageElement = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
          image.src = Util.getTileUrl(t)
          image.onload = (e: dom.Event) => {
            game.board.terrainLayer.zipWithIndex.filter(_._1.char.equals(t.char)).foreach { u =>
              val pos = u._2
              val alt = pos % 2 == 0
              val x = pos % Rules.terrainWidth
              val y = pos / Rules.terrainWidth
              val w = canvas.width / Rules.terrainWidth
              val h = canvas.height / Rules.terrainHeight
              ctx.drawImage(image, x * w, y * h, w, h)
            }
          }
        })

        Rules.unitTilesRepository.foreach(t => {
          val image: HTMLImageElement = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
          image.src = Util.getTileUrl(t)
          image.onload = (e: dom.Event) => {
            game.board.unitLayer.zipWithIndex.filter(_._1.char.equals(t.char)).foreach { u =>
              val pos = u._2
              val alt = pos % 2 == 0
              val x = pos % Rules.terrainWidth
              val y = pos / Rules.terrainWidth
              val w = canvas.width / Rules.terrainWidth
              val h = canvas.height / Rules.terrainHeight
              ctx.drawImage(image, x * w, y * h, w, h)
            }
          }
        })

      }
    }
  }

}
