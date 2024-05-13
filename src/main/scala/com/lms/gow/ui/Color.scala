package com.lms.gow.ui

import com.lms.gow.model.repo.PlayerRepository
import com.lms.gow.model.repo.PlayerRepository.Player

object Color {
  private def rgb(r: Int, g: Int, b: Int) = s"rgb($r, $g, $b)"
  private def rgba(r: Int, g: Int, b: Int, a: Int) = s"rgba($r, $g, $b, $a)"
  val Black: String = rgb(0, 0, 0)
  val White: String = rgb(255, 255, 255)
  val Silver: String = rgb(247, 247, 247)
  val Blue: String = rgb(0, 0, 255)
  val Red: String = rgb(255, 0, 0)
  val Green: String = rgb(0, 255, 0)
  val Orange: String = rgb(255, 255, 0)
  val Gray: String = rgb(128, 128, 128)
  val Highlight: String = rgb(255, 255, 0)

  def fromPlayer(pl: Player): String = {
    pl match {
      case PlayerRepository.Red => Red
      case PlayerRepository.Blue => Blue
      case PlayerRepository.Neutral => Gray
      case _ => Gray
    }
  }
}