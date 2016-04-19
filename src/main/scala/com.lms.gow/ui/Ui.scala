package com.lms.gow.ui

import com.lms.gow.io.Loader
import com.lms.gow.model.{Game, Rules}
import org.scalajs.dom
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.HTMLImageElement

case class Ui(game: Game, gameCanvas: Canvas, gameOverlay: Canvas) {

  val boardSize = new Point(Rules.terrainWidth, Rules.terrainWidth)
  var uiSize = new Point(gameCanvas.width, gameCanvas.height)
  var tileSize = uiSize / boardSize

  object Color {
    def rgb(r: Int, g: Int, b: Int) = s"rgb($r, $g, $b)"
    def rgba(r: Int, g: Int, b: Int, a: Int) = s"rgba($r, $g, $b, $a)"
    val White = rgb(255, 255, 255)
    val Silver = rgb(240, 240, 240)
    val Blue = rgb(0, 0, 255)
    val Red = rgb(255, 0, 0)
    val Highlight = rgba(0, 255, 0, 64)
  }

  def redrawGame {
    val ctx = gameCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    0 until Rules.totalTiles foreach { index =>
      val tile = Point.fromLinear(index, Rules.terrainWidth)
      if (index % 2 == 0) ctx.fillStyle = Color.Silver else ctx.fillStyle = Color.White
      ctx.fillRect(tile.x * tileSize.x, tile.y * tileSize.y, tileSize.x, tileSize.y)
    }

    Rules.terrainTilesRepository.foreach(t => {
      val image: HTMLImageElement = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
      image.src = Loader.getTileUrl(t)
      image.onload = (e: dom.Event) => {
        game.board.terrainLayer.zipWithIndex.filter(_._1.equals(t)).foreach { u =>
          val tile = Point.fromLinear(u._2, Rules.terrainWidth)
          ctx.drawImage(image, tile.x * tileSize.x, tile.y * tileSize.y, tileSize.x, tileSize.y)
        }
      }
    })

    Rules.unitTilesRepository.foreach(t => {
      val image: HTMLImageElement = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
      image.src = Loader.getTileUrl(t)
      image.onload = (e: dom.Event) => {
        game.board.unitLayer.zipWithIndex.filter(_._1.equals(t)).foreach { u =>
          val tile = Point.fromLinear(u._2, Rules.terrainWidth)
          ctx.drawImage(image, tile.x * tileSize.x, tile.y * tileSize.y, tileSize.x, tileSize.y)
          if (u._1.isBlue) ctx.fillStyle = Color.Blue else ctx.fillStyle = Color.Red
          ctx.fillRect(
            tile.x * tileSize.x,
            (tile.y * tileSize.y) + (tileSize.y - tileSize.y / 12),
            tileSize.x, tileSize.y / 12)
        }
      }
    })

  }

  def redrawMouseOverlay(mouse: Point): Unit = {
    val ctx = gameOverlay.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    val x = (uiSize.x - (uiSize.x - mouse.x))
    val y = (uiSize.y - (uiSize.y - mouse.y))
    ctx.clearRect(0, 0, uiSize.x, uiSize.y)
    ctx.fillStyle = Color.Highlight
    ctx.fillRect((x - (x % tileSize.x)), (y - (y % tileSize.y)), tileSize.x, tileSize.y)
  }

  def resize(s: Point): Unit = {
    uiSize = s
    tileSize = uiSize / boardSize
    gameCanvas.height = uiSize.y.toInt
    gameCanvas.width = uiSize.x.toInt
    gameOverlay.height = uiSize.y.toInt
    gameOverlay.width = uiSize.x.toInt
    redrawGame
  }

}
