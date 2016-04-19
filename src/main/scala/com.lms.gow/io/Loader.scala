package com.lms.gow.io

import com.lms.gow.model.TileRepository

object Loader {

  def getResUrl(res: String): String = {
    s"target/scala-2.11/classes/$res"
  }

  def getTileUrl(tile: TileRepository.Tile): String = {
    s"target/scala-2.11/classes/tiles/${tile.char}.png"
  }

}