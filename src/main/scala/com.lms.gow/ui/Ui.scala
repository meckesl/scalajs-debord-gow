package com.lms.gow.ui

import com.lms.gow.Util
import com.lms.gow.model.{Game, Rules}
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.HTMLImageElement

object Ui {

  def redrawMouseOverlay(canvas: html.Canvas, game: Game, mouseX: Double, mouseY: Double): Unit = {
    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    val w = canvas.width / Rules.terrainWidth
    val h = canvas.height / Rules.terrainHeight
    val x = (canvas.width - (canvas.width - mouseX))
    val y = (canvas.height - (canvas.height - mouseY))
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    ctx.fillStyle = "rgba(0,255,0,64)"
    ctx.fillRect((x - (x % w)) , (y - (y % h)) , w, h)
  }

  def redrawOverlay(canvas: html.Canvas, game: Game): Unit = {
    canvas.height = dom.window.innerHeight
    canvas.width = dom.window.innerWidth
  }

  def redrawGame(canvas: html.Canvas, game: Game) {

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
        game.board.terrainLayer.zipWithIndex.filter(_._1.equals(t)).foreach { u =>
          val pos = u._2
          val x = pos % Rules.terrainWidth
          val y: Int = pos / Rules.terrainWidth
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
        game.board.unitLayer.zipWithIndex.filter(_._1.equals(t)).foreach { u =>
          val pos = u._2
          val x = pos % Rules.terrainWidth
          val y = pos / Rules.terrainWidth
          val w = canvas.width / Rules.terrainWidth
          val h = canvas.height / Rules.terrainHeight
          ctx.drawImage(image, x * w, y * h, w, h)
          if (u._1.isBlue)
            ctx.fillStyle = "rgb(0,0,255)"
          else
            ctx.fillStyle = "rgb(255,0,0)"
          ctx.fillRect(x * w, (y * h) + (h - h / 12), w, h / 12)
        }
      }
    })

  }

}
