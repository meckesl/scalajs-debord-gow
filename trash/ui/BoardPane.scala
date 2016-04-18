package com.lms.gow.ui

import javafx.beans
import javafx.beans.InvalidationListener
import javafx.event.EventHandler
import javafx.scene.canvas.{Canvas, GraphicsContext}
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle

import com.lms.gow.model._

import scala.collection.mutable

class BoardPane(g: Board) extends Pane {

  def tileSize = 50

  val width = tileSize * Rules.terrainWidth
  val height = tileSize * Rules.terrainHeight

  private def tileImage(t: Tile) = new Image("tiles/" + t.char.toString + ".png")
  val images = (g.terrainLayer ++ g.unitLayer)
    .filterNot(_.eq(VoidTile))
    .map(t => (t.char, tileImage(t)))
    .toMap

  val terrainCanvas = new Canvas(width, height)
  val unitCanvas = new Canvas(width, height)
  val comCanvas = new Canvas(width, height)
  getChildren addAll(terrainCanvas, unitCanvas, comCanvas)

  val query = new GameQuery(g)
  var selected: (Int, Int) = _
  var moveOptions: Seq[(Int, Int)] = _
  var selectSquare: Rectangle = _
  var moveSquares: Seq[Rectangle] = _

  comCanvas.setOnMouseClicked(new EventHandler[MouseEvent] {
    override def handle(event: MouseEvent) = onTileClicked(event)
  })

  /*val listener = new InvalidationListener {
    override def invalidated(o: beans.Observable) {
      scanBoardCoordinates(drawTileLayers)
    }
  }
  widthProperty addListener (listener)
  heightProperty addListener (listener)*/

  def drawTerrainTile(x: Int, y: Int, gc: GraphicsContext): Unit = {

    val relx = x * tileSize
    val rely = y * tileSize

    if ((x + y) % 2 == 0) {
      gc.setFill(Color.WHITE)
      gc.fillRect(relx, rely, tileSize, tileSize)
    }

    val t = query.getTerrainTile(x, y)
    if (!t.eq(VoidTile)) {
      gc.drawImage(images(t.char), x * tileSize, y * tileSize, tileSize, tileSize)
    }
  }

  def drawUnitTile(x: Int, y: Int, gc: GraphicsContext): Unit = {

    val t = query.getUnitTile(x, y)

    if (!t.eq(VoidTile)) {

      val relx = x * tileSize
      val rely = y * tileSize

      if (!query.isUnitOnline(x, y))
        gc.setGlobalAlpha(0.3)

      gc.drawImage(images(t.char), relx, rely, tileSize, tileSize)

      gc.setGlobalAlpha(1)

      if (t.isBlue)
        gc.setFill(Color.BLUE)
      else
        gc.setFill(Color.RED)
      gc.fillRect(relx, rely + (tileSize - tileSize / 15), tileSize, tileSize / 15)

    }
  }

  def drawComTile(x: Int, y: Int, gc: GraphicsContext): Unit = {

    println(x + "/" + y)

    val relx = x * tileSize
    val rely = y * tileSize
    val c = query.getComTile(x, y)
    val red = c._1
    val blue = c._2

    gc.setLineWidth(4)
    gc.setGlobalAlpha(0.05)

    def doDraw(dirs: mutable.Set[Direction]) = {
      dirs.foreach(_ match {
        case N | S => {
          gc.moveTo(relx + tileSize / 2, rely)
          gc.lineTo(relx + tileSize / 2, rely + tileSize)
        }
        case NE | SW => {
          gc.moveTo(relx, rely + tileSize)
          gc.lineTo(relx + tileSize, rely)
        }
        case E | W => {
          gc.moveTo(relx, rely + tileSize / 2)
          gc.lineTo(relx + tileSize, rely + tileSize / 2)
        }
        case SE | NW => {
          gc.moveTo(relx, rely)
          gc.lineTo(relx + tileSize, rely + tileSize)
        }
      })
      gc.stroke
    }

    //gc.setStroke(Color.RED)
    //doDraw(red)

    gc.setStroke(Color.BLUE)
    doDraw(blue)

  }

  def drawTileLayers(x: Int, y: Int): Unit = {

    drawTerrainTile(x, y, terrainCanvas.getGraphicsContext2D)
    drawUnitTile(x, y, unitCanvas.getGraphicsContext2D)
    drawComTile(x, y, comCanvas.getGraphicsContext2D)

    /*def setPerspective(p: Pane): Unit = {
      val e = new PerspectiveTransform()
      e.setUlx(tileSize * 2)
      e.setUly(0)
      e.setUrx(width - tileSize * 2)
      e.setUry(0)
      e.setLlx(0)
      e.setLly(height)
      e.setLrx(width)
      e.setLry(height)
      p.setEffect(e)
    }*/

  }

  def onTileClicked(event: MouseEvent) = {

    val x = (math.floor(event.getSceneX / tileSize)).toInt
    val y = (math.floor(event.getSceneY / tileSize)).toInt

    if (query.canUnitMove(x, y)) {
      selected = (x, y)
      moveOptions = query.getCoordinatesInRange(x, y, 1)
      refreshUxLayer
    } else if (selectSquare != null) {
      g.move(selected._1, selected._2, x, y)
      scanBoardCoordinates(drawTileLayers)
    }

    def refreshUxLayer = {

      if (selectSquare != null)
        getChildren.remove(selectSquare)

      selectSquare = new Rectangle(selected._1 * tileSize, selected._2 * tileSize, tileSize, tileSize)
      selectSquare.setFill(Color.CHARTREUSE)
      selectSquare.setOpacity(0.2)
      getChildren.add(selectSquare)

      if (moveSquares != null)
        moveSquares.foreach(ms => {
          getChildren.remove(ms)
        })

      moveOptions.foreach(mo => {
        val mos = new Rectangle(selected._1 * tileSize, selected._2 * tileSize, tileSize, tileSize)
        mos.setFill(Color.CHARTREUSE)
        mos.setOpacity(0.2)
        getChildren.add(mos)
      })

    }

  }

  def scanBoardCoordinates(func: (Int, Int) => Unit) {
    (0 until Rules.terrainWidth).foreach(x => {
      (0 until Rules.terrainHeight).foreach(y => {
        func(x, y)
      })
    })
  }

  scanBoardCoordinates(drawTileLayers)

  /*val e = new PerspectiveTransform()
  e.setUlx(tileSize * 2)
  e.setUly(0)
  e.setUrx(width - tileSize * 2)
  e.setUry(0)
  e.setLlx(0)
  e.setLly(height)
  e.setLrx(width)
  e.setLry(height)
  setEffect(e)
  */

}
