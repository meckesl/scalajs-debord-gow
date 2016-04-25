import AppTest._
import com.lms.gow.model.repo.TileRepository.VoidTile
import com.lms.gow.model.repo.{RuleRepository, TileRepository}
import com.lms.gow.model.{Game, GameSquare, Point}

object BoardPositions {

  private var game: Game = null

  def setupGame(a: String, b: String): Game = {
    val terrain = a.filter(_ > ' ').filter(TileRepository.all.map(_.char).contains(_))
      .map(TileRepository.getByChar(_))
    val units = b.filter(_ > ' ').filter(TileRepository.all.map(_.char).contains(_))
      .map(TileRepository.getByChar(_))
    RuleRepository.startingTerrain = terrain.map(t => {
      if (TileRepository.terrains.contains(t)) t else VoidTile
    })
    RuleRepository.startingUnits = units.map(u => {
      if (TileRepository.units.contains(u)) u else VoidTile
    })
    assert(RuleRepository.startingTerrain.size.equals(RuleRepository.startingUnits.size))
    game = new Game()
    game
  }

  def get(x: Int, y: Int): GameSquare = {
    game.gameSquares(new Point(x - 1, y - 1).toLinear(RuleRepository.squareX))
  }

  def getWithTurn(x: Int, y: Int) = {
    val u = get(x, y)
    assert(u.unit != VoidTile)
    if (!u.isCurrentTurn)
      game.nextTurn()
    assert(u.isCurrentTurn)
    u
  }

  val boardTest1 =
    """
      |-- 01 02 03 04 05 06 07 08 09 10
      |01 .  .  .  .  .  .  .  .  .  .
      |02 .  .  .  .  M  M  .  .  .  .
      |03 .  .  .  .  .  .  .  .  .  .
      |04 .  .  .  .  .  .  .  .  .  .
      |05 .  .  .  .  .  .  .  .  .  .
      |06 .  M  M  M  .  M  M  .  .  .
      |07 .  .  .  .  .  .  .  .  .  .
      |08 .  .  .  .  .  .  .  .  .  .
      |09 .  .  .  .  .  .  .  .  .  .
      |10 .  .  .  .  .  .  .  .  .  .
    """

  val unitsTest1 =
    """
      |-- 01 02 03 04 05 06 07 08 09 10
      |01 .  .  .  .  .  .  .  .  .  .
      |02 .  .  .  .  M  M  .  .  .  .
      |03 .  .  .  .  .  v  .  .  .  .
      |04 .  .  .  .  .  .  .  .  .  .
      |05 .  .  .  .  .  .  .  .  .  .
      |06 .  M  M  M  .  M  M  .  .  .
      |07 .  .  v  .  .  v  .  .  .  .
      |08 .  .  .  .  .  .  .  .  .  .
      |09 .  .  .  .  .  .  .  .  .  .
      |10 .  .  .  .  .  .  .  .  .  .
    """

  val vanillaBoard =
    """
      |-- 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 34 25
      |01 .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
      |02 .  .  .  .  .  .  .  F  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
      |03 .  .  .  .  .  .  .  .  .  M  M  M  M  .  .  .  .  .  .  .  .  .  .  .  .
      |04 .  .  .  .  .  .  .  .  .  M  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
      |05 .  .  .  .  .  .  .  .  .  M  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
      |06 .  .  .  .  .  .  .  .  .  =  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
      |07 .  .  .  .  .  .  .  .  .  M  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
      |08 .  .  .  .  .  .  .  .  .  M  .  .  .  .  .  .  .  .  .  .  F  .  .  .  .
      |09 .  .  .  .  .  .  .  .  .  M  .  .  F  .  .  .  .  .  .  .  .  .  .  .  .
      |10 .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
      |11 .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
      |12 .  .  .  .  .  .  .  .  .  .  .  .  .  .  F  .  .  .  .  .  .  .  .  .  .
      |13 .  .  F  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
      |14 .  .  .  .  .  .  .  .  .  .  M  M  M  M  M  M  .  .  .  .  .  .  .  .  .
      |15 .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  =  .  .  .  .  .  .  F  .  .
      |16 .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  M  .  .  .  .  .  .  .  .  .
      |17 .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  M  .  .  .  .  .  .  .  .  .
      |18 .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  M  .  .  .  .  .  .  .  .  .
      |19 .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
      |20 .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
    """

  val vanillaUnits =
    """
      |-- 01 02 03 04 05 06 07 08 09 10 11 12 13 14 15 16 17 18 19 20 21 22 23 34 25
      |01 .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
      |02 .  .  .  .  .  .  .  F  .  .  .  .  .  .  A  .  .  .  .  .  .  .  .  .  .
      |03 .  .  .  .  .  .  .  .  .  M  M  M  M  .  .  .  .  .  .  .  .  .  .  .  .
      |04 .  .  R  .  .  .  .  A  .  M  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
      |05 .  .  .  .  .  .  .  .  .  M  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
      |06 .  .  .  .  E  .  .  .  .  I  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
      |07 .  .  V  V  .  I  I  C  I  M  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
      |08 .  .  V  V  I  N  I  I  I  M  .  .  .  .  .  .  .  .  .  .  F  .  .  .  .
      |09 .  .  .  .  .  I  .  .  .  M  .  .  F  .  .  .  .  .  .  .  .  .  .  .  .
      |10 .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
      |11 .  .  .  .  .  .  .  .  .  .  .  .  .  .  i  i  i  c  v  .  .  .  .  .  .
      |12 .  .  .  .  .  .  .  .  .  .  .  .  .  .  i  i  i  v  v  .  .  .  .  .  .
      |13 .  .  F  .  .  .  .  .  .  .  .  .  .  .  i  i  i  v  .  .  .  .  .  .  .
      |14 .  .  .  .  .  .  .  .  .  .  M  M  M  M  M  M  e  .  .  .  .  .  .  .  .
      |15 .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  n  .  .  .  .  .  .  r  .  .
      |16 .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  M  .  .  .  .  .  .  .  .  .
      |17 .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  M  .  .  .  .  .  .  .  .  .
      |18 .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  M  .  .  .  .  .  .  .  .  .
      |19 .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
      |20 .  .  a  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  a  .  .
    """

}
