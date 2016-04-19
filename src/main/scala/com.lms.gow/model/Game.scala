package com.lms.gow.model

import com.lms.gow.model.PlayerRepository.{Blue, Player}

class Game {
  val board = new Board(this)
  var turnPlayer: Player = Blue
  var turnRemainingMoves = Rules.movesPerTurn
}