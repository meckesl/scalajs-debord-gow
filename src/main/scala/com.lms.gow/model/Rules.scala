package com.lms.gow.model

import com.lms.gow.io.Loader
import com.lms.gow.model.repo.TileRepository
import com.lms.gow.model.repo.TileRepository.Tile
import org.scalajs.dom.{Event, XMLHttpRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

object Rules {

  val terrainWidth = 25
  val terrainHeight = 20
  val totalTiles = terrainWidth * terrainHeight
  val movesPerTurn = 5
  val attacksPerTurn = 1
  var startingTerrain: Seq[Tile] = null
  var startingUnits: Seq[Tile] = null

  def load(): Future[Boolean] = {

    def loadInitialBoardPosition(file: String, tiles: Set[Tile]): Future[Seq[Tile]] = {
      val promise: Promise[Seq[Tile]] = Promise()
      val xhr = new XMLHttpRequest()
      xhr.open("GET", Loader.getResUrl(file))
      xhr.onload = {
        (e: Event) =>
          if (xhr.status == 200)
            promise.success(
              xhr.responseText.filter(_ > ' ')
                .map(TileRepository.getByChar(_)))
          else
            promise.failure(new RuntimeException("cannot read tiles"))
      }
      xhr.send()
      promise.future
    }

    val loaded: Promise[Boolean] = Promise()
    loadInitialBoardPosition("init.board", TileRepository.terrains) onSuccess {
      case tiles: Seq[Tile] => {
        startingTerrain = tiles
        loadInitialBoardPosition("init.units", TileRepository.units) onSuccess {
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