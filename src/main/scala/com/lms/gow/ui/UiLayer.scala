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

  private val l = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  private val boardSize = new Point(RuleRepository.squareX, RuleRepository.squareY)
  def size = new Point(canvas.width, canvas.height)
  def tileSize: Point = size / boardSize

  def clearTile(sq: GameSquare): Unit = {
    val p: Point = sq.coords * tileSize
    l.clearRect(p.x, p.y, tileSize.x, tileSize.y)
  }

  def clearLayer(): Unit = {
    l.clearRect(0, 0, size.x, size.y)
  }

  def tileBackground(index: Int): Unit = {
    val bg = Point.fromLinear(index, RuleRepository.squareX)
    if (index % 2 == 0) l.fillStyle = Color.Silver else l.fillStyle = Color.White
    l.fillRect(bg.x * tileSize.x, bg.y * tileSize.y, tileSize.x, tileSize.y)
  }

  def tileTerrain(sq: GameSquare, image: HTMLImageElement): Unit = {
    val te: Point = sq.coords * tileSize
    l.shadowBlur = 12
    l.shadowColor = Color.Gray
    l.shadowOffsetX = 7
    l.shadowOffsetY = 0
    l.drawImage(image, te.x, te.y, tileSize.x, tileSize.y)
  }

  def tileUnit(sq: GameSquare, image: HTMLImageElement = null): Unit = {
    val u: Point = sq.coords * tileSize
    l.save()
    l.shadowBlur = 5
    l.shadowColor = Color.Gray
    l.shadowOffsetX = 5
    l.shadowOffsetY = 5
    if (!sq.isOnline)
      l.globalAlpha = 0.3
    if (null == image)
      l.drawImage(Loader.imageCache(sq.unit), u.x, u.y, tileSize.x, tileSize.y)
    else
      l.drawImage(image, u.x, u.y, tileSize.x, tileSize.y)
    tileUnitMovementBar(sq)
    l.restore()
  }

  private def tileUnitMovementBar(sq: GameSquare): Unit = {
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
        tileSize.x / 15, tileSize.y / 12)
    }
  }

  private class sqCoords(sq: GameSquare) {
    val nw: Point = sq.coords * tileSize
    val se: Point = nw + tileSize
    val n: Point = nw + new Point(tileSize.x / 2, 0)
    val s: Point = se - new Point(tileSize.x / 2, 0)
    val ne: Point = nw + new Point(tileSize.x, 0)
    val sw: Point = se - new Point(tileSize.x, 0)
    val e: Point = ne + new Point(0, tileSize.y / 2)
    val w: Point = sw - new Point(0, tileSize.y / 2)
    val source: Point = n + new Point(0, tileSize.y / 2)
  }

  def tileCommunication(sq: GameSquare): Unit = {

    val co = new sqCoords(sq)

    def drawLine(a: Point, b: Point): Unit = {
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
      com._2 foreach {
        case N => drawLine(co.source, co.n)
        case NE => drawLine(co.source, co.ne)
        case E => drawLine(co.source, co.e)
        case SE => drawLine(co.source, co.se)
        case S => drawLine(co.source, co.s)
        case SW => drawLine(co.source, co.sw)
        case W => drawLine(co.source, co.w)
        case NW => drawLine(co.source, co.nw)
        case SOURCE =>  drawLine(co.source, co.n)
                        drawLine(co.source, co.ne)
                        drawLine(co.source, co.e)
                        drawLine(co.source, co.se)
                        drawLine(co.source, co.s)
                        drawLine(co.source, co.sw)
                        drawLine(co.source, co.w)
                        drawLine(co.source, co.nw)
      }
    })
  }

  def tileUnitHighlight(sq: GameSquare): Unit = {
    l.save()
    val u: Point = sq.coords * tileSize
    l.shadowBlur = 0
    l.shadowColor = Color.Highlight
    l.shadowOffsetX = 2
    l.shadowOffsetY = 0
    l.drawImage(Loader.imageCache(sq.unit), u.x, u.y, tileSize.x, tileSize.y)
    l.restore()
  }

  def tileHighlight(sq: GameSquare, alpha: Double, color: String): Unit = {
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

  def drawActionArrow(source: GameSquare, dest: GameSquare): Unit = {
    val from = source.coords * tileSize + tileSize / 2
    val to = dest.coords * tileSize + tileSize / 2
    if (source.canMoveTo(dest))
      l.strokeStyle = Color.Green
    else if (source.canTakeArsenal(dest))
      l.strokeStyle = Color.Orange
    else if (source.canAttack(dest))
      l.strokeStyle = Color.Red
    else
      l.strokeStyle = Color.Gray
    l.lineWidth = 8
    l.beginPath()
    l.moveTo(from.x, from.y)
    l.bezierCurveTo(from.x, to.y, to.x, to.y, to.x, to.y)
    l.stroke()
  }

  def interfaceAttackPanel(sq: GameSquare): Unit = {

    val tileSizeB = tileSize * 2

    def drawUnit(sq: GameSquare, p: Point): Unit = {
      Loader.loadTileAsync(sq.terrain, terrain => {
        l.drawImage(terrain, p.x, p.y, tileSizeB.x, tileSizeB.y)
        if (!sq.unit.equals(VoidTile)) {
          Loader.loadTileAsync(sq.unit, unit => {
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

    l.strokeStyle = Color.Black
    l.lineWidth = 1
    l.shadowBlur = 0
    l.shadowOffsetX = 0
    l.shadowOffsetY = 0

    val attackers = sq.canBeTargetOf
    attackers.zipWithIndex.foreach(a => {
      drawUnit(a._1, new Point(0, tileSizeB.y * a._2))
      //l.strokeText(s"+${a._1.unit.attack}", tileSizeB.x + 10, tileSizeB.y * a._2)
    })

    val defenders = sq.alliesInRange.toSeq
    defenders.zipWithIndex.foreach(a => {
      drawUnit(a._1, new Point((size - tileSizeB).x, tileSizeB.y * a._2))
    })

    l.strokeText(s"Attaque: ${sq.canBeTargetOfStrength}", tileSizeB.x + 10, tileSizeB.y, canvas.width)
    l.strokeText(s"Défense: ${sq.defenseStrength}", canvas.width - (tileSizeB.x * 4), tileSizeB.y, canvas.width)

    val as = sq.canBeTargetOfStrength
    val ds = sq.defenseStrength
    val result = as - ds

  }

  private def interfacePanel(): Unit = {
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

  def interfaceTileStatus(sq: GameSquare): Unit = {

    clearLayer()
    interfacePanel()

    val tileSizeB = tileSize * 3 * 3
    val p = tileSize

    Loader.loadTileAsync(sq.terrain, terrain => {
      l.drawImage(terrain, p.x, p.y, tileSizeB.x, tileSizeB.y)
      if (!sq.unit.equals(VoidTile)) {
        Loader.loadTileAsync(sq.unit, unit => {
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
