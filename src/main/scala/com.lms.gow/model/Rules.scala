package com.lms.gow.model

import com.lms.gow.model.Cardinality._
import com.lms.gow.model.Tile._
import org.scalajs.dom.{Event, XMLHttpRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

object Rules {

  val terrainWidth = 25
  val terrainHeight = 20
  val totalTiles = terrainWidth * terrainHeight
  val movesPerTurn = 5
  val attacksPerTurn = 1
  val terrainTilesRepository = Seq(Fortress, Mountain, MountainPass)
  val unitTilesRepository = Seq(BlueCannon, BlueSwiftCannon, BlueRelay, BlueSwiftRelay, BlueInfantry, BlueCavalry, BlueArsenal,
    RedCannon, RedSwiftCannon, RedRelay, RedSwiftRelay, RedInfantry, RedCavalry, RedArsenal)
  var startingTerrain: Seq[Tile] = null
  var startingUnits: Seq[Tile] = null
  val directions = Seq(N, NE, E, SE, S, SW, W, NW)

  def load(): Future[Boolean] = {

    def loadTiles(file: String, tileRepository: Seq[Tile]): Future[Seq[Tile]] = {
      val promise: Promise[Seq[Tile]] = Promise()
      val xhr = new XMLHttpRequest()
      xhr.open("GET", s"target/scala-2.11/classes/$file")
      xhr.onload = {
        (e: Event) =>
          if (xhr.status == 200)
            promise.success(
              xhr.responseText.filter(_ > ' ')
                .map((tileRepository.map(_.char) zip tileRepository)
                  .toMap
                  .get(_)
                  .getOrElse(VoidTile)))
          else
            promise.failure(new RuntimeException("cannot read tiles"))
      }
      xhr.send()
      promise.future
    }

    val loaded: Promise[Boolean] = Promise()
    loadTiles("init.board", terrainTilesRepository) onSuccess {
      case tiles: Seq[Tile] => {
        startingTerrain = tiles
        loadTiles("init.units", unitTilesRepository) onSuccess {
          case tiles: Seq[Tile] => {
            startingUnits = tiles
            loaded.success(true)
          }
        }
      }
    }
    loaded.future
  }

}