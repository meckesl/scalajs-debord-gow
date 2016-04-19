package com.lms.gow.model

import com.lms.gow.model.repo.CardinalityRepository
import com.lms.gow.model.repo.PlayerRepository.{Blue, Red}
import com.lms.gow.model.repo.TileRepository.{Mountain, Tile, VoidTile}
import com.lms.gow.ui.model.Point

import scala.collection.Seq

case class BoardTile(index: Int, terrain: Tile, b: Board, g: Game) {

  val coords = Point.fromLinear(index, Rules.terrainWidth)
  var unit : Tile = null
  //val comTile = board.comLayer(index)

  def inRange(r: Int): Seq[BoardTile] = {

    if (r.equals(1))
      CardinalityRepository.all map (c => {
        val card = Point.toLinear(new Point(c.x, c.y) + coords, Rules.terrainWidth)
        b.boardTiles(card.toInt)
      })
    else
      Seq(this)
  }

  def canMove = g.turnPlayer == unit.player && unit.speed > 0 //isOnline &&

  def moveTo(dest: BoardTile): Boolean = {

    if (g.turnPlayer.equals(unit.player)
      && dest.unit.equals(VoidTile)
      && !dest.terrain.equals(Mountain)
      && g.turnRemainingMoves > 0) {
      g.turnRemainingMoves = g.turnRemainingMoves - 1
      if (g.turnRemainingMoves == 0) {
        g.turnRemainingMoves = Rules.movesPerTurn
        if (g.turnPlayer == Red)
          g.turnPlayer = Blue
        else
          g.turnPlayer = Red
      }

      b.boardTiles(index).unit = unit
      unit = VoidTile

      //refreshComLayer
      true
    }
    else
      false
  }

  /*def isOnline = inRange(1)
    .exists(c => {
      game.turnPlayer == Blue && c.comTile._2.nonEmpty ||
        game.turnPlayer == Red && c.comTile._1.nonEmpty
    })*/

}