package com.lms.gow.model

import scala.collection.Seq

object Rules {

  val terrainWidth = 25
  val terrainHeight = 20
  val totalTiles = terrainWidth * terrainHeight
  val movesPerTurn = 5
  val attacksPerTurn = 1
  val terrainTilesRepository = Seq(Fortress, Mountain, MountainPass)
  val unitTilesRepository = Seq(BlueCannon, BlueSwiftCannon, BlueRelay, BlueSwiftRelay, BlueInfantry, BlueCavalry, BlueArsenal,
    RedCannon, RedSwiftCannon, RedRelay, RedSwiftRelay, RedInfantry, RedCavalry, RedArsenal)
  val startingTerrain = IO.loadTilesFromFile("init.board", terrainTilesRepository)
  val startingUnits = IO.loadTilesFromFile("init.units", unitTilesRepository)
  val directions = Seq(N, NE, E, SE, S, SW, W, NW)

}