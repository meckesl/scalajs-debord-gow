package com.lms.gow.model

import com.lms.gow.model.repo.CardinalityRepository._
import com.lms.gow.model.repo.PlayerRepository.{Blue, Neutral, Player, Red}
import com.lms.gow.model.repo.TileRepository._
import com.lms.gow.model.repo.{CardinalityRepository, RuleRepository, TileRepository}

import scala.collection.{Seq, immutable, mutable}

case class GameSquare(index: Int, terrain: Tile, g: Game) {

  var unit: Tile = VoidTile
  val coords: Point = Point.fromLinear(index, RuleRepository.squareX)
  val com: Map[Player, mutable.Set[Cardinality]] = immutable.HashMap(Blue -> mutable.Set[Cardinality](), Red -> mutable.Set[Cardinality]())

  private def hasAdjacentOnlineAlly(sq: GameSquare = this): Boolean =
    sq.inRange(1)
      .filterNot(_.equals(this))
      .exists(s => {
        s.unit.player.equals(unit.player) &&
          s.com(unit.player).nonEmpty
      })

  def isOnline: Boolean = com(unit.player).nonEmpty || hasAdjacentOnlineAlly() || unit.isCom

  def isCurrentTurn: Boolean = g.turnPlayer.equals(unit.player)

  def canAttack(dest: GameSquare): Boolean = {
    targetsInAttackRange.contains(dest)
  }

  def canTakeArsenal(dest: GameSquare): Boolean = {
    canMoveTo(dest, Set(BlueArsenal, RedArsenal)
      .filterNot(_.player.equals(unit.player)).head)
  }

  def canMove: Boolean =
    isCurrentTurn &&
      g.turnRemainingMoves > 0 &&
      !g.turnMovedUnits.exists(_.equals(this)) &&
      unit.speed > 0 &&
      isOnline

  def canMoveTo(dest: GameSquare, allowedDest: Tile = VoidTile): Boolean =
    canMove &&
      dest.unit.equals(allowedDest) &&
      !dest.terrain.equals(Mountain) &&
      inRange(unit.speed).contains(dest)

  def moveUnitTo(dest: GameSquare): Boolean = {
    if (canMoveTo(dest)) {
      dest.unit = unit
      unit = VoidTile
      g.refreshComLayer()
      g.turnMovedUnits add dest
      g.turnRemainingMoves -= 1
      if (g.turnRemainingMoves == 0)
        g.nextTurn()
      true
    }
    else
      false
  }

  def takeArsenal(arsenal: GameSquare): Unit = {
    arsenal.unit = unit
    unit = VoidTile
    g.refreshComLayer()
    g.nextTurn()
  }

  def launchAttackOn(): Int = {
    val as = canBeTargetOfStrength
    val ds = defenseStrength
    val result = as - ds

    if (result > 1) {
      g.capturedUnits :+ unit
      unit = VoidTile
      g.refreshComLayer()
      g.nextTurn()
      2
    } else if (result == 1) {
      g.forcedRetreat = this
      g.nextTurn()
      1
    }
    else
      0
  }

  def targetsInAttackRange: mutable.Set[GameSquare] = inCombatRange(unit.range).filterNot(ir => {
    Set(unit.player, Neutral).contains(ir.unit.player)
  }).filterNot(ir => Set(RedArsenal, BlueArsenal).contains(ir.unit))

  def canBeTargetOfStrength: Int = {
    val attackers = canBeTargetOf
    val baseStrength = attackers.toSeq.map(_.unit.attack).sum
    if (!Set(Fortress, MountainPass).contains(terrain)) {
      val cavalryBonus = inRange(1)
        .intersect(attackers)
        .filterNot(_.terrain.equals(Fortress))
        .toSeq
        .map(_.unit)
        .count(Seq(BlueCavalry, RedCavalry).contains(_)) * TileRepository.cavalryChargeBonus
      baseStrength + cavalryBonus
    } else baseStrength
  }

  def defenseStrength: Int = alliesInRange.toSeq.map(sq => {
    var d = sq.unit.defense
    if (Set(Fortress, MountainPass).contains(sq.terrain))
      if (Set(BlueInfantry, RedInfantry, BlueCannon,
        RedCannon, BlueSwiftCannon, RedSwiftCannon).contains(sq.unit))
        d += sq.terrain.defense
    d
  }).sum

  def canBeTargetOf: Set[GameSquare] = {
    val enemies = mutable.Set[GameSquare]()
    1 to TileRepository.units.maxBy(_.range).range foreach (range => {
      enemies ++= inCombatRange(range)
        .filterNot(_.unit.player.equals(Neutral))
        .filterNot(_.unit.player.equals(unit.player))
        .filter(_.unit.range >= range)
        .filter(_.isOnline)
    })
    enemies.toSet
  }

  def alliesInRange: Set[GameSquare] = {
    val allies = mutable.Set[GameSquare]()
    1 to TileRepository.units.maxBy(_.range).range foreach (attackRange => {
      allies ++= inCombatRange(attackRange)
        .filter(_.unit.player.equals(unit.player))
        .filter(_.unit.range >= attackRange)
        .filter(_.isOnline)
    })
    allies.toSet
  }

  def adjacentSquare(c: Cardinality): GameSquare = {
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
      val cur = cursor.adjacentSquare(d)
      if (null != cur && !cur.terrain.equals(Mountain)) {
        acc += cur
        if (range > 1)
          acc ++= inCombatRange(range - 1, Set(d), cur, acc)
      }
    })
    acc
  }

  def inRange(r: Int): Set[GameSquare] = {
    if (r.equals(1))
      CardinalityRepository.all.map(adjacentSquare)
        .filterNot(adj => null == adj || adj.terrain.equals(Mountain)).toSet
    else if (r.equals(2))
      inRange(1) flatMap (_.inRange(1))
    else if (r.equals(3))
      inRange(2) flatMap (_.inRange(1))
    else
      Set(this)
  }

}