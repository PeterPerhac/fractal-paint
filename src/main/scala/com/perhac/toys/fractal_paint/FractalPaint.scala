package com.perhac.toys.fractal_paint

import java.awt.Color
import java.awt.Color.getHSBColor
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage

import scala.collection.immutable.LazyList.unfold
import scala.swing._
import scala.swing.event.{Key, KeyPressed}
import scala.util.Random

case class Vector(x: Double, y: Double) {
  def *(factor: Double): Vector = Vector(x * factor, y * factor)
}
case class Point(x: Int, y: Int) {
  def to(p2: Point): Vector = Vector(p2.x - this.x, p2.y - this.y)
  def +(v: Vector): Point = Point(this.x + v.x.toInt, this.y + v.y.toInt)
  def withinBounds(maxX: Int, maxY: Int): Boolean = x >= 0 && x < maxX && y >= 0 && y < maxY
}

object FractalPaint extends SimpleSwingApplication {

  val A: Int = 1024

  class DataPanel(frame: MainFrame) extends Panel {

    val canvas = new BufferedImage(A, A, BufferedImage.TYPE_INT_RGB)

    var polyPointCount: Int = 3
    var addCenterPoint: Boolean = false
    var doClear: Boolean = true
    var distance: Double = 0.50
    var radius: Double = A / 3
    var rotation: Double = 0.00
    val translation: Vector = Vector(A / 2, A / 2)
    var pixelCount: Int = 50000
    var restrictPointChoice: Boolean = false
    var tetherHueToRefresh: Boolean = false
    var hue: Float = 0.3f
    var color: Int = _
    def newColor(): Unit = color = getHSBColor(hue, 1.0f, 1.0f).getRGB

    var polygon: Array[Point] = newPolygon(polyPointCount)

    def newPolygon(nSides: Int): Array[Point] = {
      import math._
      def int(d: Double): Int = d.round.toInt
      val step: Double = 2 * Pi / nSides
      val halfPi = Pi / 2
      val outerPoints = (0 until nSides).toArray.map { idx =>
        Point(int(cos(idx * step - halfPi + rotation) * radius), int(sin(idx * step - halfPi + rotation) * radius)) + translation
      }
      if (addCenterPoint) {
        outerPoints :+ Point(A / 2, A / 2)
      } else outerPoints
    }

    def updateTitle(): Unit =
      frame.title = s"$polyPointCount-sided polygon. " +
        f"distance = $distance%.2f, " +
        f"rotation = $rotation%.2f rad, " +
        f"radius = $radius%.0f, " +
        s"points = $pixelCount ${if (restrictPointChoice) ", restricted choice of points" else ""}"

    def doRefresh(): Unit = {
      if (doClear) {
        val g = canvas.createGraphics()
        g.setBackground(Color.BLACK)
        g.setColor(Color.BLACK)
        g.fill(new Rectangle2D.Float(0f, 0f, A.toFloat, A.toFloat))
        g.dispose()
      }
      if (tetherHueToRefresh) {
        hue = hue + 0.01f
      }
      newColor()
      plotPoints()
      updateTitle()
      this.repaint()
    }

    listenTo(keys)
    focusable = true
    requestFocusInWindow()

    reactions += {
      case KeyPressed(_, Key.Up, _, _) =>
        distance = distance + 0.005d
        doRefresh()
      case KeyPressed(_, Key.Down, _, _) =>
        distance = distance - 0.005d
        doRefresh()
      case KeyPressed(_, Key.Right, _, _) =>
        rotation = rotation + 0.010d
        polygon = newPolygon(polyPointCount)
        doRefresh()
      case KeyPressed(_, Key.Left, _, _) =>
        rotation = rotation - 0.010d
        polygon = newPolygon(polyPointCount)
        doRefresh()
      case KeyPressed(_, Key.I, _, _) =>
        radius = radius + 1
        polygon = newPolygon(polyPointCount)
        doRefresh()
      case KeyPressed(_, Key.O, _, _) =>
        radius = radius - 1
        polygon = newPolygon(polyPointCount)
        doRefresh()
      case KeyPressed(_, Key.S, _, _) =>
        pixelCount = pixelCount + 500
        doRefresh()
      case KeyPressed(_, Key.X, _, _) =>
        if (pixelCount > 500) {
          pixelCount = pixelCount - 500
          doRefresh()
        }
      case KeyPressed(_, Key.Space, _, _) =>
        doClear = !doClear
        if (doClear) doRefresh() else updateTitle()
      case KeyPressed(_, Key.C, _, _) =>
        addCenterPoint = !addCenterPoint
        polygon = newPolygon(polyPointCount)
        doRefresh()
      case KeyPressed(_, Key.R, _, _) =>
        restrictPointChoice = !restrictPointChoice
        doRefresh()
      case KeyPressed(_, Key.T, _, _) =>
        tetherHueToRefresh = !tetherHueToRefresh
        doRefresh()
      case KeyPressed(_, Key.H, _, _) =>
        hue = hue + 0.01f
        doRefresh()
      case KeyPressed(_, Key.A, _, _) =>
        polyPointCount = polyPointCount + 1
        polygon = newPolygon(polyPointCount)
        if (doClear) doRefresh() else updateTitle()
      case KeyPressed(_, Key.Z, _, _) =>
        if (polyPointCount > 3) {
          polyPointCount = polyPointCount - 1
          polygon = newPolygon(polyPointCount)
          if (doClear) doRefresh() else updateTitle()
        }
      case KeyPressed(_, Key.Q, _, _) =>
        frame.closeOperation()
    }

    def randomVertex(prevIdx: Int): Int = {
      def randomVertexIndex(): Int = Random.nextInt(polygon.length)
      var newIdx = randomVertexIndex()
      if (restrictPointChoice) {
        while (newIdx == prevIdx) newIdx = randomVertexIndex()
      }
      newIdx
    }

    def plotPoints(): Unit =
      unfold((polygon(0), 0)) {
        case (p, vIdx) =>
          val idx = randomVertex(vIdx)
          val dot = p + (p.to(polygon(idx)) * distance)
          Some((dot, dot -> idx))
      }.slice(1, pixelCount + 1) //discard annoying first pixel
        .foreach(pixel => if (pixel.withinBounds(A, A)) { canvas.setRGB(pixel.x, pixel.y, color) })

    override def paintComponent(g: Graphics2D): Unit =
      g.drawImage(canvas, null, null)

  }

  override def top: MainFrame = new MainFrame {
    contents = new DataPanel(this) {
      preferredSize = new Dimension(A, A)
      updateTitle()
      newColor()
      plotPoints()
    }
  }
}
