package com.lms.gow.ui

import com.lms.gow.model.repo.PlayerRepository
import com.lms.gow.model.repo.PlayerRepository.Player

object Color {
  def rgb(r: Int, g: Int, b: Int) = s"rgb($r, $g, $b)"
  def rgba(r: Int, g: Int, b: Int, a: Int) = s"rgba($r, $g, $b, $a)"
  val White = rgb(255, 255, 255)
  val Silver = rgb(247, 247, 247)
  val Blue = rgb(0, 0, 255)
  val Red = rgb(255, 0, 0)
  val Green = rgb(0, 255, 0)
  val Orange = rgb(255, 255, 0)
  val Gray = rgb(128, 128, 128)
  val Highlight = rgb(255, 255, 0)
  def fromPlayer(pl: Player): String = {
    if (pl.equals(PlayerRepository.Blue))
      Blue
    else if (pl.equals(PlayerRepository.Red))
      Red
    else
      Gray
  }
}