package com.perhac.toys.fractal_paint

import java.awt.Color
import java.awt.Color.getHSBColor
import java.awt.event.ActionEvent
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage

import javax.swing.Timer

import scala.collection.immutable.LazyList.unfold
import scala.collection.mutable.ListBuffer
import scala.collection.{immutable, mutable}
import scala.swing._
import scala.swing.event.{Key, KeyPressed, KeyReleased}
import scala.util.Random

case class Vector(x: Double, y: Double) {
  def *(factor: Double): Vector = Vector(x * factor, y * factor)
}
case class Point(x: Int, y: Int) {
  def to(p2: Point): Vector = Vector(p2.x - this.x, p2.y - this.y)
  def +(v: Vector): Point = Point(this.x + v.x.toInt, this.y + v.y.toInt)
  def withinBounds(maxX: Int, maxY: Int): Boolean = x >= 0 && x < maxX && y >= 0 && y < maxY
}

case class Configuration(
      polyPointCount: Int,
      hue: Float,
      addCenterPoint: Boolean,
      doClear: Boolean,
      restrictPointChoice: Boolean,
      tetherHueToRefresh: Boolean,
      distance: Double,
      radius: Double,
      rotation: Double,
      pixels: Int
)

object FractalPaint extends SimpleSwingApplication {

  val A: Int = 1024

  val defaultConfiguration: Configuration = Configuration(
    polyPointCount = 3,
    hue = 0.3f,
    addCenterPoint = false,
    doClear = true,
    restrictPointChoice = false,
    tetherHueToRefresh = true,
    distance = 0.5,
    radius = A / 3,
    rotation = 0.00d,
    pixels = 30000
  )

  class DataPanel(frame: MainFrame) extends Panel {

    val canvas = new BufferedImage(A, A, BufferedImage.TYPE_INT_RGB)
    val playbackBuffer: ListBuffer[ReplayableAction] = ListBuffer.empty
    val pressedKeys: collection.mutable.Set[Key.Value] = mutable.HashSet()
    val autoResettableKeys: collection.immutable.Set[Key.Value] =
      immutable.HashSet(Key.Space, Key.C, Key.R, Key.T, Key.N, Key.A, Key.Z, Key.Q)

    def reset(): Unit = {
      polyPointCount = currentConfiguration.polyPointCount
      hue = currentConfiguration.hue
      addCenterPoint = currentConfiguration.addCenterPoint
      doClear = currentConfiguration.doClear
      restrictPointChoice = currentConfiguration.restrictPointChoice
      tetherHueToRefresh = currentConfiguration.tetherHueToRefresh
      distance = currentConfiguration.distance
      radius = currentConfiguration.radius
      rotation = currentConfiguration.rotation
      pixels = currentConfiguration.pixels
      newColor()
      polygon = newPolygon(polyPointCount)
    }

    var currentConfiguration: Configuration = defaultConfiguration
    var playbackActive: Boolean = false
    var polyPointCount, color, pixels: Int = _
    var addCenterPoint, doClear, restrictPointChoice, tetherHueToRefresh: Boolean = _
    var distance, radius, rotation: Double = _
    var hue: Float = _
    val translation: Vector = Vector(A / 2, A / 2)
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
        s"points = $pixels ${if (restrictPointChoice) ", restricted choice of points" else ""}"

    def clearCanvas(): Unit = {
      val g = canvas.createGraphics()
      g.setBackground(Color.BLACK)
      g.setColor(Color.BLACK)
      g.fill(new Rectangle2D.Float(0f, 0f, A.toFloat, A.toFloat))
      g.dispose()
    }

