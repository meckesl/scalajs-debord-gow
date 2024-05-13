import com.lms.gow.model.repo.TileRepository.VoidTile
import com.lms.gow.model.repo.{RuleRepository, TileRepository}
import com.lms.gow.model.{Game, GameSquare, Point}

object BoardPositions {

  private var game: Game = _

  def setupGame(t: String, u: String, width: Int = 25): Game = {

    assert(t.length.equals(u.length))
    val terrain = t.filter(_ > ' ').filter(TileRepository.all.map(_.char).contains(_))
    val units = u.filter(_ > ' ').filter(TileRepository.all.map(_.char).contains(_))
    assert(units.length.equals(terrain.length))

    RuleRepository.startingTerrain = terrain.map(t => {
      val tile = TileRepository.getByChar(t)
      if (TileRepository.terrains.contains(tile)) tile else VoidTile
    })
    RuleRepository.startingUnits = units.map(u => {
      val tile = TileRepository.getByChar(u)
      if (TileRepository.units.contains(tile)) tile else VoidTile
    })

    assert(RuleRepository.startingTerrain.size.equals(RuleRepository.startingUnits.size))

    RuleRepository.squareX = width
    RuleRepository.squareY = RuleRepository.startingTerrain.size / width

    assert((RuleRepository.squareX * RuleRepository.squareY).equals(RuleRepository.startingTerrain.size))

    game = new Game()
    game
  }

  def get(x: Int, y: Int): GameSquare = {
    game.gameSquares(new Point(x - 1, y - 1).toLinear(RuleRepository.squareX))
  }

  def getWithTurn(x: Int, y: Int): GameSquare = {
    val u = get(x, y)
    assert(u.unit != VoidTile)
    if (!u.isCurrentTurn)
      game.nextTurn()
    assert(u.isCurrentTurn)
    u
  }

  val cavalryCharge: (String, String) = (
    """
      |-- 01 02 03 04 05
      |01 M  .  .  .  .
      |02 =  .  .  .  .
      |03 M  .  .  .  .
      |04 .  .  .  .  .
      |05 .  .  .  .  F
    """,
    """
      |-- 01 02 03 04 05
      |01 M  .  .  I  v
      |02 v  .  .  .  a
      |03 M  V  .  R  .
      |04 .  .  .  .  .
      |05 .  A  .  V  v
    """)

  val combatFromPDF =
    """
      |-- 01 02 03 04 05
      |01 .  .  A  C  .
      |02 .  V  .  I  .
      |03 .  .  V  .  .
      |04 c  .  i  v  .
      |05 .  i  a  .  .
    """

  val communicationLines =
    """
      |-- 01 02 03 04 05 06 07 08 09 10
      |01 A  .  .  .  .  .  .  .  .  i
      |02 .  I  R  .  .  .  .  .  .  .
      |03 .  .  .  .  .  .  .  .  .  .
      |04 .  .  i  .  I  .  .  .  .  .
      |05 I  r  .  i  .  .  .  .  .  .
      |06 .  .  M  M  M  .  .  .  .  .
      |07 I  .  .  .  .  .  .  .  .  .
      |08 .  .  i  i  i  .  .  .  .  .
      |09 .  a  .  .  .  .  .  .  .  .
      |10 .  .  .  .  .  .  .  .  .  .
    """

  val movementObstruction =
    """
      |-- 01 02 03 04 05 06 07 08 09 10
      |01 .  .  .  .  .  .  .  .  .  .
      |02 .  .  .  .  M  M  .  .  .  .
      |03 .  .  .  .  .  v  .  .  .  .
      |04 .  .  .  .  r  .  .  .  .  .
      |05 .  .  .  .  .  .  .  .  .  .
      |06 .  M  M  M  .  M  M  .  .  .
      |07 .  .  v  .  a  v  .  .  .  .
      |08 .  .  .  .  .  .  .  .  .  .
      |09 .  .  .  .  .  .  .  .  .  .
      |10 .  .  .  .  .  .  .  .  .  .
    """

  val vanilla: (String, String) = (
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
    ,
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
    )

}
