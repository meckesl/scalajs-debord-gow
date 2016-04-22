package com.lms.gow.ui

import com.lms.gow.io.Loader
import com.lms.gow.model.repo.PlayerRepository._
import com.lms.gow.model.repo.TileRepository.VoidTile
import com.lms.gow.model.repo.{RuleRepository, TileRepository}
import com.lms.gow.model.{Game, GameSquare, Point}
import org.scalajs.dom
import org.scalajs.dom.html.Canvas

import scala.collection.mutable

case class UiController(game: Game, backgroundCanvas: Canvas, comCanvas: Canvas, terrainCanvas: Canvas, unitCanvas: Canvas, overlayCanvas: Canvas, interfaceCanvas: Canvas) {

  val uiBackround = new UiLayer(backgroundCanvas)
  val uiCom = new UiLayer(comCanvas)
  val uiTerrain = new UiLayer(terrainCanvas)
  val uiUnits = new UiLayer(unitCanvas)
  val uiOverlay = new UiLayer(overlayCanvas)

  val boardDimensions = new Point(RuleRepository.squareX, RuleRepository.squareY)
  def tileSize = uiSize / boardDimensions
  var squareHover: GameSquare = null
  var squareClicked: GameSquare = null
  var squareMoved: GameSquare = null
  var mouseDown = false

  var uiSize = new Point(terrainCanvas.width, terrainCanvas.height)
  var interfaceSize = new Point(interfaceCanvas.width, interfaceCanvas.height)

  def drawInterface() {
    val ctx = interfaceCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    ctx.fillStyle = Color.Silver
    ctx.strokeStyle = Color.Gray
    ctx.lineWidth = 5
    ctx.shadowBlur = 10
    ctx.shadowColor = Color.Gray
    ctx.shadowOffsetX = 10
    ctx.shadowOffsetY = 10
    ctx.beginPath
    ctx.rect(0, 0, interfaceCanvas.width, interfaceCanvas.height)
    ctx.fill
    ctx.stroke
    ctx.closePath
  }

  def squareStatus(sq: GameSquare) = {

    val ctx = interfaceCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    drawInterface()

    val boxSize = tileSize * 3
    val margin = 20
    val p = new Point(margin, margin)

    Loader.getTileAsync(sq.terrain, terrain => {
      ctx.drawImage(terrain, p.x, p.y, boxSize.x, boxSize.y)
      if (!sq.unit.equals(VoidTile)) {
        Loader.getTileAsync(sq.unit, unit => {
          ctx.drawImage(unit, p.x, p.y, boxSize.x, boxSize.y)
          if (!sq.unit.player.equals(Neutral)) {
            ctx.fillStyle = Color.fromPlayer(sq.unit.player)
            ctx.fillRect(
              p.x + (boxSize.x / 20),
              p.y + (boxSize.y - boxSize.y / 12),
              boxSize.x - (boxSize.x / 20), boxSize.y / 12)
          }
          val txtp = p + new Point(0, boxSize.y + margin)
          ctx.closePath
          ctx.lineWidth = 1
          ctx.strokeStyle = "rgb(0,0,0)"
          ctx.strokeText(
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
                 com: ${
              sq.com(sq.unit.player).mkString(",")
            }
               """, txtp.x, txtp.y, interfaceSize.x - margin)
        })
      }
    })
  }

  def getGameSquare(p: Point): GameSquare = {
    val corrected = (p - (p % tileSize)) / tileSize
    val index = corrected.toLinear(RuleRepository.squareX)
    game.gameSquares(index)
  }

  def onResize(s: Point) {
    uiSize = s
    backgroundCanvas.width = uiSize.x.toInt
    backgroundCanvas.height = uiSize.y.toInt
    comCanvas.width = uiSize.x.toInt
    comCanvas.height = uiSize.y.toInt
    terrainCanvas.width = uiSize.x.toInt
    terrainCanvas.height = uiSize.y.toInt
    unitCanvas.width = uiSize.x.toInt
    unitCanvas.height = uiSize.y.toInt
    overlayCanvas.width = uiSize.x.toInt
    overlayCanvas.height = uiSize.y.toInt
    interfaceSize = uiSize / new Point(3, 5)
    interfaceCanvas.width = interfaceSize.x.toInt
    interfaceCanvas.height = interfaceSize.y.toInt

    0 until RuleRepository.squareCount foreach (uiBackround.tileBackground(_))

    uiTerrain.clearLayer()
    TileRepository.terrains.foreach(t => {
      Loader.getTileAsync(t, image => {
        game.gameSquares.filter(_.terrain.equals(t)).foreach(sq => {
          uiTerrain.tileTerrain(sq, image)
        })
      })
    })

    uiUnits.clearLayer()
    TileRepository.units.foreach(u => {
      Loader.getTileAsync(u, image => {
        game.gameSquares.filter(_.unit.equals(u)).foreach(sq => {
          uiUnits.tileUnit(sq, image)
        })
      })
    })

    uiCom.clearLayer()
    game.gameSquares.foreach(sq => {
      uiCom.tileCommunication(sq)
    })

    drawInterface()

    if (null != squareClicked) {
      uiOverlay.tileUnitHighlight(squareClicked)
      squareStatus(squareClicked)
    }
  }

  def onMousemove(e: dom.MouseEvent) {

    val curSq = getGameSquare(new Point(e.clientX, e.clientY))

    if (null == squareMoved || squareMoved == curSq) {
      if (curSq != squareHover) {
        // No mouse drag
        uiOverlay.clearLayer()
        uiOverlay.tileUnitHighlight(curSq)
        curSq.alliesInRange().foreach(uiOverlay.tileHighlight(_, 0.1, Color.Green))
        curSq.targetsInAttackRange().foreach(uiOverlay.tileHighlight(_, 0.1, Color.Red))
        squareHover = curSq
      }
    } else {
      // Mouse drag
      uiOverlay.clearLayer()
      uiOverlay.tileUnitHighlight(squareMoved)
      uiOverlay.drawActionArrow(squareMoved, curSq)
      curSq.canBeTargetOf().foreach(uiOverlay.tileHighlight(_, 0.1, Color.Green))
      curSq.alliesInRange().foreach(uiOverlay.tileHighlight(_, 0.1, Color.Red))

      if (squareMoved.canAttack(curSq))
        uiOverlay.tileHighlight(curSq, 0.3, Color.Red)

      squareHover = curSq
    }
  }

  def isCurrentTurn(sq: GameSquare) = game.turnPlayer.equals(sq.unit.player)



  def onClick(e: dom.MouseEvent) {

    uiOverlay.clearLayer()
    if (isCurrentTurn(squareHover)) {
      squareClicked = squareHover
      uiOverlay.tileUnitHighlight(squareClicked)
      squareStatus(squareClicked)
    }

  }

  def onMouseup(e: dom.MouseEvent) {
    if (null != squareMoved && squareMoved.canMoveTo(squareHover)) {
      squareMoved.moveUnitTo(squareHover)

      uiUnits.clearLayer()
      TileRepository.units.foreach(u => {
        Loader.getTileAsync(u, image => {
          game.gameSquares.filter(_.unit.equals(u)).foreach(sq => {
            uiUnits.tileUnit(sq, image)
          })
        })
      })

      uiCom.clearLayer()
      game.gameSquares.foreach(sq => {
        uiCom.tileCommunication(sq)
      })
    }
    squareMoved = null
  }

  def onMousedown(e: dom.MouseEvent) {
    val mouse = new Point(e.clientX, e.clientY)
    if (getGameSquare(mouse).canMove) {
      squareMoved = getGameSquare(mouse)
      squareStatus(squareMoved)
    }
    else
      squareMoved = null
  }

}
