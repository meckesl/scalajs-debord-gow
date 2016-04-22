package com.lms.gow.io

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

  def getResUrl(res: String): String = {
    s"target/scala-2.11/classes/$res"
  }

  private def getTileUrl(tile: TileRepository.Tile): String = {
    var s = ""
    if (tile.equals(VoidTile))
      s = "dot"
    else
      s = tile.char.toString
    s"target/scala-2.11/classes/tiles/$s/0.png"
  }

  def getTileAsync(t: Tile, callback: (HTMLImageElement) => Unit) {
    if (imageCache.get(t).isDefined) {
      callback(imageCache.get(t).get)
    } else {
      val image = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
      image.src = Loader.getTileUrl(t)
      image.onload = (e: dom.Event) => {
        imageCache.put(t, image)
        callback(image)
      }
    }
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
      case tilesT: Seq[Tile] => {
        RuleRepository.startingTerrain = tilesT.map(t => {
          if (TileRepository.terrains.contains(t)) t else VoidTile
        })
        loadInitialBoardPosition("fight.units", TileRepository.units) onSuccess {
          case tilesU: Seq[Tile] => {
            RuleRepository.startingUnits = tilesU.map(u => {
              if (TileRepository.units.contains(u)) u else VoidTile
            })
            loaded.success(true)
          }
        }
      }
    }
    loaded.future
  }

}