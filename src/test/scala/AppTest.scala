import BoardPositions._
import com.lms.gow.model.repo.TileRepository._
import com.lms.gow.model.repo.{PlayerRepository, RuleRepository}
import utest._

object AppTest extends TestSuite {

  val tests = TestSuite {

    'vanilla {

      val game = setupGame(vanillaBoard, vanillaUnits)

      'boardDimensions {
        assert(RuleRepository.startingTerrain.size == 25 * 20)
        assert(RuleRepository.startingUnits.size == 25 * 20)
      }

      'terrainPlacement {
        assert(get(8, 2).terrain.equals(Fortress))
        assert(get(10, 6).terrain.equals(MountainPass))
        assert(get(10, 7).terrain.equals(Mountain))
        assert(get(11, 9).terrain.equals(VoidTile))
      }

      'unitPlacement {
        assert(get(3, 4).unit.equals(RedRelay))
        assert(get(8, 4).unit.equals(RedArsenal))
        assert(get(10, 6).unit.equals(RedInfantry))
      }

      'unitDistribution {
        val totalRed = game.gameSquares.filter(_.unit.player equals PlayerRepository.Red).size
        val totalBlue = game.gameSquares.filter(_.unit.player equals PlayerRepository.Blue).size
        assert(totalRed equals totalBlue)
      }

      'unitGameTurn {
        val infantry = get(15, 11)
        assert(infantry.unit.equals(BlueInfantry))
        if (!infantry.isCurrentTurn)
          game.nextTurn()
        assert(infantry.isCurrentTurn)
        game.nextTurn()
        assert(!infantry.isCurrentTurn)
      }

      'unitBasicMovementRange {
        val infantry = getWithTurn(15, 11)
        val free = infantry.inRange(infantry.unit.speed)
          .filter(infantry.canMoveTo(_))
        assert(free.size.equals(5))

        val infantry2 = getWithTurn(16, 11)
        val free2 = infantry2.inRange(infantry.unit.speed)
          .filter(infantry2.canMoveTo(_))
        assert(free2.size.equals(3))

        val infantry3 = getWithTurn(16, 12)
        val free3 = infantry3.inRange(infantry.unit.speed)
          .filter(infantry3.canMoveTo(_))
        assert(free3.size.equals(0))

        val infantry4 = getWithTurn(17, 13)
        val free4 = infantry4.inRange(infantry.unit.speed)
          .filter(infantry4.canMoveTo(_))
        assert(free4.size.equals(1))
      }

    }

    'unitObstruction {

      setupGame(movementObstruction, movementObstruction, 10)

      val cavalry1 = getWithTurn(3, 7)
      assert(cavalry1.unit.equals(BlueCavalry))
      assert(!cavalry1.canMoveTo(get(3, 5)))

      val cavalry2 = getWithTurn(6, 7)
      assert(cavalry2.canMoveTo(get(6, 5)))

      val cavalry3 = getWithTurn(6, 3)
      assert(cavalry3.canMoveTo(get(6, 1)))

    }

    'communicationLines {

      val game = setupGame(communicationLines, communicationLines, 10)

      'relaysDoRelay {
        assert(!getWithTurn(10, 1).isOnline)
        assert(getWithTurn(3, 4).isOnline)
      }

      'outOfComUnitsCannotMove {
        val ooc = getWithTurn(10, 1)
        assert(!ooc.isOnline)
        assert(!ooc.canMove)
        assert(ooc.inRange(ooc.unit.speed)
          .filter(ooc.canMoveTo(_)).isEmpty)
      }

      'allInComRangeAreOnlineAndCanMove {
        assert(game.gameSquares
          .filter(_.unit.equals(BlueInfantry))
          .filter(_.canMove)
          .filter(_.isOnline).size == 4)
      }

      'enemyUnitBlocksCom {
        getWithTurn(1, 7).moveUnitTo(get(2, 7))
        game.nextTurn()
        assert(game.gameSquares
          .filter(_.unit.equals(BlueInfantry))
          .filter(_.canMove)
          .filter(_.isOnline).size == 2)
        get(1, 7).unit = get(2, 7).unit
        get(2, 6).unit = VoidTile
        game.refreshComLayer()
      }

      'unitCanMoveOutOfComEvenIfItMakesItOffline {
        assert(getWithTurn(4, 8).isOnline)
        assert(get(5, 7).unit.equals(VoidTile))
        getWithTurn(4, 8).moveUnitTo(get(5, 7))
        assert(get(4, 8).unit.equals(VoidTile))
        assert(!getWithTurn(5, 7).isOnline)
        assert(game.gameSquares
          .filter(_.unit.equals(BlueInfantry))
          .filter(_.canMove)
          .filter(_.isOnline).size == 3)
      }

    }

  }

}