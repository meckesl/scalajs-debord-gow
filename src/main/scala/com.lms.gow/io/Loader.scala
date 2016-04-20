package com.lms.gow.io

import com.lms.gow.model.repo.TileRepository.{Tile, VoidTile}
import com.lms.gow.model.repo.{RuleRepository, TileRepository}
import org.scalajs.dom._
import org.scalajs.dom.raw.XMLHttpRequest

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

object Loader {

  def getResUrl(res: String): String = {
    s"target/scala-2.11/classes/$res"
  }

  def getTileUrl(tile: TileRepository.Tile): String = {
    var s = ""
    if (tile.equals(VoidTile))
      s = "dot"
    else
      s = tile.char.toString
    s"target/scala-2.11/classes/tiles/$s.png"
  }

  def loadStartingGamePosition(): Future[Boolean] = {

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
        RuleRepository.startingTerrain = tiles
        loadInitialBoardPosition("init.units", TileRepository.units) onSuccess {
          case tiles: Seq[Tile] => {
            RuleRepository.startingUnits = tiles
            loaded.success(true)
          }
        }
      }
    }
    loaded.future
  }

}