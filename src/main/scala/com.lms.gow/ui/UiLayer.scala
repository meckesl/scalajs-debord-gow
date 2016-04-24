package com.lms.gow.ui

import com.lms.gow.io.Loader
import com.lms.gow.model.repo.CardinalityRepository._
import com.lms.gow.model.repo.PlayerRepository.Neutral
import com.lms.gow.model.repo.RuleRepository
import com.lms.gow.model.repo.TileRepository.VoidTile
import com.lms.gow.model.{GameSquare, Point}
import org.scalajs.dom
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.HTMLImageElement

class UiLayer(canvas: Canvas) {

  val l = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  val boardSize = new Point(RuleRepository.squareX, RuleRepository.squareY)
  def size = new Point(canvas.width, canvas.height)
  def tileSize = size / boardSize

  def clearTile(sq: GameSquare) {
    val p: Point = sq.coords * tileSize
    l.clearRect(p.x, p.y, tileSize.x, tileSize.y)
  }

  def clearLayer() {
    l.clearRect(0, 0, size.x, size.y)
  }

  def tileBackground(index: Int) {
    val bg = Point.fromLinear(index, RuleRepository.squareX)
    if (index % 2 == 0) l.fillStyle = Color.Silver else l.fillStyle = Color.White
    l.fillRect(bg.x * tileSize.x, bg.y * tileSize.y, tileSize.x, tileSize.y)
  }

  def tileTerrain(sq: GameSquare, image: HTMLImageElement) {
    val te: Point = sq.coords * tileSize
    l.shadowBlur = 12
    l.shadowColor = Color.Gray
    l.shadowOffsetX = 7
    l.shadowOffsetY = 0
    l.drawImage(image, te.x, te.y, tileSize.x, tileSize.y)
  }

  def tileUnit(sq: GameSquare, image: HTMLImageElement = null) {
    val u: Point = sq.coords * tileSize
    l.save()
    l.shadowBlur = 5
    l.shadowColor = Color.Gray
    l.shadowOffsetX = 5
    l.shadowOffsetY = 5
    if (!sq.isOnline)
      l.globalAlpha = 0.3
    if (null == image)
      l.drawImage(Loader.imageCache.get(sq.unit).get, u.x, u.y, tileSize.x, tileSize.y)
    else
      l.drawImage(image, u.x, u.y, tileSize.x, tileSize.y)
    tileUnitMovementBar(sq)
    l.restore()
  }

