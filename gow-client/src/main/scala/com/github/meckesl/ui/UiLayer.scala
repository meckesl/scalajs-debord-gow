package com.github.meckesl.ui

import com.github.meckesl.io.Loader
import com.github.meckesl.repo.CardinalityRepository._
import com.github.meckesl.repo.{CardinalityRepository, RuleRepository}
import com.github.meckesl.{Point, Square}
import com.github.meckesl.repo.PlayerRepository.Neutral
import com.github.meckesl.repo.TileRepository.VoidTile
import org.scalajs.dom
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.HTMLImageElement

class UiLayer(canvas: Canvas) {

  private val l = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  private val boardSize = new Point(RuleRepository.squareX, RuleRepository.squareY)
  def size = new Point(canvas.width, canvas.height)
  def tileSize: Point = size / boardSize

  def clearTile(sq: Square): Unit = {
    val p: Point = sq.coords * tileSize
    l.clearRect(p.x, p.y, tileSize.x, tileSize.y)
  }

  def clearLayer(): Unit = {
    l.clearRect(0, 0, size.x, size.y)
  }

  def tileBackground(index: Int): Unit = {
    val bg = Point.fromLinear(index, RuleRepository.squareX)
    if (index % 2 == 0) l.fillStyle = Color.Silver else l.fillStyle = Color.White
    l.globalAlpha = 0.5
    l.fillRect(bg.x * tileSize.x, bg.y * tileSize.y, tileSize.x, tileSize.y)
  }

  def tileTerrain(sq: Square, image: HTMLImageElement): Unit = {
    val te: Point = sq.coords * tileSize
    l.shadowBlur = 12
    l.shadowColor = Color.Gray
    l.shadowOffsetX = 7
    l.shadowOffsetY = 0
    l.drawImage(image, te.x, te.y, tileSize.x, tileSize.y)
  }

  private def hasUnitMovedEastwards(sq: Square) =
    (sq.lastMoveDir.equals(CardinalityRepository.SOURCE) && sq.coords.x < boardSize.x/2) ||
      sq.lastMoveDir.equals(CardinalityRepository.E)

  def tileUnit(sq: Square, image: HTMLImageElement = null): Unit = {
    val u: Point = sq.coords * tileSize
    tileUnitMovementBar(sq)
    l.save()
    l.shadowBlur = 5
    l.shadowColor = Color.Gray
    l.shadowOffsetX = 5
    l.shadowOffsetY = 5
    if (!sq.isOnline)
      l.globalAlpha = 0.3
    var computedX = u.x
    if (hasUnitMovedEastwards(sq)){
      l.translate(tileSize.x, 0)
      l.scale(-1, 1)
      computedX = -u.x
    }
    if (null == image) {
      l.drawImage(Loader.imageCache(sq.unit), computedX, u.y, tileSize.x, tileSize.y)
    } else {
      l.drawImage(image, computedX, u.y, tileSize.x, tileSize.y)
    }
    l.setTransform(1, 0, 0, 1, 0, 0)
    l.restore()
  }

  def tileUnitHighlight(sq: Square): Unit = {
    l.save()
    val u: Point = sq.coords * tileSize
    l.shadowBlur = 0
    l.shadowColor = Color.Highlight
    l.shadowOffsetX = 2
    l.shadowOffsetY = 0
    var computedX = u.x
    if (hasUnitMovedEastwards(sq)) {
      l.translate(tileSize.x, 0)
      l.scale(-1, 1)
      computedX = -u.x
    }
    l.drawImage(Loader.imageCache(sq.unit), computedX, u.y, tileSize.x, tileSize.y)
    l.restore()
  }

  private def tileUnitMovementBar(sq: Square): Unit = {
    if (sq.canMove) {
      val u: Point = sq.coords * tileSize
      l.fillStyle = Color.fromPlayer(sq.unit.player)
      l.beginPath()
      l.ellipse(
        u.x + tileSize.x / 2,
        u.y + (tileSize.y - tileSize.y / 20),
        tileSize.x / 2,
        tileSize.y / 20,
        0, 0, Math.PI * 2)
      l.fill()
    }
  }

  def tileCommunication(sq: Square): Unit = {

    class sqCoords(sq: Square) {
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

    val co = new sqCoords(sq)

    def drawLine(a: Point, b: Point, lineWidth: Int = 1): Unit = {
      //l.setLineDash(js.Array(5.0, 15.0))
      l.globalAlpha = 0.6
      l.lineWidth=lineWidth
      l.beginPath()
      l.moveTo(a.x, a.y)
      l.lineTo(b.x, b.y)
      l.stroke()
      l.closePath()
      l.globalAlpha = 1
    }

    sq.com.foreach(com => {
      l.strokeStyle = Color fromPlayer com._1
      com._2.foreach {
        case N => drawLine(co.source, co.n)
        case NE => drawLine(co.source, co.ne)
        case E => drawLine(co.source, co.e)
        case SE => drawLine(co.source, co.se)
        case S => drawLine(co.source, co.s)
        case SW => drawLine(co.source, co.sw)
        case W => drawLine(co.source, co.w)
        case NW => drawLine(co.source, co.nw)
        case SOURCE => drawLine(co.n, co.s)
                       drawLine(co.e, co.w)
                       drawLine(co.ne, co.sw)
                       drawLine(co.se, co.nw)
        case _ =>
      }
    })
  }

  def tileHighlight(sq: Square, alpha: Double, color: String): Unit = {
    l.save()
    l.globalAlpha = alpha
    l.fillStyle = color
    val a = sq.coords * tileSize
    l.beginPath()
    l.ellipse(a.x + tileSize.x / 2, a.y + tileSize.y / 2, tileSize.x / 2, tileSize.y / 2, 0, 0, Math.PI * 2)
    l.fill()
    l.closePath()
    l.restore()
  }

  def drawActionArrow(source: Square, dest: Square): Unit = {

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

    val headlen = 10 // length of head in pixels
    val angle = Math.atan2(to.y - from.y, to.x - from.x)
    l.lineJoin = "miter" // set the lineJoint to miter
    l.lineTo(to.x, to.y)
    l.stroke()
    l.save()
    // to draw the triangle at the end of line we move to end of line and rotate the context
    l.translate(to.x, to.y)
    l.rotate(angle + Math.PI / 2) // rotate angle modified
    l.beginPath()
    l.moveTo(0, -headlen)
    l.lineTo(headlen, headlen)
    l.lineTo(-headlen, headlen)
    l.closePath()
    l.fillStyle = l.strokeStyle // coloring the triangle same as stroke color
    l.fill()

    l.restore()
  }

  def interfaceAttackPanel(sq: Square): Unit = {

    val tileSizeB = tileSize * 2

    def drawUnit(sq: Square, p: Point): Unit = {
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

    l.strokeText(s"Attack: ${sq.canBeTargetOfStrength}", tileSizeB.x + 10, tileSizeB.y, canvas.width)
    l.strokeText(s"Defence: ${sq.defenseStrength}", canvas.width - (tileSizeB.x * 4), tileSizeB.y, canvas.width)

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
    l.beginPath()
    l.rect(0, 0, canvas.width, canvas.height)
    l.fill()
    l.stroke()
    l.closePath()
  }

  def interfaceTileStatus(sq: Square): Unit = {

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
          l.closePath()
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
