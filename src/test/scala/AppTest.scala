import BoardPositions._
import com.lms.gow.model.repo.TileRepository._
import com.lms.gow.model.repo.{PlayerRepository, RuleRepository}
import utest._

object AppTest extends TestSuite {

  val tests: Tests = utest.Tests {

    Symbol("vanilla") {

      val game = setupGame(vanilla._1, vanilla._2)

      Symbol("boardDimensions") {
        assert(RuleRepository.startingTerrain.size == 25 * 20)
        assert(RuleRepository.startingUnits.size == 25 * 20)
      }

      Symbol("terrainPlacement") {
        assert(get(8, 2).terrain.equals(Fortress))
        assert(get(10, 6).terrain.equals(MountainPass))
        assert(get(10, 7).terrain.equals(Mountain))
        assert(get(11, 9).terrain.equals(VoidTile))

      }

      Symbol("unitPlacement") {
        assert(get(3, 4).unit.equals(RedRelay))
        assert(get(8, 4).unit.equals(RedArsenal))
        assert(get(10, 6).unit.equals(RedInfantry))

      }

      Symbol("tileDistribution") {
        val totalRed = game.gameSquares.filter(_.unit.player equals PlayerRepository.Red).size
        val totalBlue = game.gameSquares.filter(_.unit.player equals PlayerRepository.Blue).size
        assert(totalRed equals totalBlue)
        assert(game.gameSquares.count(_.terrain.equals(Mountain)) == 9 * 2)
        assert(game.gameSquares.count(sq => Seq(RedArsenal, BlueArsenal)
          .contains(sq.unit)) == 2 * 2)
      }

      Symbol("unitGameTurn") {
        val infantry = get(15, 11)
        assert(infantry.unit.equals(BlueInfantry))
        if (!infantry.isCurrentTurn)
          game.nextTurn()
        assert(infantry.isCurrentTurn)
        game.nextTurn()
        assert(!infantry.isCurrentTurn)
      }

      Symbol("unitBasicMovementRange") {
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

    Symbol("unitObstruction") {

      setupGame(movementObstruction, movementObstruction, 10)

      val cavalry1 = getWithTurn(3, 7)
      assert(cavalry1.unit.equals(BlueCavalry))
      assert(!cavalry1.canMoveTo(get(3, 5)))

      val cavalry2 = getWithTurn(6, 7)
      assert(cavalry2.canMoveTo(get(6, 5)))

      val cavalry3 = getWithTurn(6, 3)
      assert(cavalry3.canMoveTo(get(6, 1)))

    }

    Symbol("combatFromRulesPDF") {

      setupGame(combatFromPDF, combatFromPDF, 5) // as described in liamgillick PDF

      Symbol("defensePtsOn44") {
        assert(getWithTurn(4, 4).defenseStrength.equals(19))
      }

      Symbol("attackPtsOn44") {
        assert(getWithTurn(4, 4).canBeTargetOfStrength.equals(23 - cavalryChargeBonus)) //FIXME
      }

      Symbol("doesNotContributeDefense") {
        assert(getWithTurn(4, 4).alliesInRange.contains(get(1, 4)))
        assert(!getWithTurn(4, 4).alliesInRange.contains(get(2, 5)))
      }

      /*'attackResultOn44 {
        getWithTurn(3,3)
        assert(get(4,4).launchAttackOn().equals(2))
      }*/

    }

    Symbol("cavalryCharge") {

      setupGame(cavalryCharge._1, cavalryCharge._2, 5)

      Symbol("normalTerrainCavalryChargeHasBonus") {
        getWithTurn(5, 1)
        assert(get(4, 1).canBeTargetOfStrength.equals(4 + cavalryChargeBonus))
      }

      Symbol("noCavalryChargeFromFortress") {
        getWithTurn(5, 5)
        assert(get(4, 5).canBeTargetOfStrength.equals(4))
      }

      Symbol("noCavalryChargeToFortress") {
        getWithTurn(4, 5)
        assert(get(5, 5).canBeTargetOfStrength.equals(4))
      }

      Symbol("noCavalryChargeToMountainPass") {
        getWithTurn(2, 3)
        assert(get(1, 2).canBeTargetOfStrength.equals(4))
      }

      Symbol("cavalryChargeOKFromMountainPass") {
        getWithTurn(1, 2)
        assert(get(2, 3).canBeTargetOfStrength.equals(4 + cavalryChargeBonus))
      }

    }

    Symbol("communicationLines") {

      val game = setupGame(communicationLines, communicationLines, 10)

      Symbol("relaysDoRelay") {
        assert(!getWithTurn(10, 1).isOnline)
        assert(getWithTurn(3, 4).isOnline)
      }

      Symbol("outOfComUnitsCannotMove") {
        val ooc = getWithTurn(10, 1)
        assert(!ooc.isOnline)
        assert(!ooc.canMove)
        assert(ooc.inRange(ooc.unit.speed)
          .filter(ooc.canMoveTo(_)).isEmpty)
      }

      Symbol("allInComRangeAreOnlineAndCanMove") {
        getWithTurn(3, 8)
        assert(game.gameSquares
          .filter(_.unit.equals(BlueInfantry))
          .filter(_.canMove)
          .filter(_.isOnline).size == 4)
      }

      Symbol("enemyUnitBlocksCom") {
        getWithTurn(1, 7).moveUnitTo(get(2, 7))
        game.nextTurn()
        assert(game.gameSquares
          .filter(_.unit.equals(BlueInfantry))
          .filter(_.canMove)
          .filter(_.isOnline).size == 2)
        // Clear enemy unit
        get(1, 7).unit = get(2, 7).unit
        get(2, 6).unit = VoidTile
      }

      Symbol("unitCanMoveOutOfCom") {
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