package com.lms.gow.model

class GameQuery(g: Game) {

  def getUnitTile(x: Int, y: Int) = g.unitLayer(Coordinates.indexFromCoordinates(x, y))

  def getTerrainTile(x: Int, y: Int) = g.terrainLayer(Coordinates.indexFromCoordinates(x, y))

  def getComTile(x: Int, y: Int) = (g.comLayerRed(Coordinates.indexFromCoordinates(x, y)), g.comLayerBlue(Coordinates.indexFromCoordinates(x, y)))

  def getCoordinatesInRange(x: Int, y: Int, r: Int) : Seq[(Int, Int)] = {
    if (r.equals(1))
      Rules.directions.map(d => (x + d.x, y + d.y))
    else
      Seq((x, y))
      //FIXME handle all ranges
  }

  def hasCom(x: Int, y: Int) = {
    val unit = g.unitLayer(Coordinates.indexFromCoordinates(x, y))
    if (unit.eq(VoidTile))
      false
    else if (unit.isBlue)
      getComTile(x, y)._2.size > 0 || getCoordinatesInRange(x, y, 1).
        map(c => getUnitTile(c._1, c._2)).
        filter(_.isUnit).
        filter(_.isBlue)
        .size > 0
    else
      getComTile(x, y)._1.size > 0 || getCoordinatesInRange(x, y, 1).
        map(c => getUnitTile(c._1, c._2)).
        filter(_.isUnit).
        filterNot(_.isBlue)
        .size > 0
  }

  def isUnitOnline(x: Int, y: Int) =  getUnitTile(x,y).isCom || hasCom(x, y)

  def canUnitMove(x: Int, y: Int) = getUnitTile(x, y).speed > 0 && isUnitOnline(x, y) && getUnitTile(x,y).isBlue.equals(g.blueTurn)

}
