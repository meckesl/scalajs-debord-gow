package com.lms.gow.model

import com.lms.gow.model.repo.CardinalityRepository.Cardinality
import com.lms.gow.model.repo.PlayerRepository.{Blue, Neutral, Player, Red}
import com.lms.gow.model.repo.TileRepository.{BlueArsenal, Mountain, RedArsenal}
import com.lms.gow.model.repo.{CardinalityRepository, RuleRepository}

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

    def propagate(source: GameSquare, cursor: GameSquare, dir: Seq[Cardinality]): Unit = {
      val pl = source.unit.player
      dir.foreach(d => {
        val sq = cursor.getAdjacentSquare(d)
        if (null != sq && !sq.com(pl).contains(d)) {
          if (!sq.terrain.equals(Mountain) && Seq(Neutral, pl).contains(sq.unit.player)) {
            sq.com(pl) += d
            if (sq.unit.isCom && sq.unit.player.equals(pl))
              propagate(sq, sq, CardinalityRepository.all)
            else
              propagate(source, sq, Seq(d))
          }
        }
      })
    }

    gameSquares.foreach(_.com.foreach(_._2.clear()))
    gameSquares.filter(sq => Seq(RedArsenal, BlueArsenal).contains(sq.unit))
      .foreach(sq => propagate(sq, sq, CardinalityRepository.all))
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