package com.lms.gow.ui

import com.lms.gow.io.Loader
import com.lms.gow.model.repo.CardinalityRepository._
import com.lms.gow.model.repo.PlayerRepository.Blue
import com.lms.gow.model.repo.TileRepository.VoidTile
import com.lms.gow.model.repo.{PlayerRepository, RuleRepository, TileRepository}
import com.lms.gow.model.{Game, GameSquare}
import com.lms.gow.ui.model.Point
import org.scalajs.dom
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.HTMLImageElement

case class Ui(game: Game, gameCanvas: Canvas, gameOverlay: Canvas, statusCanvas: Canvas) {

  val boardSize = new Point(RuleRepository.squareX, RuleRepository.squareX)
  var uiSize = new Point(gameCanvas.width, gameCanvas.height)
  var statusSize = new Point(statusCanvas.width, statusCanvas.height)
  var tileSize = uiSize / boardSize
  var curStatusSquare: GameSquare = null

  object Color {
    def rgb(r: Int, g: Int, b: Int) = s"rgb($r, $g, $b)"
    def rgba(r: Int, g: Int, b: Int, a: Int) = s"rgba($r, $g, $b, $a)"
    val White = rgb(255, 255, 255)
    val Silver = rgb(247, 247, 247)
    val Blue = rgb(0, 0, 255)
    val Red = rgb(255, 0, 0)
    val BlueAlpha = rgba(0, 0, 255, 64)
    val RedAlpha = rgba(255, 0, 0, 64)
    val Highlight = rgba(0, 255, 0, 64)
  }

  def redrawGame() {
    val ctx = gameCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    // Background
    0 until RuleRepository.squareCount foreach { index =>
      val tile = Point.fromLinear(index, RuleRepository.squareX)
      if (index % 2 == 0) ctx.fillStyle = Color.Silver else ctx.fillStyle = Color.White
      ctx.fillRect(tile.x * tileSize.x, tile.y * tileSize.y, tileSize.x, tileSize.y)
    }

    // Terrain
    TileRepository.terrains.foreach(t => {
      val image: HTMLImageElement = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
      image.src = Loader.getTileUrl(t)
      image.onload = (e: dom.Event) => {
        game.gameSquares.filter(_.terrain.equals(t)).foreach { sq =>
          val te: Point = sq.coords * tileSize
          ctx.drawImage(image, te.x, te.y, tileSize.x, tileSize.y)
        }
      }
    })

    // Units
    TileRepository.units.foreach(t => {
      val image: HTMLImageElement = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
      image.src = Loader.getTileUrl(t)
      image.onload = (e: dom.Event) => {
        game.gameSquares.filter(_.unit.equals(t)).foreach { sq =>
          val te: Point = sq.coords * tileSize
          ctx.drawImage(image, te.x, te.y, tileSize.x, tileSize.y)
          if (PlayerRepository.Blue.equals(sq.unit.player)) ctx.fillStyle = Color.Blue else ctx.fillStyle = Color.Red
          ctx.fillRect(
            te.x + (tileSize.x / 20),
            te.y + (tileSize.y - tileSize.y / 12),
            tileSize.x - (tileSize.x / 20), tileSize.y / 12)
        }
      }
    })

    // Communication
    game.gameSquares.foreach(sq => {
      sq.com.foreach(com => {

        if (com._1.equals(Blue))
          ctx.strokeStyle = Color.BlueAlpha
        else
          ctx.strokeStyle = Color.RedAlpha

        com._2.foreach(c => {

          var a: Point = null
          var b: Point = null

          ctx.beginPath()
          if (Seq(NW, SE, SOURCE).contains(c)) {
            a = sq.coords * tileSize
            b = a + tileSize
            ctx.moveTo(a.x, a.y)
            ctx.lineTo(b.x, b.y)
            ctx.stroke()
          }
          if (Seq(N, S, SOURCE).contains(c)) {
            a = (sq.coords * tileSize) + new Point(tileSize.x / 2, 0)
            b = a + new Point(0, tileSize.y)
            ctx.moveTo(a.x, a.y)
            ctx.lineTo(b.x, b.y)
            ctx.stroke()
          }
          if (Seq(NE, SW, SOURCE).contains(c)) {
            a = (sq.coords * tileSize) + new Point(0, tileSize.y)
            b = a + new Point(tileSize.x, -tileSize.y)
            ctx.moveTo(a.x, a.y)
            ctx.lineTo(b.x, b.y)
            ctx.stroke()
          }
          if (Seq(W, E, SOURCE).contains(c)) {
            a = (sq.coords * tileSize) + new Point(0, tileSize.y / 2)
            b = a + new Point(tileSize.x, 0)
            ctx.moveTo(a.x, a.y)
            ctx.lineTo(b.x, b.y)
            ctx.stroke()
          }
          ctx.closePath()
        })
      })
    })

  }

  def redrawStatusOverlay() {
    val ctx = statusCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    ctx.fillStyle = Color.Silver
    ctx.fillRect(0, 0, statusCanvas.width, statusCanvas.height)
  }

  def redrawMouseOverlay(mouse: Point) {

    def getGameSquare(mouse: Point): GameSquare = {
      val np = uiSize - (uiSize - mouse)
      val abs = (np - (np % tileSize)) / tileSize
      val index = abs.toLinear(RuleRepository.squareX)
      val sq = game.gameSquares(index)
      dom.console.log(s"unit:${sq.unit.char} com:${sq.com.map(_._2.toString).mkString(",")}")
      sq
    }

    def drawSquareHighlight(sq: GameSquare) = {
      val ctx = gameOverlay.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
      ctx.globalAlpha = 0.5
      ctx.clearRect(0, 0, uiSize.x, uiSize.y)
      ctx.fillStyle = Color.Highlight
      val np = sq.coords * tileSize
      ctx.fillRect(np.x, np.y, tileSize.x, tileSize.y)
    }

    def drawSquareStatusInfo(sq: GameSquare) = {

      val ctx = statusCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
      redrawStatusOverlay

      val boxSize = tileSize * 1.5
      val margin = 20
      val p = new Point(margin, margin)

      val terrain: HTMLImageElement = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
      terrain.src = Loader.getTileUrl(sq.terrain)
      terrain.onload = (e: dom.Event) => {
        ctx.drawImage(terrain, p.x, p.y, boxSize.x, boxSize.y)

        if (!sq.unit.equals(VoidTile)) {
          val unit: HTMLImageElement = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
          unit.src = Loader.getTileUrl(sq.unit)
          unit.onload = (e: dom.Event) => {
            ctx.drawImage(unit, p.x, p.y, boxSize.x, boxSize.y)
            val txtp = p + new Point(0, boxSize.y + margin)
            ctx.strokeText(
              s"""
                 |unit: ${sq.unit.char}
                 |terrain: ${sq.terrain.char}
                 |attack: ${sq.unit.attack}
                 |defense: ${sq.unit.defense}
                 |movement: ${sq.unit.speed}
               """, txtp.x, txtp.y, statusSize.x - margin)

          }
        }
      }
    }

    val currentSq = getGameSquare(mouse)
    if (currentSq != curStatusSquare) {
      drawSquareStatusInfo(currentSq)
      drawSquareHighlight(currentSq)
      curStatusSquare = currentSq
    }

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
    statusSize = new Point(statusCanvas.width, statusCanvas.height)
    redrawGame()
    redrawStatusOverlay()
  }

}
