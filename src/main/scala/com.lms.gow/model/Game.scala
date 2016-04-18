package com.lms.gow.model

import com.lms.gow.model.Tile._
import com.lms.gow.model.Cardinality._
import scala.collection._

class Game {

  var blueTurn = true
  var remainingMoves = Rules.movesPerTurn
  val board = new Board

  class Board {

    val terrainLayer = Rules.startingTerrain
    val unitLayer = mutable.Seq(Rules.startingUnits: _*)
    val comLayer = Seq.fill(Rules.totalTiles)(mutable.Set[Direction](), mutable.Set[Direction]())

    class Coordinates(x: Int, y: Int) {

      val index = x + y * Rules.terrainWidth
      val unitTile = unitLayer(index)
      val terrainTile = terrainLayer(index)
      val comTile = comLayer(index)

      def inRange(r: Int): Seq[Coordinates] = {
        if (r.equals(1))
          Rules.directions.map(d => new Coordinates(x + d.x, y + d.y))
        else
          Seq(this)
      }

      def canMove = isOnline && blueTurn.equals(unitTile.isBlue) && unitTile.speed > 0

      def isOnline = {
        inRange(1)
          .filter(c => {
            c.unitTile.isUnit && c.unitTile.isBlue.equals(blueTurn) &&
              (blueTurn && c.comTile._2.size > 0 ||
                !blueTurn && c.comTile._1.size > 0)
          }).size > 0
      }

    }

    def move(source: Coordinates, dest: Coordinates): Boolean = {

      val unit = unitLayer(source.index)
      val dstUnit = unitLayer(dest.index)
      val dstTerrain = Rules.startingTerrain(dest.index)

      if (unit.isBlue.equals(blueTurn)
        && dstUnit.equals(VoidTile)
        && !dstTerrain.equals(Mountain)
        && remainingMoves > 0) {
        remainingMoves = remainingMoves - 1
        if (remainingMoves == 0) {
          remainingMoves = Rules.movesPerTurn
          blueTurn = !blueTurn
        }

        unitLayer.update(source.index, VoidTile)
        unitLayer.update(dest.index, unit)

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

}