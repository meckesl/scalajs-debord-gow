
package com.lms.gow

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.Stage

import com.lms.gow.model._
import com.lms.gow.ui.BoardPane

class AppX extends Application {

  val game = new Game
  val board = new BoardPane(game)
  val status = new Pane
  val scene = new Scene(board, Color.WHITESMOKE)

  def start(stage: Stage) {
    stage setTitle ("Game of War")
    stage setScene (scene)
    stage show
  }

}

object AppX {

  def main(args: Array[String]) {
    Application.launch(classOf[App], args: _*)
  }

}
