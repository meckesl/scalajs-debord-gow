package com.lms.gow.model.repo

object CardinalityRepository {
  abstract case class Cardinality(y: Int, x: Int)
  object N extends Cardinality(-1, 0)
  object NE extends Cardinality(-1, 1)
  object E extends Cardinality(0, 1)
  object SE extends Cardinality(1, 1)
  object S extends Cardinality(1, 0)
  object SW extends Cardinality(1, -1)
  object W extends Cardinality(0, -1)
  object NW extends Cardinality(-1, -1)
  object SOURCE extends Cardinality(0, 0)
  def all: Set[Cardinality] = Set(N, NE, E, SE, S, SW, W, NW, SOURCE)
  def opposite(c: Cardinality): Cardinality = {
    c match {
      case N => S
      case NE => SW
      case E => W
      case SE => NW
      case S => N
      case SW => NE
      case W => E
      case NW => SE
      case SOURCE => SOURCE
    }
  }
}
