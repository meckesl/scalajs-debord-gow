package com.lms.gow.io

import com.lms.gow.model.Tile.Tile

object Loader {

  def getResUrl(res: String) : String = {
    s"target/scala-2.11/classes/$res"
  }

  def getTileUrl(tile: Tile) : String = {
    s"target/scala-2.11/classes/tiles/${tile.char}.png"
  }

}
