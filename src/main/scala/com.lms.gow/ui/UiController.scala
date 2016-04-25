package com.lms.gow.ui

import com.lms.gow.io.Loader
import com.lms.gow.model.repo.{RuleRepository, TileRepository}
import com.lms.gow.model.{Game, GameSquare, Point}
import org.scalajs.dom
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.HTMLAudioElement

case class UiController(game: Game, backgroundCanvas: Canvas, comCanvas: Canvas, terrainCanvas: Canvas, unitCanvas: Canvas, overlayCanvas: Canvas, interfaceCanvas: Canvas) {

  val uiBackround = new UiLayer(backgroundCanvas)
  val uiCom = new UiLayer(comCanvas)
  val uiTerrain = new UiLayer(terrainCanvas)
  val uiUnits = new UiLayer(unitCanvas)
  val uiOverlay = new UiLayer(overlayCanvas)
  val uiInterface = new UiLayer(interfaceCanvas)

  var squareHover: GameSquare = null
  var squareClicked: GameSquare = null
  var squareSource: GameSquare = null

  def tileSize = uiSize / new Point(RuleRepository.squareX, RuleRepository.squareY)
  var uiSize = new Point(terrainCanvas.width, terrainCanvas.height)
  var interfaceSize = new Point(interfaceCanvas.width, interfaceCanvas.height)

  val audioChannel1 = dom.document.createElement("audio").asInstanceOf[HTMLAudioElement]
  val audioChannel2 = dom.document.createElement("audio").asInstanceOf[HTMLAudioElement]
  val audioChannel3 = dom.document.createElement("audio").asInstanceOf[HTMLAudioElement]

  audioChannel3.src = Loader.getSoundUrl("start")
  audioChannel3.play

  def getGameSquare(p: Point): GameSquare = {
    val corrected = (p - (p % tileSize)) / tileSize
    val index = corrected.toLinear(RuleRepository.squareX)
    game.gameSquares(index)
  }

  def boardChangeRedraw = {
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
    interfaceSize = uiSize / 3
    interfaceCanvas.width = interfaceSize.x.toInt
    interfaceCanvas.height = interfaceSize.y.toInt

    0 until RuleRepository.squareCount foreach uiBackround.tileBackground

    uiTerrain.clearLayer()
    TileRepository.terrains.foreach(t => {
      Loader.getTileAsync(t, image => {
        game.gameSquares.filter(_.terrain.equals(t)).foreach(sq => {
          uiTerrain.tileTerrain(sq, image)
        })
      })
    })

    boardChangeRedraw

    if (null != squareClicked) {
      uiOverlay.tileUnitHighlight(squareClicked)
      uiInterface.interfaceTileStatus(squareClicked)
    }
  }

  def onMousemove(e: dom.MouseEvent) {

    val curSq = getGameSquare(new Point(e.clientX, e.clientY))

    def onMousemoveHover = {
      uiOverlay.clearLayer()
      uiInterface.clearLayer()
      if (curSq.isCurrentTurn) {
        uiOverlay.tileUnitHighlight(curSq)
        curSq.alliesInRange.foreach(uiOverlay.tileHighlight(_, 0.1, Color.fromPlayer(game.turnPlayer)))
        curSq.targetsInAttackRange.foreach(t => uiOverlay.tileHighlight(t, 0.1, Color.fromPlayer(t.unit.player)))
        uiInterface.interfaceTileStatus(curSq)
      }
      squareHover = curSq
    }

    def onMousemoveDrag = {
      uiOverlay.clearLayer()
      uiOverlay.tileUnitHighlight(squareSource)
      uiOverlay.drawActionArrow(squareSource, curSq)
      if (squareSource.canAttack(curSq)) {
        audioChannel2.src = Loader.getSoundUrl(curSq, "target")
        audioChannel2.play
        curSq.canBeTargetOf.foreach(
          uiOverlay.tileHighlight(_, 0.1, Color.fromPlayer(game.turnPlayer)))
        curSq.alliesInRange.foreach(
          uiOverlay.tileHighlight(_, 0.1, Color.fromPlayer(curSq.unit.player)))
        uiOverlay.tileHighlight(curSq, 0.3, Color.fromPlayer(curSq.unit.player))
        uiInterface.interfaceAttackPanel(curSq)
      }
      squareHover = curSq
    }

    if (null == squareSource || squareSource == curSq) {
      if (curSq != squareHover)
        onMousemoveHover
    } else
      onMousemoveDrag

  }

  def onClick(e: dom.MouseEvent) {
    if (squareHover.isCurrentTurn) {
      squareClicked = squareHover
      uiOverlay.clearLayer()
      uiOverlay.tileUnitHighlight(squareClicked)
      uiInterface.interfaceTileStatus(squareClicked)
    }
  }

  def onMouseup(e: dom.MouseEvent) {
    if (null != squareSource) {
      if (squareSource.canMoveTo(squareHover)) {
        audioChannel2.src = Loader.getSoundUrl(squareSource, "move")
        audioChannel2.play
        squareSource.moveUnitTo(squareHover)
        boardChangeRedraw
        if (game.turnMovedUnits.isEmpty) {
          audioChannel3.src = Loader.getSoundUrl("nextTurn")
          audioChannel3.play
        }
      } else if (squareSource.canTakeArsenal(squareHover)) {
        squareSource.takeArsenal(squareHover)
        audioChannel2.src = Loader.getSoundUrl(squareHover, "attack")
        audioChannel2.play
        boardChangeRedraw
        audioChannel3.src = Loader.getSoundUrl("nextTurn")
        audioChannel3.play
      } else if (squareSource.canAttack(squareHover)) {
        val attackResult = squareHover.launchAttackOn
        if (attackResult == 2 || attackResult == 1) {
          audioChannel2.src = Loader.getSoundUrl(squareSource, "attack")
          audioChannel2.play
          boardChangeRedraw
          audioChannel3.src = Loader.getSoundUrl("nextTurn")
          audioChannel3.play
        }
      }
    }
    squareSource = null
  }

  def onMousedown(e: dom.MouseEvent) {
    val mouse = new Point(e.clientX, e.clientY)
    val curSq = getGameSquare(mouse)
    if (curSq.canMove) {
      audioChannel1.src = Loader.getSoundUrl(curSq, "select")
      audioChannel1.play
      squareSource = getGameSquare(mouse)
      uiInterface.interfaceTileStatus(squareSource)
    }
    else
      squareSource = null
  }

  def onKeydown(e: dom.KeyboardEvent) {
    dom.console.log(s"key=${e.keyCode}")
    if (e.keyCode.equals(32)) {
      game.nextTurn()
      boardChangeRedraw
      audioChannel3.src = Loader.getSoundUrl("nextTurn")
      audioChannel3.play
    }
  }

}
