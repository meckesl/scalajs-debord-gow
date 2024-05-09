package com.lms.gow.model

case class Point(x: Double, y: Double) {
  def +(other: Point) = Point(x + other.x, y + other.y)
  def -(other: Point) = Point(x - other.x, y - other.y)
  def %(other: Point) = Point(x % other.x, y % other.y)
  def <(other: Point) = x < other.x && y < other.y
  def >(other: Point) = x > other.x && y > other.y
  def /(value: Double) = Point(x / value, y / value)
  def /(other: Point) = Point(x / other.x, y / other.y)
  def *(value: Double) = Point(x * value, y * value)
  def *(other: Point) = Point(x * other.x, y * other.y)
  def length = Math.sqrt(lengthSquared)
  def lengthSquared = x * x + y * y
  def within(a: Point, b: Point, extra: Point = Point(0, 0)) = {
    import math.{max, min}
    x >= min(a.x, b.x) - extra.x &&
      x < max(a.x, b.x) + extra.y &&
      y >= min(a.y, b.y) - extra.x &&
      y < max(a.y, b.y) + extra.y
  }
  def rotate(theta: Double) = {
    val (cos, sin) = (Math.cos(theta), math.sin(theta))
    Point(cos * x - sin * y, sin * x + cos * y)
  }
  def toLinear(refWidth: Int) : Int = math.round((y * refWidth + x)).toInt
}

object Point {
  def fromLinear(linear: Int, refWidth: Int): Point =
    Point(linear % refWidth, linear / refWidth)
}