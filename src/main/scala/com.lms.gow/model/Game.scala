package com.lms.gow.model

import com.lms.gow.model.repo.CardinalityRepository.Cardinality
import com.lms.gow.model.repo.PlayerRepository.{Blue, Neutral, Player, Red}
import com.lms.gow.model.repo.TileRepository.{BlueArsenal, Mountain, RedArsenal, Tile}
import com.lms.gow.model.repo.{CardinalityRepository, RuleRepository}

import scala.collection.mutable

class Game {

  var turnRemainingMoves: Int = RuleRepository.turnMoves
  val turnMovedUnits: mutable.Set[GameSquare] = mutable.Set[GameSquare]()
  var turnPlayer: Player = if (scala.util.Random.nextBoolean) Blue else Red

  var forcedRetreat: Option[GameSquare] = None

  val capturedUnits: mutable.Seq[Tile] = mutable.Seq[Tile]()

  val gameSquares: Seq[GameSquare] =
    0 until RuleRepository.squareCount map (i => {
      val sq = new GameSquare(i, RuleRepository.startingTerrain.get(i), this)
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
    def propagate(source: GameSquare, cursor: GameSquare, dir: Set[Cardinality]): Unit = {
      val pl = source.unit.player
      dir.foreach(d => {
        val sq = cursor.adjacentSquare(d)
        if (null != sq && !sq.com(pl).contains(d)) {
          sq.com(pl) += d
          if (!sq.terrain.equals(Mountain) &&
            (Seq(Neutral, pl).contains(sq.unit.player) || sq.unit.isCom)) {
            if (sq.unit.isCom && sq.unit.player.equals(pl))
              propagate(sq, sq, CardinalityRepository.all)
            else
              propagate(source, sq, Set(d))
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
