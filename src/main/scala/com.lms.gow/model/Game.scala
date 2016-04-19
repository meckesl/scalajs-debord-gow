package com.lms.gow.model

import com.lms.gow.model.repo.PlayerRepository.{Blue, Player}
import com.lms.gow.model.repo.RuleRepository

class Game {
  var turnPlayer: Player = Blue
  var turnRemainingMoves = RuleRepository.turnMoves
  val gameSquares =
    0 until RuleRepository.squareCount map (i => {
      val bt = new GameSquare(i, RuleRepository.startingTerrain(i), this)
      bt.unit = RuleRepository.startingUnits(i)
      bt
    })
}