package com.lms.gow.model

import com.lms.gow.model.repo.CardinalityRepository._
import com.lms.gow.model.repo.PlayerRepository.{Blue, Neutral, Red}
import com.lms.gow.model.repo.TileRepository._
import com.lms.gow.model.repo.{CardinalityRepository, RuleRepository, TileRepository}

import scala.collection.immutable.HashMap
import scala.collection.{Seq, mutable}

case class GameSquare(index: Int, terrain: Tile, g: Game) {

  var unit: Tile = VoidTile
  val coords = Point.fromLinear(index, RuleRepository.squareX)
  val com = HashMap(Seq(Blue, Red).map(p => p -> mutable.Set[Cardinality]()): _*)

  private def hasAdjacentOnlineAlly(sq: GameSquare = this): Boolean =
    sq.inRange(1)
      .filterNot(_.equals(this))
      .filter(s => {
        s.unit.player.equals(unit.player) &&
          s.com(unit.player).nonEmpty
      }).nonEmpty

  def isOnline = com(unit.player).nonEmpty || hasAdjacentOnlineAlly() || unit.isCom

  def canAttack(dest: GameSquare): Boolean = {
    targetsInAttackRange.contains(dest)
  }

  def canMove =
    g.turnPlayer.equals(unit.player) &&
      g.turnRemainingMoves > 0 &&
      g.turnMovedUnits.filter(_.equals(this)).isEmpty &&
      unit.speed > 0 &&
      isOnline

  def canMoveTo(dest: GameSquare) =
    canMove &&
      dest.unit.equals(VoidTile) &&
      !dest.terrain.equals(Mountain) &&
      inRange(unit.speed).contains(dest) &&
      (unit.isCom || dest.com(unit.player).nonEmpty ||
        hasAdjacentOnlineAlly(dest))

  def moveUnitTo(dest: GameSquare): Boolean = {
    if (canMoveTo(dest)) {
      dest.unit = unit
      unit = VoidTile
      g.refreshComLayer
      g.turnMovedUnits add dest
      g.turnRemainingMoves -= 1
      if (g.turnRemainingMoves == 0)
        g.nextTurn()
      true
    }
    else
      false
  }

  def targetsInAttackRange() = inCombatRange(unit.range).filter(ir => {
    !Set(unit.player, Neutral).contains(ir.unit.player)
  })

  def canBeTargetOf(): mutable.Set[GameSquare] = {
    val enemies = mutable.Set[GameSquare]()
    1 to TileRepository.units.maxBy(_.range).range foreach (range => {
      enemies ++= inCombatRange(range)
        .filterNot(_.unit.player.equals(Neutral))
        .filterNot(_.unit.player.equals(unit.player))
        .filter(_.unit.range >= range)
        .filter(_.isOnline)
    })
    enemies
  }

  def alliesInRange(): mutable.Set[GameSquare] = {
    val allies = mutable.Set[GameSquare]()
    1 to TileRepository.units.maxBy(_.range).range foreach (range => {
      allies ++= inCombatRange(range)
        .filterNot(_.unit.player.equals(Neutral))
        .filter(_.unit.player.equals(unit.player))
        .filter(_.unit.range >= range)
        .filter(_.isOnline)
    })
    allies
  }

  def getAdjacentSquare(c: Cardinality) = {
    val i = (new Point(c.x, c.y) + coords).toLinear(RuleRepository.squareX)
    if ((i < g.gameSquares.size && i >= 0) &&
      (!(coords.x == 0 && Set(NW, SW, W).contains(c))) &&
      (!(coords.x == RuleRepository.squareX - 1 && Seq(NE, SE, E).contains(c))))
      g.gameSquares(i)
    else
      null
  }

  private def inCombatRange(
                             range: Int,
                             dir: Set[Cardinality] = CardinalityRepository.all,
                             cursor: GameSquare = this,
                             acc: mutable.Set[GameSquare] = mutable.Set[GameSquare]()): mutable.Set[GameSquare] = {
    dir.foreach(d => {
      val cur = cursor.getAdjacentSquare(d)
      if (null != cur && !cur.terrain.equals(Mountain)) {
        acc += cur
        if (range > 1)
          acc ++= inCombatRange(range - 1, Set(d), cur, acc)
      }
    })
    acc
  }

  private def inRange(r: Int): Set[GameSquare] = {
    if (r.equals(1))
      CardinalityRepository.all map (getAdjacentSquare(_)) filterNot (_.terrain.equals(Mountain)) toSet
    else if (r.equals(2))
      inRange(1) flatMap (_.inRange(1))
    else if (r.equals(3))
      inRange(2) flatMap (_.inRange(1))
    else
      Set(this)
  }

  override def toString = s"(x=${coords.x} y=${coords.y}, unit=${unit.char})"

}