package com.lms.gow.model

import com.lms.gow.model.repo.CardinalityRepository.{Cardinality, SOURCE}
import com.lms.gow.model.repo.PlayerRepository.{Blue, Neutral, Player, Red}
import com.lms.gow.model.repo.TileRepository.{BlueArsenal, Mountain, RedArsenal, Tile, VoidTile}
import com.lms.gow.model.repo.{CardinalityRepository, RuleRepository}

import scala.collection.mutable
import scala.util.Random

class Game {

  var turnRemainingMoves: Int = RuleRepository.turnMoves
  val turnMovedUnits: mutable.Set[Square] = mutable.Set[Square]()
  var turnPlayer: Player = if (Random.nextBoolean()) Blue else Red

  var forcedRetreat: Option[Square] = None

  val capturedUnits: mutable.Seq[Tile] = mutable.Seq[Tile]()

  val gameSquares: Seq[Square] =
    0 until RuleRepository.squareCount map (i => {
      val sq = Square(i, RuleRepository.startingTerrain(i), this)
      sq.unit = RuleRepository.startingUnits(i)
      sq
    })

  def nextTurn(): Unit = {
    turnRemainingMoves = RuleRepository.turnMoves
    turnMovedUnits.clear()
    if (turnPlayer == Red)
      turnPlayer = Blue
    else
      turnPlayer = Red
  }

  def refreshComLayer(): Unit = {

    def terrainBlocks(sq: Square) = sq.terrain.equals(Mountain)
    def opponentBlocks(sq: Square, pl: Player) = !Seq(Neutral, pl).contains(sq.unit.player)

    def propagate(source: Square, cursor: Square, dir: Set[Cardinality]): Unit = {
      val pl = source.unit.player
      dir.foreach(d => {
        val sq = cursor.adjacentSquare(d)
        if (null != sq && !sq.com(pl).contains(d)) {
          sq.com(pl) += CardinalityRepository.opposite(d)
          if (!terrainBlocks(sq) && (!opponentBlocks(sq, pl) || sq.unit.isCom)) {
            sq.com(pl) += d
            if (sq.unit.isCom && sq.unit.player.equals(pl))
              propagate(sq, sq, CardinalityRepository.all)
            else
              propagate(source, sq, Set(d))
          }
        }
      })
    }
    def subPropagate(source: Square, cursor: Square, dir: Set[Cardinality]): Unit = {
      val pl = source.unit.player
      if (source.com(pl).nonEmpty)
        dir.filterNot(_.equals(SOURCE)).foreach(d => {
          val sq = cursor.adjacentSquare(d)
          if (null != sq && sq.unit.player.equals(pl)
            && !sq.com(pl).contains(CardinalityRepository.opposite(d))) {
            cursor.com(pl) += d
            sq.com(pl) += CardinalityRepository.opposite(d)
            subPropagate(source, sq, CardinalityRepository.all)
            if(sq.unit.isCom)
              propagate(sq, sq, CardinalityRepository.all)
          }
        })
    }
    gameSquares.foreach(_.com.foreach(_._2.clear()))
    gameSquares.filter(sq => Seq(RedArsenal, BlueArsenal).contains(sq.unit))
      .foreach(sq => propagate(sq, sq, CardinalityRepository.all))
    gameSquares.filter(sq => !sq.unit.isCom && sq.com.values.nonEmpty && !sq.unit.player.equals(Neutral))
      .foreach(sq => subPropagate(sq, sq, CardinalityRepository.all))
  }

  refreshComLayer()

}
