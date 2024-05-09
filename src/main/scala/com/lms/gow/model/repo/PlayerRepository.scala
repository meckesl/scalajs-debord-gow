package com.lms.gow.model.repo

object PlayerRepository {
  abstract case class Player(c: Char)
  object Red extends Player('r')
  object Blue extends Player('b')
  object Neutral extends Player('n')
}
