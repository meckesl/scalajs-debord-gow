package com.github.meckesl.ui

import com.github.meckesl.io.Loader
import com.github.meckesl.repo.{RuleRepository, TileRepository}
import com.github.meckesl.{Game, Point, Square}
import com.github.meckesl.repo.TileRepository.VoidTile
import org.scalajs.dom
import org.scalajs.dom.{Blob, BlobPropertyBag, HTMLAnchorElement, HTMLAudioElement, URL}
import org.scalajs.dom.html.Canvas

import scala.scalajs.js

case class UiController(game: Game, backgroundCanvas: Canvas, comCanvas: Canvas, terrainCanvas: Canvas, unitCanvas: Canvas, overlayCanvas: Canvas, interfaceCanvas: Canvas) {

  private val uiBackground = new UiLayer(backgroundCanvas)
  private val uiCom = new UiLayer(comCanvas)
  private val uiTerrain = new UiLayer(terrainCanvas)
  private val uiUnits = new UiLayer(unitCanvas)
  private val uiOverlay = new UiLayer(overlayCanvas)
  private val uiInterface = new UiLayer(interfaceCanvas)

  private var squareHover: Square = _
  private var squareClicked: Square = _
  private var squareSource: Square = _

  def tileSize: Point = uiSize / new Point(RuleRepository.squareX, RuleRepository.squareY)
  private var uiSize = new Point(terrainCanvas.width, terrainCanvas.height)
  private var interfaceSize = new Point(interfaceCanvas.width, interfaceCanvas.height)

  private val audioChannel1 = dom.document.createElement("audio").asInstanceOf[HTMLAudioElement]
  private val audioChannel2 = dom.document.createElement("audio").asInstanceOf[HTMLAudioElement]
  private val audioChannel3 = dom.document.createElement("audio").asInstanceOf[HTMLAudioElement]
  Set(audioChannel1, audioChannel2, audioChannel3).foreach(_.volume=0.3)

  audioChannel3.src = Loader.getSoundUrl("start")
  audioChannel3.play()

  private def getGameSquare(p: Point): Square = {
    val corrected = (p - (p % tileSize)) / tileSize
    val index = corrected.toLinear(RuleRepository.squareX)
    game.gameSquares(index)
  }

  private def redraw(): Unit = {
    uiUnits.clearLayer()
    TileRepository.units.foreach(u => {
      Loader.loadTileAsync(u, image => {
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

  def onResize(s: Point): Unit = {
    uiSize = s
    Set(backgroundCanvas, comCanvas, terrainCanvas, unitCanvas, overlayCanvas).foreach(c=>{
      c.width = uiSize.x.toInt
      c.height = uiSize.y.toInt
    })
    interfaceSize = uiSize / 3
    interfaceCanvas.width = interfaceSize.x.toInt
    interfaceCanvas.height = interfaceSize.y.toInt

    0 until RuleRepository.squareCount foreach uiBackground.tileBackground

    uiTerrain.clearLayer()
    TileRepository.terrains.foreach(t => {
      Loader.loadTileAsync(t, image => {
        game.gameSquares.filter(_.terrain.equals(t)).foreach(sq => {
          uiTerrain.tileTerrain(sq, image)
        })
      })
    })

    redraw()

    if (null != squareClicked) {
      uiOverlay.tileUnitHighlight(squareClicked)
      uiInterface.interfaceTileStatus(squareClicked)
    }
  }

  def onMousemove(e: dom.MouseEvent): Unit = {

    val hoverSq = getGameSquare(new Point(e.clientX, e.clientY))

    if (hoverSq.canMove)
      dom.document.body.style.cursor = "pointer"
    else
      dom.document.body.style.cursor = "default"

    def onMousemoveHover(): Unit = {
      uiOverlay.clearLayer()
      uiInterface.clearLayer()
      if (hoverSq.isCurrentTurn) {
        uiOverlay.tileUnitHighlight(hoverSq)
        hoverSq.alliesInRange.foreach(uiOverlay.tileHighlight(_, 0.1, Color.fromPlayer(game.turnPlayer)))
        hoverSq.targetsInAttackRange.foreach(t => uiOverlay.tileHighlight(t, 0.1, Color.fromPlayer(t.unit.player)))
        uiInterface.interfaceTileStatus(hoverSq)
      }
      squareHover = hoverSq
    }

    def onMousemoveDrag(): Unit = {
      uiOverlay.clearLayer()
      uiOverlay.tileUnitHighlight(squareSource)
      uiOverlay.drawActionArrow(squareSource, hoverSq)
      if (squareSource.canAttack(hoverSq)) {
        audioChannel2.src = Loader.getSoundUrl(hoverSq, "target")
        audioChannel2.play()
        hoverSq.canBeTargetOf.foreach(
          uiOverlay.tileHighlight(_, 0.1, Color.fromPlayer(game.turnPlayer)))
        hoverSq.alliesInRange.foreach(
          uiOverlay.tileHighlight(_, 0.1, Color.fromPlayer(hoverSq.unit.player)))
        uiOverlay.tileHighlight(hoverSq, 0.3, Color.fromPlayer(hoverSq.unit.player))
        uiInterface.interfaceAttackPanel(hoverSq)
      }
      squareHover = hoverSq
    }

    if (null == squareSource || squareSource == hoverSq) {
      if (hoverSq != squareHover)
        onMousemoveHover()
    } else
      onMousemoveDrag()

  }

  def onClick(e: dom.MouseEvent): Unit = {
    if (squareHover.isCurrentTurn) {
      squareClicked = squareHover
      uiOverlay.clearLayer()
      uiOverlay.tileUnitHighlight(squareClicked)
      uiInterface.interfaceTileStatus(squareClicked)
    }
  }

  def onMouseup(e: dom.MouseEvent): Unit = {
    if (null != squareSource) {
      if (squareSource.canMoveTo(squareHover)) {
        audioChannel2.src = Loader.getSoundUrl(squareSource, "move")
        audioChannel2.play()
        squareSource.moveUnitTo(squareHover)
        redraw()
        if (game.turnMovedUnits.isEmpty) {
          audioChannel3.src = Loader.getSoundUrl("nextTurn")
          audioChannel3.play()
        }
      } else if (squareSource.canTakeArsenal(squareHover)) {
        squareSource.takeArsenal(squareHover)
        audioChannel2.src = Loader.getSoundUrl(squareHover, "attack")
        audioChannel2.play()
        redraw()
        audioChannel3.src = Loader.getSoundUrl("nextTurn")
        audioChannel3.play()
      } else if (squareSource.canAttack(squareHover)) {
        val attackResult = squareHover.launchAttackOn()
        if (attackResult == 2 || attackResult == 1) {
          audioChannel2.src = Loader.getSoundUrl(squareSource, "attack")
          audioChannel2.play()
          redraw()
          audioChannel3.src = Loader.getSoundUrl("nextTurn")
          audioChannel3.play()
        }
      }
    }
    squareSource = null
  }

  def onMousedown(e: dom.MouseEvent): Unit = {
    val mouse = new Point(e.clientX, e.clientY)
    val curSq = getGameSquare(mouse)
    if (curSq.canMove) {
      audioChannel1.src = Loader.getSoundUrl(curSq, "select")
      audioChannel1.play()
      squareSource = getGameSquare(mouse)
      uiInterface.interfaceTileStatus(squareSource)
    }
    else
      squareSource = null
  }

  def onKeydown(e: dom.KeyboardEvent): Unit = {
    val nextTurn = dom.KeyCode.Space
    val downloadGame = dom.KeyCode.D
    e.keyCode match {
      case `nextTurn` =>
        game.nextTurn()
        redraw()
        audioChannel3.src = Loader.getSoundUrl("nextTurn")
        audioChannel3.play()
      case `downloadGame` =>
        downloadGameFile()
      case _ =>
    }
  }

  private def downloadGameFile(): Unit = {
    def doc =
      game.gameSquares
        .map(sq => if (!sq.unit.equals(VoidTile)) sq.unit.char else sq.terrain.char)
        .grouped(RuleRepository.squareX)
        .map(_.mkString(" "))
        .mkString("\n")
    val a = dom.document.createElement("a").asInstanceOf[HTMLAnchorElement]
    val pb = new BlobPropertyBag{}
    pb.`type`="text/plain"
    val blob = new Blob(js.Array(doc), pb)
    val url = URL.createObjectURL(blob)
    a.href = url
    a.setAttribute("download", "game.gow")
    a.style.display = "none"
    dom.document.body.appendChild(a)
    a.click()
    dom.document.body.removeChild(a)
    URL.revokeObjectURL(url)
  }

}
