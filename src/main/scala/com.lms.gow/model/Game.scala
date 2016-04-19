package com.lms.gow.model

import com.lms.gow.model.repo.PlayerRepository.{Blue, Player}

class Game {
  val board = new Board(this)
  var turnPlayer: Player = Blue
  var turnRemainingMoves = Rules.movesPerTurn
}