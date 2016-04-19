package com.lms.gow.model

import com.lms.gow.model.repo.CardinalityRepository.Cardinality
import com.lms.gow.model.repo.PlayerRepository.{Blue, Red}
import com.lms.gow.model.repo.TileRepository.{Mountain, Tile, VoidTile}
import com.lms.gow.model.repo.{CardinalityRepository, RuleRepository}
import com.lms.gow.ui.model.Point

import scala.collection.immutable.HashMap
import scala.collection.{Seq, mutable}

case class GameSquare(index: Int, terrain: Tile, g: Game) {

  val coords = Point.fromLinear(index, RuleRepository.squareX)
  var unit: Tile = null
  val com = HashMap(Seq(Blue, Red).map(p => (p -> mutable.Set[Cardinality]())): _*)

  def canMove =
    g.turnPlayer.equals(unit.player) &&
      g.turnRemainingMoves > 0 &&
      unit.speed > 0 &&
      isOnline

  def moveTo(dest: GameSquare): Boolean = {
    if (canMove && dest.unit.equals(VoidTile) && !dest.terrain.equals(Mountain)) {
      g.gameSquares(index).unit = unit
      unit = VoidTile
      g.turnRemainingMoves -= 1
      if (g.turnRemainingMoves == 0)
        g.nextTurn()
      //refreshComLayer
      true
    }
    else
      false
  }

  def inRange(r: Int): Seq[GameSquare] = {
    if (r.equals(1))
      CardinalityRepository.all map (c => {
        val i = Point.toLinear(
          new Point(c.x, c.y) + coords,
          RuleRepository.squareX).toInt
        g.gameSquares(i)
      })
    else
      Seq(this)
  }

  def isOnline = inRange(1).exists(_.com(g.turnPlayer).nonEmpty)

}