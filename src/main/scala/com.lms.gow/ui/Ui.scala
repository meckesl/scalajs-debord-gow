package com.lms.gow.ui

import com.lms.gow.io.Loader
import com.lms.gow.model.repo.CardinalityRepository._
import com.lms.gow.model.repo.PlayerRepository._
import com.lms.gow.model.repo.TileRepository.VoidTile
import com.lms.gow.model.repo.{PlayerRepository, RuleRepository, TileRepository}
import com.lms.gow.model.{Game, GameSquare, Point}
import org.scalajs.dom
import org.scalajs.dom.html.Canvas
import org.scalajs.dom.raw.HTMLImageElement

case class Ui(game: Game, gameCanvas: Canvas, gameOverlay: Canvas, statusCanvas: Canvas) {

  val boardDimensions = new Point(RuleRepository.squareX, RuleRepository.squareY)

  var uiSize = new Point(gameCanvas.width, gameCanvas.height)
  var statusSize = new Point(statusCanvas.width, statusCanvas.height)
  var tileSize = uiSize / boardDimensions

  var squareHover: GameSquare = null
  var squareClicked: GameSquare = null

  object Color {
    def rgb(r: Int, g: Int, b: Int) = s"rgb($r, $g, $b)"
    def rgba(r: Int, g: Int, b: Int, a: Int) = s"rgba($r, $g, $b, $a)"
    val White = rgb(255, 255, 255)
    val Silver = rgb(247, 247, 247)
    val Blue = rgb(0, 0, 255)
    val Red = rgb(255, 0, 0)
    val Gray = rgb(128, 128, 128)
    val Highlight = rgba(0, 255, 0, 64)
    def fromPlayer(pl: Player): String = {
      if (pl.equals(PlayerRepository.Blue))
        Blue
      else if (pl.equals(PlayerRepository.Red))
        Red
      else
        Gray
    }
  }

  def drawGame() {
    val ctx = gameCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    // Background
    0 until RuleRepository.squareCount foreach { index =>
      val tile = Point.fromLinear(index, RuleRepository.squareX)
      if (index % 2 == 0) ctx.fillStyle = Color.Silver else ctx.fillStyle = Color.White
      ctx.fillRect(tile.x * tileSize.x, tile.y * tileSize.y, tileSize.x, tileSize.y)
    }

    // Terrain
    TileRepository.terrains.foreach(t => {
      val image: HTMLImageElement = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
      image.src = Loader.getTileUrl(t)
      image.onload = (e: dom.Event) => {
        game.gameSquares.filter(_.terrain.equals(t)).foreach { sq =>
          val te: Point = sq.coords * tileSize
          ctx.shadowBlur = 12
          ctx.shadowColor = Color.Gray
          ctx.shadowOffsetX = 7
          ctx.shadowOffsetY = 0
          ctx.drawImage(image, te.x, te.y, tileSize.x, tileSize.y)
        }
      }
    })

    // Units
    TileRepository.units.foreach(t => {
      val image: HTMLImageElement = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
      image.src = Loader.getTileUrl(t)
      image.onload = (e: dom.Event) => {
        game.gameSquares.filter(_.unit.equals(t)).foreach { sq =>
          val te: Point = sq.coords * tileSize
          ctx.shadowBlur = 5
          ctx.shadowColor = Color.Gray
          ctx.shadowOffsetX = 5
          ctx.shadowOffsetY = 5
          ctx.drawImage(image, te.x, te.y, tileSize.x, tileSize.y)
          ctx.fillStyle = Color.fromPlayer(sq.unit.player)
          ctx.fillRect(
            te.x + (tileSize.x / 20),
            te.y + (tileSize.y - tileSize.y / 12),
            tileSize.x - (tileSize.x / 20), tileSize.y / 12)
        }
      }
    })

    // Communication
    game.gameSquares.foreach(sq => {
      sq.com.foreach(com => {
        ctx.strokeStyle = Color.fromPlayer(com._1)
        com._2.foreach(c => {
          var a: Point = null
          var b: Point = null

          ctx.beginPath()
          if (Seq(NW, SE, SOURCE).contains(c)) {
            a = sq.coords * tileSize
            b = a + tileSize
          }
          if (Seq(N, S, SOURCE).contains(c)) {
            a = (sq.coords * tileSize) + new Point(tileSize.x / 2, 0)
            b = a + new Point(0, tileSize.y)
          }
          if (Seq(NE, SW, SOURCE).contains(c)) {
            a = (sq.coords * tileSize) + new Point(0, tileSize.y)
            b = a + new Point(tileSize.x, -tileSize.y)
          }
          if (Seq(W, E, SOURCE).contains(c)) {
            a = (sq.coords * tileSize) + new Point(0, tileSize.y / 2)
            b = a + new Point(tileSize.x, 0)
          }
          ctx.moveTo(a.x, a.y)
          ctx.lineTo(b.x, b.y)
          ctx.stroke()
          ctx.closePath()
        })
      })
    })

  }

  def drawStatus() {
    val ctx = statusCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    ctx.fillStyle = Color.Silver
    ctx.strokeStyle = Color.fromPlayer(game.turnPlayer)
    ctx.lineWidth = 10
    ctx.shadowBlur = 10
    ctx.shadowColor = Color.Gray
    ctx.shadowOffsetX = 10
    ctx.shadowOffsetY = 10
    ctx.beginPath
    ctx.rect(0, 0, statusCanvas.width, statusCanvas.height)
    ctx.fill
    ctx.stroke
    ctx.closePath
  }

  def squareHighlight(sq: GameSquare, alpha: Double) = {
    val ctx = gameOverlay.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    ctx.globalAlpha = alpha
    ctx.fillStyle = Color.Highlight
    ctx.strokeStyle = Color.Highlight
    ctx.lineWidth = 2
    val np = (sq.coords * tileSize) + tileSize / 2
    ctx.beginPath
    ctx.arc(np.x, np.y, tileSize.x / 2, 0, 2 * Math.PI, anticlockwise = false)
    ctx.fill
    ctx.stroke
    ctx.closePath
  }

  def squareStatus(sq: GameSquare) = {

    val ctx = statusCanvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    drawStatus()

    val boxSize = tileSize * 3
    val margin = 20
    val p = new Point(margin, margin)
    val terrain: HTMLImageElement = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
    terrain.src = Loader.getTileUrl(sq.terrain)

    terrain.onload = (e: dom.Event) => {
      ctx.drawImage(terrain, p.x, p.y, boxSize.x, boxSize.y)
      if (!sq.unit.equals(VoidTile)) {
        val unit: HTMLImageElement = dom.document.createElement("img").asInstanceOf[HTMLImageElement]
        unit.src = Loader.getTileUrl(sq.unit)
        unit.onload = (e: dom.Event) => {
          ctx.drawImage(unit, p.x, p.y, boxSize.x, boxSize.y)

          if (!sq.unit.player.equals(Neutral)) {
            ctx.fillStyle = Color.fromPlayer(sq.unit.player)
            ctx.fillRect(
              p.x + (boxSize.x / 20),
              p.y + (boxSize.y - boxSize.y / 12),
              boxSize.x - (boxSize.x / 20), boxSize.y / 12)
          }

          val txtp = p + new Point(0, boxSize.y + margin)
          ctx.closePath
          ctx.lineWidth = 1
          ctx.strokeStyle = "rgb(0,0,0)"
          ctx.strokeText(
            s"""
                 unit: ${sq.unit.char}
                 terrain: ${sq.terrain.char}
                 attack: ${sq.unit.attack}
                 defense: ${sq.unit.defense}
                 movement: ${sq.unit.speed}
               """, txtp.x, txtp.y, statusSize.x - margin)
        }
      }
    }
  }

  def onHover(mouse: Point) {

    def getGameSquare(mouse: Point): GameSquare = {
      val np = uiSize - (uiSize - mouse)
      val z = (np - (np % tileSize)) / tileSize
      dom.console.log(z.x + "/" + z.y)
      val index = z.toLinear(RuleRepository.squareX)
      game.gameSquares(index)
    }

    val curSq = getGameSquare(mouse)

    if (curSq != squareHover) {
      val ctx = gameOverlay.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
      ctx.clearRect(0, 0, uiSize.x, uiSize.y)
      squareHighlight(curSq, 0.1)
      if (null != squareClicked)
        squareHighlight(squareClicked, 0.3)
      squareHover = curSq
    }

  }

  def onClick(mouse: Point): Unit = {
    val ctx = gameOverlay.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    ctx.clearRect(0, 0, uiSize.x, uiSize.y)
    squareClicked = squareHover
    squareHighlight(squareHover, 0.3)
    squareStatus(squareHover)
  }

  def onResize(s: Point): Unit = {
    uiSize = s
    tileSize = uiSize / boardDimensions
    gameCanvas.height = uiSize.y.toInt
    gameCanvas.width = uiSize.x.toInt
    gameOverlay.height = gameCanvas.height
    gameOverlay.width = gameCanvas.width
    statusCanvas.height = (uiSize.y / 4).toInt
    statusCanvas.width = gameCanvas.width / 3
    statusSize = new Point(statusCanvas.width, statusCanvas.height)
    drawGame()
    drawStatus()
    if (null != squareClicked) {
      squareHighlight(squareClicked, 0.3)
      squareStatus(squareClicked)
    }
  }

}
