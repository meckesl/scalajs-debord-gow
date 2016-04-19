package com.lms.gow.model

import com.lms.gow.model.CardinalityRepository.Cardinality
import com.lms.gow.model.PlayerRepository.{Blue, Red}
import com.lms.gow.model.TileRepository.{Mountain, VoidTile}

import scala.collection.{Seq, mutable}

class Board(game: Game) {

  val terrainLayer = Rules.startingTerrain
  val unitLayer = mutable.Seq(Rules.startingUnits: _*)
  val comLayer = Seq.fill(Rules.totalTiles)(mutable.Set[Cardinality](), mutable.Set[Cardinality]())

  class Coordinates(x: Int, y: Int) {

    val index = x + y * Rules.terrainWidth
    val unitTile = unitLayer(index)
    val terrainTile = terrainLayer(index)
    val comTile = comLayer(index)

    def inRange(r: Int): Seq[Coordinates] = {
      if (r.equals(1))
        CardinalityRepository.all map (c => new Coordinates(x + c.x, y + c.y))
      else
        Seq(this)
    }

    def canMove = isOnline && game.turnPlayer == unitTile.player && unitTile.speed > 0

    def isOnline = inRange(1)
      .exists(c => {
        game.turnPlayer == Blue && c.comTile._2.nonEmpty ||
          game.turnPlayer == Red && c.comTile._1.nonEmpty
      })

  }

  def move(source: Coordinates, dest: Coordinates): Boolean = {

    val unit = unitLayer(source.index)
    val dstUnit = unitLayer(dest.index)
    val dstTerrain = Rules.startingTerrain(dest.index)

    if (game.turnPlayer.equals(unit.player)
      && dstUnit.equals(VoidTile)
      && !dstTerrain.equals(Mountain)
      && game.turnRemainingMoves > 0) {
      game.turnRemainingMoves = game.turnRemainingMoves - 1
      if (game.turnRemainingMoves == 0) {
        game.turnRemainingMoves = Rules.movesPerTurn
        if (game.turnPlayer == Red)
          game.turnPlayer = Blue
        else
          game.turnPlayer = Red
      }

      unitLayer update(source.index, VoidTile)
      unitLayer update(dest.index, unit)

      //refreshComLayer
      true
    }
    else
      false
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