  def tileUnitMovementBar(sq: GameSquare) {
    val u: Point = sq.coords * tileSize
    l.fillStyle = Color.fromPlayer(sq.unit.player)
    if (sq.canMove) {
      l.fillRect(
        u.x + (tileSize.x / 20),
        u.y + (tileSize.y - tileSize.y / 12),
        tileSize.x - (tileSize.x / 20), tileSize.y / 12)
    } else {
      l.fillRect(
        u.x + (tileSize.x / 20),
        u.y + (tileSize.y - tileSize.y / 12),
        (tileSize.x / 15), tileSize.y / 12)
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

  def tileCommunication(sq: GameSquare) = {

    val co = new sqCoords(sq)

    def drawLine(a: Point, b: Point) {
      l.globalAlpha = 0.5

      l.beginPath()
      l.moveTo(a.x, a.y)
      l.lineTo(b.x, b.y)
      l.stroke()
      l.closePath()
      l.globalAlpha = 1
    }

    sq.com.foreach(com => {
      l.strokeStyle = Color fromPlayer com._1
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

  def tileUnitHighlight(sq: GameSquare) {
    l.save()
    val u: Point = sq.coords * tileSize
    l.shadowBlur = 0
    l.shadowColor = Color.Highlight
    l.shadowOffsetX = 2
    l.shadowOffsetY = 0
    l.drawImage(Loader.imageCache.get(sq.unit).get, u.x, u.y, tileSize.x, tileSize.y)
    l.restore()
  }

  def tileHighlight(sq: GameSquare, alpha: Double, color: String) = {
    l.save()
    l.globalAlpha = alpha
    l.fillStyle = color
    val a = sq.coords * tileSize
    l.beginPath
    l.rect(a.x, a.y, tileSize.x, tileSize.y)
    l.fill
    l.closePath
    l.restore()
  }

  def drawActionArrow(source: GameSquare, dest: GameSquare) {
    val from = source.coords * tileSize + tileSize / 2
    val to = dest.coords * tileSize + tileSize / 2
    if (source.canMoveTo(dest))
      l.strokeStyle = Color.Green
    else if (source.canAttack(dest))
      l.strokeStyle = Color.Red
    else
      l.strokeStyle = Color.Gray
    l.lineWidth = 10
    l.beginPath()
    l.moveTo(from.x, from.y)
    l.bezierCurveTo(from.x, to.y, to.x, to.y, to.x, to.y)
    l.stroke()
  }

  def interfaceAttackPanel(sq: GameSquare) {

    val tileSizeB = tileSize * 3

    def drawUnit(sq: GameSquare, p: Point) = {
      Loader.getTileAsync(sq.terrain, terrain => {
        l.drawImage(terrain, p.x, p.y, tileSizeB.x, tileSizeB.y)
        if (!sq.unit.equals(VoidTile)) {
          Loader.getTileAsync(sq.unit, unit => {
            l.drawImage(unit, p.x, p.y, tileSizeB.x, tileSizeB.y)
            if (!sq.unit.player.equals(Neutral)) {
              l.fillStyle = Color.fromPlayer(sq.unit.player)
              l.fillRect(
                p.x + (tileSizeB.x / 20),
                p.y + (tileSizeB.y - tileSizeB.y / 12),
                tileSizeB.x - (tileSizeB.x / 20), tileSizeB.y / 12)
            }
          })
        }
      })
    }

    clearLayer()
    interfacePanel()

    val attackers = sq.canBeTargetOf
    attackers.zipWithIndex.foreach(a =>{
      drawUnit(a._1, new Point(0, tileSizeB.y * a._2))
    })

    val defenders = sq.alliesInRange
    defenders.zipWithIndex.foreach(a =>{
      drawUnit(a._1, new Point((size - tileSizeB).x, tileSizeB.y * a._2))
    })

    val as = sq.canBeTargetOfStrength
    val ds = sq.defenseStrength
    val result = as - ds



  }

  private def interfacePanel() {
    l.fillStyle = Color.Silver
    l.strokeStyle = Color.Gray
    l.lineWidth = 5
    l.shadowBlur = 10
    l.shadowColor = Color.Gray
    l.shadowOffsetX = 10
    l.shadowOffsetY = 10
    l.beginPath
    l.rect(0, 0, canvas.width, canvas.height)
    l.fill
    l.stroke
    l.closePath
  }

  def interfaceTileStatus(sq: GameSquare) = {

    clearLayer()
    interfacePanel()

    val tileSizeB = tileSize * 3 * 3
    val p = tileSize

    Loader.getTileAsync(sq.terrain, terrain => {
      l.drawImage(terrain, p.x, p.y, tileSizeB.x, tileSizeB.y)
      if (!sq.unit.equals(VoidTile)) {
        Loader.getTileAsync(sq.unit, unit => {
          l.drawImage(unit, p.x, p.y, tileSizeB.x, tileSizeB.y)
          if (!sq.unit.player.equals(Neutral)) {
            l.fillStyle = Color.fromPlayer(sq.unit.player)
            l.fillRect(
              p.x + (tileSizeB.x / 20),
              p.y + (tileSizeB.y - tileSizeB.y / 12),
              tileSizeB.x - (tileSizeB.x / 20), tileSizeB.y / 12)
          }
          val txtp = p + new Point(0, tileSizeB.y + p.y)
          l.closePath
          l.lineWidth = 1
          l.strokeStyle = "rgb(0,0,0)"
          l.strokeText(
            s"""
                 unit: ${
              sq.unit.char
            }
                 terrain: ${
              sq.terrain.char
            }
                 attack: ${
              sq.unit.attack
            }
                 defense: ${
              sq.unit.defense
            }
                 movement: ${
              sq.unit.speed
            }
               """, txtp.x, txtp.y, canvas.width - p.x)
        })
      }
    })
  }

}
