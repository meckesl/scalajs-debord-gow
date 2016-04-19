package com.lms.gow.model

import com.lms.gow.model.repo.{CardinalityRepository, RuleRepository}
import com.lms.gow.model.repo.PlayerRepository.{Blue, Red}
import com.lms.gow.model.repo.TileRepository.{Mountain, Tile, VoidTile}
import com.lms.gow.ui.model.Point

import scala.collection.Seq

case class GameSquare(index: Int, terrain: Tile, g: Game) {

  val coords = Point.fromLinear(index, RuleRepository.squareWidth)
  var unit : Tile = null
  //val comTile = board.comLayer(index)

  def inRange(r: Int): Seq[GameSquare] = {

    if (r.equals(1))
      CardinalityRepository.all map (c => {
        val card = Point.toLinear(new Point(c.x, c.y) + coords, RuleRepository.squareWidth)
        g.gameSquares(card.toInt)
      })
    else
      Seq(this)
  }

  def canMove = g.turnPlayer == unit.player && unit.speed > 0 //isOnline &&

  def moveTo(dest: GameSquare): Boolean = {

    if (g.turnPlayer.equals(unit.player)
      && dest.unit.equals(VoidTile)
      && !dest.terrain.equals(Mountain)
      && g.turnRemainingMoves > 0) {
      g.turnRemainingMoves -= 1
      if (g.turnRemainingMoves == 0) {
        g.turnRemainingMoves = RuleRepository.turnMoves
        if (g.turnPlayer == Red)
          g.turnPlayer = Blue
        else
          g.turnPlayer = Red
      }

      g.gameSquares(index).unit = unit
      unit = VoidTile

      //refreshComLayer
      true
    }
    else
      false
  }

  /*def isOnline = inRange(1)
    .exists(c => {
      game.turnPlayer == Blue && c.comTile._2.nonEmpty ||
        game.turnPlayer == Red && c.comTile._1.nonEmpty
    })*/

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