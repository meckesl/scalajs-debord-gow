import BoardPositions._
import com.lms.gow.model.repo.TileRepository._
import com.lms.gow.model.repo.{PlayerRepository, RuleRepository}
import utest._

object AppTest extends TestSuite {

  val tests: Tests = Tests {

    test("vanilla") {

      val game = setupGame(vanilla._1, vanilla._2)

      test("boardDimensions") {
        assert(RuleRepository.startingTerrain.size == 25 * 20)
        assert(RuleRepository.startingUnits.size == 25 * 20)
      }

      test("terrainPlacement") {
        assert(get(8, 2).terrain.equals(Fortress))
        assert(get(10, 6).terrain.equals(MountainPass))
        assert(get(10, 7).terrain.equals(Mountain))
        assert(get(11, 9).terrain.equals(VoidTile))
      }

      test("unitPlacement") {
        assert(get(3, 4).unit.equals(RedRelay))
        assert(get(8, 4).unit.equals(RedArsenal))
        assert(get(10, 6).unit.equals(RedInfantry))
      }

      test("tileDistribution") {
        val totalRed = game.gameSquares.count(_.unit.player equals PlayerRepository.Red)
        val totalBlue = game.gameSquares.count(_.unit.player equals PlayerRepository.Blue)
        assert(totalRed equals totalBlue)
        assert(game.gameSquares.count(_.terrain.equals(Mountain)) == 9 * 2)
        assert(game.gameSquares.count(sq => Seq(RedArsenal, BlueArsenal)
          .contains(sq.unit)) == 2 * 2)
      }

      test("unitGameTurn") {
        val infantry = get(15, 11)
        assert(infantry.unit.equals(BlueInfantry))
        if (!infantry.isCurrentTurn)
          game.nextTurn()
        assert(infantry.isCurrentTurn)
        game.nextTurn()
        assert(!infantry.isCurrentTurn)
      }

      test("unitBasicMovementRange") {
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
        assert(free3.isEmpty)

        val infantry4 = getWithTurn(17, 13)
        val free4 = infantry4.inRange(infantry.unit.speed)
          .filter(infantry4.canMoveTo(_))
        assert(free4.size.equals(1))
      }

    }

    test("unitObstruction") {

      setupGame(movementObstruction, movementObstruction, 10)

      val cavalry1 = getWithTurn(3, 7)
      assert(cavalry1.unit.equals(BlueCavalry))
      assert(!cavalry1.canMoveTo(get(3, 5)))

      val cavalry2 = getWithTurn(6, 7)
      assert(cavalry2.canMoveTo(get(6, 5)))

      val cavalry3 = getWithTurn(6, 3)
      assert(cavalry3.canMoveTo(get(6, 1)))

    }

    test("combatFromRulesPDF") {

      setupGame(combatFromPDF, combatFromPDF, 5) // as described in liamgillick PDF

      test("defensePtsOn44") {
        assert(getWithTurn(4, 4).defenseStrength.equals(19))
      }

      test("attackPtsOn44") {
        assert(getWithTurn(4, 4).canBeTargetOfStrength.equals(23 - cavalryChargeBonus)) //FIXME
      }

      test("doesNotContributeDefense") {
        assert(getWithTurn(4, 4).alliesInRange.contains(get(1, 4)))
        assert(!getWithTurn(4, 4).alliesInRange.contains(get(2, 5)))
      }

      /*'attackResultOn44 {
        getWithTurn(3,3)
        assert(get(4,4).launchAttackOn().equals(2))
      }*/

    }

    test("cavalryCharge") {

      setupGame(cavalryCharge._1, cavalryCharge._2, 5)

      test("normalTerrainCavalryChargeHasBonus") {
        getWithTurn(5, 1)
        assert(get(4, 1).canBeTargetOfStrength.equals(4 + cavalryChargeBonus))
      }

      test("noCavalryChargeFromFortress") {
        getWithTurn(5, 5)
        assert(get(4, 5).canBeTargetOfStrength.equals(4))
      }

      test("noCavalryChargeToFortress") {
        getWithTurn(4, 5)
        assert(get(5, 5).canBeTargetOfStrength.equals(4))
      }

      test("noCavalryChargeToMountainPass") {
        getWithTurn(2, 3)
        assert(get(1, 2).canBeTargetOfStrength.equals(4))
      }

      test("cavalryChargeOKFromMountainPass") {
        getWithTurn(1, 2)
        assert(get(2, 3).canBeTargetOfStrength.equals(4 + cavalryChargeBonus))
      }

    }

    test("communicationLines") {

      val game = setupGame(communicationLines, communicationLines, 10)

      test("relaysDoRelay") {
        assert(!getWithTurn(10, 1).isOnline)
        assert(getWithTurn(3, 4).isOnline)
      }

      test("outOfComUnitsCannotMove") {
        val ooc = getWithTurn(10, 1)
        assert(!ooc.isOnline)
        assert(!ooc.canMove)
        assert(!ooc.inRange(ooc.unit.speed).exists(ooc.canMoveTo(_)))
      }

      test("allInComRangeAreOnlineAndCanMove") {
        getWithTurn(3, 8)
        assert(game.gameSquares
          .filter(_.unit.equals(BlueInfantry))
          .filter(_.canMove).count(_.isOnline) == 5)
      }

      test("enemyUnitBlocksCom") {
        getWithTurn(1, 7).moveUnitTo(get(2, 7))
        game.nextTurn()
        assert(game.gameSquares
          .filter(_.unit.equals(BlueInfantry))
          .filter(_.canMove).count(_.isOnline) == 3)
        // Clear enemy unit
        get(1, 7).unit = get(2, 7).unit
        get(2, 6).unit = VoidTile
      }

      test("unitCanMoveOutOfCom") {
        assert(getWithTurn(4, 8).isOnline)
        assert(get(5, 7).unit.equals(VoidTile))
        getWithTurn(4, 8).moveUnitTo(get(5, 7))
        assert(get(4, 8).unit.equals(VoidTile))
        assert(!getWithTurn(5, 7).isOnline)
        assert(game.gameSquares
          .filter(_.unit.equals(BlueInfantry))
          .filter(_.canMove).count(_.isOnline) == 3)
      }

    }

  }

}