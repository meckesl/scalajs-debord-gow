package com.github.meckesl

case class Point(x: Double, y: Double) {
  def +(other: Point): Point = Point(x + other.x, y + other.y)
  def -(other: Point): Point = Point(x - other.x, y - other.y)
  def %(other: Point): Point = Point(x % other.x, y % other.y)
  def <(other: Point): Boolean = x < other.x && y < other.y
  def >(other: Point): Boolean = x > other.x && y > other.y
  def /(value: Double): Point = Point(x / value, y / value)
  def /(other: Point): Point = Point(x / other.x, y / other.y)
  def *(value: Double): Point = Point(x * value, y * value)
  def *(other: Point): Point = Point(x * other.x, y * other.y)
  def length: Double = Math.sqrt(lengthSquared)
  private def lengthSquared = x * x + y * y
  def within(a: Point, b: Point, extra: Point = Point(0, 0)): Boolean = {
    import math.{max, min}
    x >= min(a.x, b.x) - extra.x &&
      x < max(a.x, b.x) + extra.y &&
      y >= min(a.y, b.y) - extra.x &&
      y < max(a.y, b.y) + extra.y
  }
  def rotate(theta: Double): Point = {
    val (cos, sin) = (Math.cos(theta), math.sin(theta))
    Point(cos * x - sin * y, sin * x + cos * y)
  }
  def toLinear(refWidth: Int) : Int = (y * refWidth + x).toInt
}

object Point {
  def fromLinear(linear: Int, refWidth: Int): Point =
    Point(linear % refWidth, linear / refWidth)
}