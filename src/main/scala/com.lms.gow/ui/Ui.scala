package com.lms.gow.ui

import com.lms.gow.io.Loader
import com.lms.gow.model.repo.{PlayerRepository, TileRepository}
import com.lms.gow.model.{Game, Rules}
import com.lms.gow.ui.model.Point
import org.scalajs.dom
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.HTMLImageElement

case class Ui(game: Game, gameCanvas: Canvas, gameOverlay: Canvas, statusCanvas: Canvas) {

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

  def redrawGame() {
    val ctx = gameCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    // Background
    0 until Rules.totalTiles foreach { index =>
      val tile = Point.fromLinear(index, Rules.terrainWidth)
      if (index % 2 == 0) ctx.fillStyle = Color.Silver else ctx.fillStyle = Color.White
      ctx.fillRect(tile.x * tileSize.x, tile.y * tileSize.y, tileSize.x, tileSize.y)
    }

    // Terrain
    TileRepository.terrains.foreach(t => {
      val image: HTMLImageElement = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
      image.src = Loader.getTileUrl(t)
      image.onload = (e: dom.Event) => {
        game.board.boardTiles.filter(_.terrain.equals(t)).foreach { t =>
          val te: Point = t.coords * tileSize
          ctx.drawImage(image, te.x, te.y, tileSize.x, tileSize.y)
        }
      }
    })

    // Units
    TileRepository.units.foreach(t => {
      val image: HTMLImageElement = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
      image.src = Loader.getTileUrl(t)
      image.onload = (e: dom.Event) => {
        game.board.boardTiles.filter(_.unit.equals(t)).foreach { t =>
          val te: Point = t.coords * tileSize
          ctx.drawImage(image, te.x, te.y, tileSize.x, tileSize.y)
          if (PlayerRepository.Blue.equals(t.unit.player)) ctx.fillStyle = Color.Blue else ctx.fillStyle = Color.Red
          ctx.fillRect(
            te.x,
            te.y + (tileSize.y - tileSize.y / 12),
            tileSize.x, tileSize.y / 12)
        }
      }
    })

  }

  def redrawStatusOverlay() {
    val ctx = statusCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    ctx.fillStyle = Color.Silver
    ctx.fillRect(0, 0, statusCanvas.width, statusCanvas.height)
  }

  def redrawMouseOverlay(mouse: Point) {
    val ctx = gameOverlay.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    ctx.globalAlpha = 0.5
    val x = uiSize.x - (uiSize.x - mouse.x)
    val y = uiSize.y - (uiSize.y - mouse.y)
    ctx.clearRect(0, 0, uiSize.x, uiSize.y)
    ctx.fillStyle = Color.Highlight
    ctx.fillRect(x - (x % tileSize.x), y - (y % tileSize.y), tileSize.x, tileSize.y)
  }

  def resize(s: Point): Unit = {
    uiSize = s
    tileSize = uiSize / boardSize
    gameCanvas.height = (tileSize.y * 20).toInt
    gameCanvas.width = (tileSize.x * 25).toInt
    gameOverlay.height = gameCanvas.height
    gameOverlay.width = gameCanvas.width
    statusCanvas.height = (uiSize.y - gameCanvas.height).toInt
    statusCanvas.width = gameCanvas.width
    redrawGame()
    redrawStatusOverlay()
  }

}
