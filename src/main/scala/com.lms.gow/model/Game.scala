package com.lms.gow.model

import scala.collection._

class Game {

  val terrainLayer = Rules.startingTerrain
  val unitLayer = mutable.Seq(Rules.startingUnits: _*)
  val comLayerRed = Seq.fill(Rules.totalTiles)(mutable.Set[Direction]())
  val comLayerBlue = Seq.fill(Rules.totalTiles)(mutable.Set[Direction]())

  var blueTurn = true
  var remainingMoves = Rules.movesPerTurn

  def move(x: Int, y: Int, newX: Int, newY: Int): Boolean = {

    val srcCoord = Coordinates.indexFromCoordinates(x, y)
    val dstCoord = Coordinates.indexFromCoordinates(newX, newY)
    val unit = unitLayer(srcCoord)
    val dstUnit = unitLayer(dstCoord)
    val dstTerrain = Rules.startingTerrain(dstCoord)

    if (unit.isBlue.equals(blueTurn)
      && dstUnit.equals(VoidTile)
      && !dstTerrain.equals(Mountain)
      && remainingMoves > 0) {
      remainingMoves = remainingMoves - 1
      if (remainingMoves == 0) {
        remainingMoves = Rules.movesPerTurn
        blueTurn = !blueTurn
      }

      unitLayer.update(srcCoord, VoidTile)
      unitLayer.update(dstCoord, unit)

      //refreshComLayer

      return true
    }

    return false
  }

  /*def refreshComLayer = {

    comLayerRed.foreach(_.clear)
    comLayerBlue.foreach(_.clear)
    unitLayer
      .zipWithIndex
      .filter(u => u._1.eq(RedArsenal) || u._1.eq(BlueArsenal))
      .map(propagateCom(_))

    def propagateCom(source: (Tile, Int)): Unit = {

      val index = source._2
      val isBlue = source._1.isBlue

      if (isBlue)
        comLayerBlue(index) ++= Rules.directions
      else
        comLayerRed(index) ++= Rules.directions

      Rules.directions.foreach(dir => {
        var pos = index

        while (shouldPropagateCom(pos, dir, isBlue)) {

          pos += dir.x + (dir.y * Rules.terrainWidth)

          if (isBlue)
            comLayerBlue(pos) += dir
          else
            comLayerRed(pos) += dir

          if (
            Seq(BlueRelay, BlueSwiftRelay, RedRelay, RedSwiftRelay).contains(unitLayer(pos))
              && unitLayer(pos).isBlue.equals(isBlue)
              && comLayerBlue(pos).size < Rules.directions.size
              && comLayerRed(pos).size < Rules.directions.size)
            propagateCom(unitLayer(pos), pos)
        }

      })

      def shouldPropagateCom(pos: Int, dir: Direction, isBlue: Boolean): Boolean = {

        val npos = pos + dir.x + (dir.y * Rules.terrainWidth)

        if (!(0 until Rules.totalTiles).contains(npos))
          return false

        if ((Seq(E, NE, SE) contains dir) && (pos % Rules.terrainWidth == Rules.terrainWidth - 1))
          return false

        if ((Seq(W, NW, SW) contains dir) && (pos % Rules.terrainWidth == 0))
          return false

        val unit = unitLayer(npos)
        if (terrainLayer(npos) == Mountain ||
          (Rules.unitTilesRepository.contains(unit)
            && unit.isBlue != isBlue))
          return false
        else
          return true
      }
    }

  }

  refreshComLayer*/

}
