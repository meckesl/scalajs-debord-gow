package com.lms.gow.ui

import com.lms.gow.io.Loader
import com.lms.gow.model.repo.CardinalityRepository._
import com.lms.gow.model.repo.{RuleRepository, TileRepository}
import com.lms.gow.model.repo.TileRepository.Tile
import com.lms.gow.model.{GameSquare, Point}
import org.scalajs.dom
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.HTMLImageElement

import scala.collection.mutable

class UiTile(canvas: Canvas) {

  val boardSize = new Point(RuleRepository.squareX, RuleRepository.squareY)
  def size = new Point(canvas.width, canvas.height)
  def tileSize = size / boardSize

  val imageCache: mutable.HashMap[Tile, HTMLImageElement] = new mutable.HashMap()

  /*TileRepository.units.foreach(t => {
      Loader.getTileAsync(t, image => {
        game.gameSquares.filter(_.unit.equals(t)).foreach { sq =>
          uiUnits.drawUnit(sq, image)
        }
      })
    })*/


  val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  def resetStyle() {
    ctx.restore
    ctx.save
  }

  def clear(sq: GameSquare) {
    val p: Point = sq.coords * tileSize
    ctx.clearRect(p.x, p.y, tileSize.x, tileSize.y)
  }

  def clearAll() {
    resetStyle
    ctx.clearRect(0, 0, size.x, size.y)
  }

  def drawBackground(index: Int) = {
    val bg = Point.fromLinear(index, RuleRepository.squareX)
    if (index % 2 == 0) ctx.fillStyle = Color.Silver else ctx.fillStyle = Color.White
    ctx.fillRect(bg.x * tileSize.x, bg.y * tileSize.y, tileSize.x, tileSize.y)
  }

  def drawTerrain(sq: GameSquare, image: HTMLImageElement) = {
    val te: Point = sq.coords * tileSize
    ctx.shadowBlur = 12
    ctx.shadowColor = Color.Gray
    ctx.shadowOffsetX = 7
    ctx.shadowOffsetY = 0
    ctx.drawImage(image, te.x, te.y, tileSize.x, tileSize.y)
  }

  def drawUnit(sq: GameSquare) {
    if (imageCache.get(sq.unit).isDefined)
      drawUnit(sq, imageCache.get(sq.unit).get)
    else
      Loader.getTileAsync(sq.unit, image => {
        imageCache put(sq.unit, image)
        drawUnit(sq, image)
      })
  }

  def drawUnit(sq: GameSquare, image: HTMLImageElement) {
    val u: Point = sq.coords * tileSize
    ctx.shadowBlur = 5
    ctx.shadowColor = Color.Gray
    ctx.shadowOffsetX = 5
    ctx.shadowOffsetY = 5
    // Unit image
    if (!sq.isOnline)
      ctx.globalAlpha = 0.3
    ctx.drawImage(image, u.x, u.y, tileSize.x, tileSize.y)
    ctx.globalAlpha = 1
    // Movement bar
    ctx.fillStyle = Color.fromPlayer(sq.unit.player)
    if (sq.canMove) {
      ctx.fillRect(
        u.x + (tileSize.x / 20),
        u.y + (tileSize.y - tileSize.y / 12),
        tileSize.x - (tileSize.x / 20), tileSize.y / 12)
    } else {
      ctx.fillRect(
        u.x + (tileSize.x / 20),
        u.y + (tileSize.y - tileSize.y / 12),
        (tileSize.x / 20), tileSize.y / 12)
    }
  }

  def drawCommunication(sq: GameSquare) = {
    sq.com.foreach(com => {
      ctx.strokeStyle = Color.fromPlayer(com._1)
      com._2.foreach(c => {
        var a: Point = null
        var b: Point = null
        def drawLine(a: Point, b: Point) = {
          ctx.globalAlpha = 0.5
          ctx.beginPath()
          ctx.moveTo(a.x, a.y)
          ctx.lineTo(b.x, b.y)
          ctx.stroke()
          ctx.closePath()
          ctx.globalAlpha = 1
        }
        if (Seq(NW, SE, SOURCE).contains(c)) {
          a = sq.coords * tileSize
          b = a + tileSize
          drawLine(a, b)
        }
        if (Seq(N, S, SOURCE).contains(c)) {
          a = (sq.coords * tileSize) + new Point(tileSize.x / 2, 0)
          b = a + new Point(0, tileSize.y)
          drawLine(a, b)
        }
        if (Seq(NE, SW, SOURCE).contains(c)) {
          a = (sq.coords * tileSize) + new Point(0, tileSize.y)
          b = a + new Point(tileSize.x, -tileSize.y)
          drawLine(a, b)
        }
        if (Seq(W, E, SOURCE).contains(c)) {
          a = (sq.coords * tileSize) + new Point(0, tileSize.y / 2)
          b = a + new Point(tileSize.x, 0)
          drawLine(a, b)
        }
      })
    })
  }

  def drawHighlight(sq: GameSquare, alpha: Double) = {
    ctx.globalAlpha = alpha
    ctx.fillStyle = Color.Highlight
    ctx.strokeStyle = Color.Highlight
    ctx.lineWidth = 2
    val a = sq.coords * tileSize
    ctx.beginPath
    ctx.rect(a.x, a.y, tileSize.x, tileSize.y)
    ctx.fill
    ctx.stroke
    ctx.closePath
  }

}
