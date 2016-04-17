package com.lms.gow.model

import org.scalajs.dom

import scala.collection.Seq

object Coordinates {

  def indexFromCoordinates(x: Int, y: Int) = (x + (y * Rules.terrainWidth))

}

object IO {

  def loadTilesFromFile(file: String, tileRepository: Seq[Tile]): Seq[Tile] = {

    val xhr = new dom.XMLHttpRequest()

    xhr.open("GET",
      s"target/scala-2.11/classes/$file");

    xhr.onload = { (e: dom.Event) =>
      if (xhr.status == 200) {
        val ret =
          xhr.responseText.filter(_ > ' ')
            .map((tileRepository.map(_.char) zip tileRepository)
              .toMap
              .get(_)
              .getOrElse(VoidTile))
      }
    }
    xhr.send()

    return List(VoidTile)

    /*io.Source.fromFile(file)
      .mkString
      .filter(_ > ' ')
      .map((tileRepository.map(_.char) zip tileRepository)
        .toMap
        .get(_)
        .getOrElse(VoidTile))*/
  }

}
