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
  def all = Set(N, NE, E, SE, S, SW, W, NW, SOURCE)
}
