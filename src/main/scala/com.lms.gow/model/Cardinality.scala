package com.lms.gow.model

object Cardinality {
  abstract case class Direction(y: Int, x: Int)
  object N extends Direction(-1, 0)
  object NE extends Direction(-1, 1)
  object E extends Direction(0, 1)
  object SE extends Direction(1, 1)
  object S extends Direction(1, 0)
  object SW extends Direction(1, -1)
  object W extends Direction(0, -1)
  object NW extends Direction(-1, -1)
}
