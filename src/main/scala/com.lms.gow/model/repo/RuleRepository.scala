package com.lms.gow.model.repo

import com.lms.gow.model.repo.TileRepository.Tile

object RuleRepository {
  val squareX = 25
  val squareY = 20
  val squareCount = squareX * squareY
  val turnMoves = 5
  val turnAttacks = 1
  var startingTerrain: Seq[Tile] = null
  var startingUnits: Seq[Tile] = null
}