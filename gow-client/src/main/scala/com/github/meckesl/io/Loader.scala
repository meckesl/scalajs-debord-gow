package com.github.meckesl.io

import com.github.meckesl.Square
import com.github.meckesl.repo.{PlayerRepository, RuleRepository, TileRepository}
import com.github.meckesl.repo.TileRepository.{Tile, VoidTile}
import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.{HTMLImageElement, XMLHttpRequest}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

object Loader {

  val imageCache: mutable.HashMap[Tile, HTMLImageElement] = new mutable.HashMap()
  private val resourcePath = "target/scala-2.13/classes"

  def getSoundUrl(sq: Square, sound: String): String = {
    s"$resourcePath/sounds/${sq.unit.char}/$sound.mp3"
  }

  def getSoundUrl(sound: String): String = {
    s"$resourcePath/sounds/$sound.mp3"
  }

  def loadTileAsync(t: Tile, callback: HTMLImageElement => Unit): Unit = {

    def getTileUrl(tile: TileRepository.Tile): String = {
      if (tile.equals(VoidTile))
        s"$resourcePath/tiles/dot/0.png"
      else if (TileRepository.units.contains(tile)) {
        val playerTag = if (tile.player.equals(PlayerRepository.Red)) "r" else ""
        s"$resourcePath/tiles/${tile.char.toString}/0$playerTag.png"
      } else
        s"$resourcePath/tiles/${tile.char.toString}/0.png"
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
      s"$resourcePath/$res"
    }

    def loadInitialBoardPosition(file: String): Future[Seq[Tile]] = {
      val promise: Promise[Seq[Tile]] = Promise()
      val xhr = new XMLHttpRequest()
      xhr.open("GET", getResUrl(file))
      xhr.onload = {
        (_: Event) =>
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
    loadInitialBoardPosition(boardFile) foreach {
      tilesT: Seq[Tile] => {
        RuleRepository.startingTerrain = tilesT.map(t => {
          if (TileRepository.terrains.contains(t)) t else VoidTile
        })
        loadInitialBoardPosition(unitFile) foreach {
          tilesU: Seq[Tile] => {
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