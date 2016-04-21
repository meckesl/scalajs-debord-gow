package com.lms.gow.model

import com.lms.gow.model.repo.CardinalityRepository.Cardinality
import com.lms.gow.model.repo.PlayerRepository.{Blue, Neutral, Player, Red}
import com.lms.gow.model.repo.TileRepository.{BlueArsenal, Mountain, RedArsenal}
import com.lms.gow.model.repo.{CardinalityRepository, RuleRepository}

import scala.collection.mutable

class Game {

  var turnRemainingMoves = RuleRepository.turnMoves
  val turnMovedUnits = mutable.Set[GameSquare]()
  var turnAttack = RuleRepository.turnAttacks
  var turnPlayer: Player = Blue

  val gameSquares =
    0 until RuleRepository.squareCount map (i => {
      val sq = new GameSquare(i, RuleRepository.startingTerrain(i), this)
      sq.unit = RuleRepository.startingUnits(i)
      sq
    })

  def nextTurn() = {
    turnRemainingMoves = RuleRepository.turnMoves
    turnMovedUnits.clear()
    turnAttack = RuleRepository.turnAttacks
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
          sq.com(pl) += d
          if (!sq.terrain.equals(Mountain) && Seq(Neutral, pl).contains(sq.unit.player)) {
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