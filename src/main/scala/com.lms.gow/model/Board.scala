package com.lms.gow.model

import scala.collection.mutable

class Board(g: Game) {

  val boardTiles: mutable.Seq[BoardTile] = {
    mutable.Seq[BoardTile](
      0 until Rules.totalTiles map (i => {
        val bt = new BoardTile(i, Rules.startingTerrain(i), this, g)
        bt.unit = Rules.startingUnits(i)
        bt
      }): _*)
  }

  /*def refreshComLayer = {

    comLayerRed.foreach(_.clear)
    comLayerBlue.foreach(_.clear)
    unitLayer
      .zipWithIndex
      .filter(u => u._1.eq(RedArsenal) || u._1.eq(BlueArsenal))
      .map(propagateCom(_))

    def propagateCom(source: (Tile, Int)): Unit = {

      val index = source._2
      val isBlue = source._1.isBlue

      if (isBlue)
        comLayerBlue(index) ++= Rules.directions
      else
        comLayerRed(index) ++= Rules.directions

      Rules.directions.foreach(dir => {
        var pos = index

        while (shouldPropagateCom(pos, dir, isBlue)) {

          pos += dir.x + (dir.y * Rules.terrainWidth)

          if (isBlue)
            comLayerBlue(pos) += dir
          else
            comLayerRed(pos) += dir

          if (
            Seq(BlueRelay, BlueSwiftRelay, RedRelay, RedSwiftRelay).contains(unitLayer(pos))
              && unitLayer(pos).isBlue.equals(isBlue)
              && comLayerBlue(pos).size < Rules.directions.size
              && comLayerRed(pos).size < Rules.directions.size)
            propagateCom(unitLayer(pos), pos)
        }

      })

      def shouldPropagateCom(pos: Int, dir: Direction, isBlue: Boolean): Boolean = {

        val npos = pos + dir.x + (dir.y * Rules.terrainWidth)

        if (!(0 until Rules.totalTiles).contains(npos))
          return false

        if ((Seq(E, NE, SE) contains dir) && (pos % Rules.terrainWidth == Rules.terrainWidth - 1))
          return false

        if ((Seq(W, NW, SW) contains dir) && (pos % Rules.terrainWidth == 0))
          return false

        val unit = unitLayer(npos)
        if (terrainLayer(npos) == Mountain ||
          (Rules.unitTilesRepository.contains(unit)
            && unit.isBlue != isBlue))
          return false
        else
          return true
      }
    }

  }

  refreshComLayer*/

}