    def doRefresh(): Unit = {
      if (doClear) {
        clearCanvas()
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

    sealed trait Action extends Function0[Unit] { def apply(): Unit }
    object Action {

      val noop: Action = NonReplayableAction(() => ())

      def replayable(
            body: => Unit,
            updatePolygon: Boolean = true,
            refreshCondition: () => Boolean = () => true
      ): Action =
        ReplayableAction(() => {
          val _ = body
          if (updatePolygon) polygon = newPolygon(polyPointCount)
          if (refreshCondition()) doRefresh() else updateTitle()
        })

      def nonReplayable(body: => Unit): Action =
        NonReplayableAction(() => if (!playbackActive) { val _ = body })
    }

    case class ReplayableAction(body: () => Unit) extends Action {
      override def apply(): Unit = body()
    }

    case class NonReplayableAction(body: () => Unit) extends Action {
      override def apply(): Unit = body()
    }

    import Action._
    val action: Key.Value => Action = {
      case Key.Up     => replayable({ distance = distance + 0.005d }, updatePolygon = false)
      case Key.Down   => replayable({ distance = distance - 0.005d }, updatePolygon = false)
      case Key.Right  => replayable { rotation = rotation + 0.010d }
      case Key.Left   => replayable { rotation = rotation - 0.010d }
      case Key.I      => replayable { radius = radius + 1 }
      case Key.O      => replayable { radius = radius - 1 }
      case Key.Equals => replayable({ pixels = pixels + 500 }, updatePolygon = false)
      case Key.Minus  => replayable({ if (pixels > 500) pixels = pixels - 500 }, updatePolygon = false)
      case Key.Space  => replayable({ doClear = !doClear }, updatePolygon = false, refreshCondition = () => doClear)
      case Key.C      => replayable { addCenterPoint = !addCenterPoint }
      case Key.R      => replayable({ restrictPointChoice = !restrictPointChoice }, updatePolygon = false)
      case Key.T      => replayable({ tetherHueToRefresh = !tetherHueToRefresh }, updatePolygon = false)
      case Key.H      => replayable({ hue = hue + 0.01f }, updatePolygon = false)
      case Key.N      => nonReplayable { currentConfiguration = defaultConfiguration; playbackBuffer.clear; reset(); doRefresh() }
      case Key.A      => replayable({ polyPointCount = polyPointCount + 1 }, refreshCondition = () => doClear)
      case Key.Z      => replayable({ if (polyPointCount > 3) polyPointCount = polyPointCount - 1 }, refreshCondition = () => doClear)
      case Key.Q      => nonReplayable(frame.closeOperation())
      case Key.Key0 =>
        nonReplayable {
          playbackBuffer.clear()
          currentConfiguration = Configuration(
            polyPointCount = polyPointCount,
            hue = hue,
            addCenterPoint = addCenterPoint,
            doClear = doClear,
            restrictPointChoice = restrictPointChoice,
            tetherHueToRefresh = tetherHueToRefresh,
            distance = distance,
            radius = radius,
            rotation = rotation,
            pixels = pixels
          )
        }
      case Key.BackSpace =>
        nonReplayable {
          val sequence = playbackBuffer.toVector
          if (sequence.nonEmpty) {
            new Thread {
              override def run(): Unit = {
                playbackActive = true
                reset()
                clearCanvas()
                try {
                  sequence.foreach { a =>
                    a(); Thread.sleep(10)
                  }
                } finally {
                  playbackActive = false
                }
              }
            }.start()
          }
        }
      case vk => nonReplayable(println(s"Unknown command: $vk"))
    }

    reactions += {
      case KeyPressed(_, k, _, _)  => pressedKeys.addOne(k)
      case KeyReleased(_, k, _, _) => pressedKeys.subtractOne(k)
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
      }.slice(1, pixels + 1) //discard annoying first pixel
        .foreach(pixel => if (pixel.withinBounds(A, A)) { canvas.setRGB(pixel.x, pixel.y, color) })

    override def paintComponent(g: Graphics2D): Unit =
      g.drawImage(canvas, null, null)

    new Timer(
      20,
      (_: ActionEvent) => {
        pressedKeys.foreach { k =>
          action(k) match {
            case ra: ReplayableAction =>
              playbackBuffer.addOne(ra)
              ra()
            case na: NonReplayableAction => na()
          }
          if (autoResettableKeys.contains(k)) pressedKeys.subtractOne(k)
        }
      }
    ).start()

  }

  override def top: MainFrame = new MainFrame {
    contents = new DataPanel(this) {
      preferredSize = new Dimension(A, A)
      reset()
      doRefresh()
    }
  }
}
