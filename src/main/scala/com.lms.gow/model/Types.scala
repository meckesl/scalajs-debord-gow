package com.lms.gow.model

abstract sealed case class Tile(char: Char, speed: Int, range: Int, attack: Int,
                                defense: Int, isUnit: Boolean, isCom: Boolean, isBlue: Boolean)

object VoidTile extends Tile('.', 0, 0, 0, 0, false, false, false)
object Fortress extends Tile('F', 0, 0, 0, 4, false, false, false)
object Mountain extends Tile('M', 0, 0, 0, 0, false, false, false)
object MountainPass extends Tile('=', 0, 0, 0, 2, false, false, false)
object RedArsenal extends Tile('A', 0, 0, 0, 0, true, true, false)
object RedRelay extends Tile('R', 1, 0, 0, 1, true, true, false)
object RedSwiftRelay extends Tile('E', 2, 0, 0, 1, true, true, false)
object RedCannon extends Tile('C', 1, 3, 5, 8, true, false, false)
object RedSwiftCannon extends Tile('N', 2, 3, 5, 8, true, false, false)
object RedInfantry extends Tile('I', 1, 2, 4, 6, true, false, false)
object RedCavalry extends Tile('V', 2, 2, 4, 5, true, false, false)
object BlueArsenal extends Tile('a', 0, 0, 0, 0, true, true, true)
object BlueRelay extends Tile('r', 1, 0, 0, 1, true, true, true)
object BlueSwiftRelay extends Tile('e', 2, 0, 0, 1, true, true, true)
object BlueCannon extends Tile('c', 1, 3, 5, 8, true, false, true)
object BlueSwiftCannon extends Tile('n', 2, 3, 5, 8, true, false, true)
object BlueInfantry extends Tile('i', 1, 2, 4, 6, true, false, true)
object BlueCavalry extends Tile('v', 2, 2, 4, 5, true, false, true)

abstract sealed case class Direction(y: Int, x: Int)

object N extends Direction(-1, 0)
object NE extends Direction(-1, 1)
object E extends Direction(0, 1)
object SE extends Direction(1, 1)
object S extends Direction(1, 0)
object SW extends Direction(1, -1)
object W extends Direction(0, -1)
object NW extends Direction(-1, -1)
