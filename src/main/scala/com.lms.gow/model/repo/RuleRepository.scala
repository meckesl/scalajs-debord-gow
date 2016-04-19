package com.lms.gow.model.repo

import com.lms.gow.model.repo.TileRepository.Tile

object RuleRepository {
  val squareWidth = 25
  val squareHeight = 20
  val squareCount = squareWidth * squareHeight
  val turnMoves = 5
  val turnAttacks = 1
  var startingTerrain: Seq[Tile] = null
  var startingUnits: Seq[Tile] = null
}