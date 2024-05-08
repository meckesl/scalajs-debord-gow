package com.lms.gow.io

import com.lms.gow.model.GameSquare
import com.lms.gow.model.repo.TileRepository.{Tile, VoidTile}
import com.lms.gow.model.repo.{RuleRepository, TileRepository}
import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.raw.{HTMLImageElement, XMLHttpRequest}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

object Loader {

  val imageCache: mutable.HashMap[Tile, HTMLImageElement] = new mutable.HashMap()

  def getSoundUrl(sq: GameSquare, sound: String): String = {
    s"target/scala-2.13/classes/sounds/${sq.unit.char}/$sound.mp3"
  }

  def getSoundUrl(sound: String): String = {
    s"target/scala-2.13/classes/sounds/$sound.mp3"
  }

  def getTileAsync(t: Tile, callback: (HTMLImageElement) => Unit): Unit = {

    def getTileUrl(tile: TileRepository.Tile): String = {
      var s = ""
      if (tile.equals(VoidTile))
        s = "dot"
      else
        s = tile.char.toString
      s"target/scala-2.13/classes/tiles/$s/0.png"
    }

    if (imageCache.contains(t)) {
      callback(imageCache(t))
    } else {
      val image = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
      image.src = getTileUrl(t)
      image.onload = (_: dom.Event) => {
        imageCache.put(t, image)
        callback(image)
      }
    }
  }

  def getStartingGamePosition(boardFile: String, unitFile: String, xWidth: Int): Future[Boolean] = {

    def getResUrl(res: String): String = {
      s"target/scala-2.13/classes/$res"
    }

    def loadInitialBoardPosition(file: String, tiles: Set[Tile]): Future[Seq[Tile]] = {
      val promise: Promise[Seq[Tile]] = Promise()
      val xhr = new XMLHttpRequest()
      xhr.open("GET", getResUrl(file))
      xhr.onload = {
        (e: Event) =>
          if (xhr.status == 200) {
            promise.success(
              xhr.responseText
                .filter(TileRepository.all.map(_.char).contains(_))
                .map(TileRepository.getByChar))
          }
          else
            promise.failure(new RuntimeException("cannot read tiles"))
      }
      xhr.send()
      promise.future
    }

    val loaded: Promise[Boolean] = Promise()
    loadInitialBoardPosition(boardFile, TileRepository.terrains) foreach  {
      case tilesT: Seq[Tile] => {
        RuleRepository.startingTerrain = Some(tilesT.map(t => {
          if (TileRepository.terrains.contains(t)) t else VoidTile
        }))
        loadInitialBoardPosition(unitFile, TileRepository.units) foreach {
          case tilesU: Seq[Tile] => {
            RuleRepository.startingUnits = tilesU.map(u => {
              if (TileRepository.units.contains(u)) u else VoidTile
            })

            RuleRepository.squareX = xWidth
            RuleRepository.squareY = RuleRepository.startingTerrain.size / xWidth

            loaded.success(true)
          }
        }
      }
    }
    loaded.future
  }

}