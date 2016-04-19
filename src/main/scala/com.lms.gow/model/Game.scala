package com.lms.gow.model

import com.lms.gow.model.repo.PlayerRepository.{Blue, Player, Red}
import com.lms.gow.model.repo.RuleRepository

class Game {

  var turnPlayer: Player = Blue
  var turnRemainingMoves = RuleRepository.turnMoves
  val gameSquares =
    0 until RuleRepository.squareCount map (i => {
      val sq = new GameSquare(i, RuleRepository.startingTerrain(i), this)
      sq.unit = RuleRepository.startingUnits(i)
      sq
    })

  def nextTurn() = {
    turnRemainingMoves = RuleRepository.turnMoves
    if (turnPlayer == Red)
      turnPlayer = Blue
    else
      turnPlayer = Red
  }

  def refreshComLayer() = {
    gameSquares.foreach(_.com.foreach(_._2.clear()))
    gameSquares.filter(_.unit.isCom).foreach(comUnit => {})
  }

  refreshComLayer()

}

/*comUnit.com(comUnit.unit.player) ++= CardinalityRepository.all

CardinalityRepository.all.foreach(dir => {

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

})
}

def refreshComLayer = {

com.foreach(_._2.clear())

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

refreshComLayer

}*/