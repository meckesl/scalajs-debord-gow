package com.lms.gow.ui

import com.lms.gow.io.Loader
import com.lms.gow.model.repo.CardinalityRepository._
import com.lms.gow.model.repo.RuleRepository
import com.lms.gow.model.repo.TileRepository.Tile
import com.lms.gow.model.{GameSquare, Point}
import org.scalajs.dom
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.HTMLImageElement

import scala.collection.mutable

class UiTile(canvas: Canvas) {

  val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  val imageCache: mutable.HashMap[Tile, HTMLImageElement] = new mutable.HashMap()

  val boardSize = new Point(RuleRepository.squareX, RuleRepository.squareY)
  def size = new Point(canvas.width, canvas.height)
  def tileSize = size / boardSize

  def clear(sq: GameSquare) {
    val p: Point = sq.coords * tileSize
    ctx.clearRect(p.x, p.y, tileSize.x, tileSize.y)
  }

  def clearAll() {
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
    ctx.save()
    ctx.shadowBlur = 5
    ctx.shadowColor = Color.Gray
    ctx.shadowOffsetX = 5
    ctx.shadowOffsetY = 5
    if (!sq.isOnline)
      ctx.globalAlpha = 0.3
    ctx.drawImage(image, u.x, u.y, tileSize.x, tileSize.y)
    ctx.globalAlpha = 1
    drawMovementBar(sq)
    ctx.restore()
  }

  def drawMovementBar(sq: GameSquare): Unit = {
    val u: Point = sq.coords * tileSize
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

  class sqCoords(sq: GameSquare) {
    val nw = sq.coords * tileSize
    val se = nw + tileSize
    val n = nw + new Point(tileSize.x / 2, 0)
    val s = se - new Point(tileSize.x / 2, 0)
    val ne = nw + new Point(tileSize.x, 0)
    val sw = se - new Point(tileSize.x, 0)
    val e = nw + new Point(0, tileSize.y / 2)
    val w = se - new Point(0, tileSize.y / 2)
  }

  def drawCommunication(sq: GameSquare) = {

    val co = new sqCoords(sq)

    def drawLine(a: Point, b: Point) = {
      ctx.globalAlpha = 0.5
      ctx.beginPath()
      ctx.moveTo(a.x, a.y)
      ctx.lineTo(b.x, b.y)
      ctx.stroke()
      ctx.closePath()
      ctx.globalAlpha = 1
    }

    sq.com.foreach(com => {
      ctx.strokeStyle = Color.fromPlayer(com._1)
      com._2.foreach(c => {
        if (Seq(NW, SE, SOURCE).contains(c))
          drawLine(co.nw, co.se)
        if (Seq(N, S, SOURCE).contains(c))
          drawLine(co.n, co.s)
        if (Seq(NE, SW, SOURCE).contains(c))
          drawLine(co.ne, co.sw)
        if (Seq(W, E, SOURCE).contains(c))
          drawLine(co.e, co.w)
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
