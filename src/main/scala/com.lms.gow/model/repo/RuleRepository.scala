package com.lms.gow.model.repo

import com.lms.gow.model.repo.TileRepository.Tile

object RuleRepository {
  val turnMoves = 5
  val turnAttacks = 1
  var startingTerrain: Seq[Tile] = null
  var startingUnits: Seq[Tile] = null
  var squareX: Int = 0
  var squareY: Int = 0
  def squareCount = squareX * squareY
}