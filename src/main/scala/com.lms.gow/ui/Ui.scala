package com.lms.gow.ui

import com.lms.gow.io.Loader
import com.lms.gow.model.repo.PlayerRepository._
import com.lms.gow.model.repo.TileRepository.{Tile, VoidTile}
import com.lms.gow.model.repo.{RuleRepository, TileRepository}
import com.lms.gow.model.{Game, GameSquare, Point}
import org.scalajs.dom
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.HTMLImageElement

case class Ui(game: Game, terrainCanvas: Canvas, comCanvas: Canvas, unitCanvas: Canvas, overlayCanvas: Canvas, statusCanvas: Canvas) {

  val uiTerrain = new UiTile(terrainCanvas)
  val uiCom = new UiTile(comCanvas)
  val uiUnits = new UiTile(unitCanvas)
  val uiOverlay = new UiTile(overlayCanvas)

  val boardDimensions = new Point(RuleRepository.squareX, RuleRepository.squareY)
  def tileSize = uiSize / boardDimensions
  var squareHover: GameSquare = null
  var squareClicked: GameSquare = null
  var squareMoved: GameSquare = null
  var mouseDown = false

  var uiSize = new Point(terrainCanvas.width, terrainCanvas.height)
  var statusSize = new Point(statusCanvas.width, statusCanvas.height)

  def getGameSquare(p: Point): GameSquare = {
    val corrected = (p - (p % tileSize)) / tileSize
    val index = corrected.toLinear(RuleRepository.squareX)
    game.gameSquares(index)
  }

  def getTileAsync(t: Tile, callback: (HTMLImageElement) => Unit) {
    val image = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
    image.src = Loader.getTileUrl(t)
    image.onload = (e: dom.Event) => {
      callback(image)
    }
  }

  def drawTerrain() {
    uiTerrain.resetStyle()
    0 until RuleRepository.squareCount foreach { index =>
      uiTerrain.drawBackground(index)
    }
    TileRepository.terrains.foreach(t => {
      getTileAsync(t, image => {
        game.gameSquares.filter(_.terrain.equals(t)).foreach { sq =>
          uiTerrain.drawTerrain(sq, image)
        }
      })
    })
  }

  def drawUnits() {
    uiUnits.resetStyle()
    TileRepository.units.foreach(t => {
      getTileAsync(t, image => {
        game.gameSquares.filter(_.unit.equals(t)).foreach { sq =>
          uiUnits.drawUnit(sq, image)
        }
      })
    })
  }

  def drawCom() {
    uiCom.clearAll()
    game.gameSquares.foreach { sq =>
      uiCom.drawCommunication(sq)
    }
  }

  def drawStatus() {
    val ctx = statusCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    ctx.fillStyle = Color.Silver
    ctx.strokeStyle = Color.Gray
    ctx.lineWidth = 5
    ctx.shadowBlur = 10
    ctx.shadowColor = Color.Gray
    ctx.shadowOffsetX = 10
    ctx.shadowOffsetY = 10
    ctx.beginPath
    ctx.rect(0, 0, statusCanvas.width, statusCanvas.height)
    ctx.fill
    ctx.stroke
    ctx.closePath
  }

  def squareStatus(sq: GameSquare) = {

    val ctx = statusCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    drawStatus()

    val boxSize = tileSize * 3
    val margin = 20
    val p = new Point(margin, margin)

    getTileAsync(sq.terrain, terrain => {
      ctx.drawImage(terrain, p.x, p.y, boxSize.x, boxSize.y)
      if (!sq.unit.equals(VoidTile)) {
        getTileAsync(sq.unit, unit => {
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
                 unit: ${sq.unit.char}
                 terrain: ${sq.terrain.char}
                 attack: ${sq.unit.attack}
                 defense: ${sq.unit.defense}
                 movement: ${sq.unit.speed}
                 com: ${sq.com(sq.unit.player).mkString(",")}
               """, txtp.x, txtp.y, statusSize.x - margin)
        })
      }
    })
  }



  def onResize(s: Point): Unit = {
    uiSize = s
    terrainCanvas.width = uiSize.x.toInt
    terrainCanvas.height = uiSize.y.toInt
    comCanvas.width = uiSize.x.toInt
    comCanvas.height = uiSize.y.toInt
    unitCanvas.width = uiSize.x.toInt
    unitCanvas.height = uiSize.y.toInt
    overlayCanvas.width = terrainCanvas.width
    overlayCanvas.height = terrainCanvas.height
    statusSize = uiSize / new Point(3, 5)
    statusCanvas.width = statusSize.x.toInt
    statusCanvas.height = statusSize.y.toInt

    drawTerrain()
    drawCom()
    drawUnits()
    drawStatus()

    if (null != squareClicked) {
      uiOverlay.drawHighlight(squareClicked, 0.3)
      squareStatus(squareClicked)
    }
  }

  def onMousemove(e: dom.MouseEvent) {

    val curSq = getGameSquare(new Point(e.clientX, e.clientY))
    val ctx = overlayCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    if (null == squareMoved || squareMoved == curSq) {
      if (curSq != squareHover) {
        uiOverlay.clearAll()
        uiOverlay.drawHighlight(curSq, 0.1)
        if (null != squareClicked)
          uiOverlay.drawHighlight(squareClicked, 0.3)
        squareHover = curSq
      }
    } else {
      uiOverlay.clearAll()
      uiOverlay.drawHighlight(squareMoved, 0.5)
      val from = squareMoved.coords * tileSize + tileSize / 2
      val to = curSq.coords * tileSize + tileSize / 2

      if (squareMoved.canMoveTo(curSq))
        ctx.strokeStyle = Color.fromPlayer(game.turnPlayer)
      else
        ctx.strokeStyle = Color.Gray
      ctx.lineWidth = 10

      ctx.beginPath()
      ctx.moveTo(from.x, from.y)
      ctx.bezierCurveTo(from.x, to.y, to.x, to.y, to.x, to.y)
      ctx.stroke()
      squareHover = curSq
    }
  }

  def onClick(e: dom.MouseEvent): Unit = {
    squareClicked = squareHover
    uiOverlay.clearAll()
    uiOverlay.drawHighlight(squareHover, 0.3)
    squareStatus(squareHover)
  }

  def onMouseup(e: dom.MouseEvent): Unit = {
    if (null != squareMoved && squareMoved.canMoveTo(squareHover)) {
      squareMoved.moveUnitTo(squareHover)
      //drawUnits()
      drawCom()
      val toClear = squareMoved
      getTileAsync(squareHover.unit, image => {
        uiUnits.clear(toClear)
        uiUnits.drawUnit(squareHover, image)
      })

      //drawStatus()
    }
    squareMoved = null
  }

  def onMousedown(e: dom.MouseEvent): Unit = {
    val mouse = new Point(e.clientX, e.clientY)
    if (getGameSquare(mouse).canMove) {
      squareMoved = getGameSquare(mouse)
      squareStatus(squareMoved)
    }
    else
      squareMoved = null
  }

}
