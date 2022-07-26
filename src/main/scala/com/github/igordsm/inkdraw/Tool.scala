package com.github.igordsm.inkdraw

import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.Event


trait Tool {
  val canvas: Canvas
  val element: dom.Element
  val cursor: String = "default"

  def activate() = {
    element.classList.add("active")
    element.classList.remove("inactive")

    doActivationLogic()
  }

  def deactivate() = {
    element.classList.add("inactive")
    element.classList.remove("active")

    doDeactivationLogic()
  }

  def doActivationLogic() = {

  }

  def doDeactivationLogic() = {

  }

}

class ZoomTool(val canvas: Canvas, val zoomId: String, factor: Double) extends Tool {
  val element = document.getElementById(zoomId)
  override val cursor: String = if (factor <= 1) "zoom-in" else "zoom-out"


  val doZoomJs: scala.scalajs.js.Function1[dom.MouseEvent, Unit] = doZoom

  override def doActivationLogic(): Unit = {
    canvas.element.addEventListener("click", doZoomJs)
  }

  override def doDeactivationLogic(): Unit = {
    canvas.element.removeEventListener("click", doZoomJs)
  }

  def doZoom(e: dom.MouseEvent) = {
    val (x, y, w, h) = canvas.getEffectiveViewBox()
    canvas.element.setAttribute("viewBox", f"$x $y ${w * factor } ${h *factor}")
  }
}

class BrushTool(val canvas: Canvas, val id: String)
    extends Tool {
  var current_path: Option[dom.SVGPathElement] = None
  val element = document.getElementById(id)
  val icon = element.querySelector(".icon").asInstanceOf[dom.HTMLElement]
  var strokeWidth = 1.0
  private var color = "black"


  def getColor() = color

  def setColor(c: String) = {
    color = c
    icon.style.color = c
  }

  val startStrokeJs: scala.scalajs.js.Function1[dom.MouseEvent, Unit] = startStroke
  val endStrokeJs: scala.scalajs.js.Function1[dom.MouseEvent, Unit] = endStroke
  val mouseMoveJS: scala.scalajs.js.Function1[dom.MouseEvent, Unit] = pointerMove

  override def doActivationLogic() = {
    canvas.element.addEventListener("pointerdown", startStrokeJs)
    canvas.element.addEventListener("pointerup", endStrokeJs)
    canvas.element.addEventListener("pointermove", mouseMoveJS)

    BrushConfiguration.show(this)
  }

  override def doDeactivationLogic(): Unit = {
    canvas.element.removeEventListener("pointerdown", startStrokeJs)
    canvas.element.removeEventListener("pointerup", endStrokeJs)
    canvas.element.removeEventListener("pointermove", mouseMoveJS)

    BrushConfiguration.hide()
  }

  def offsetToViewBox(c: Canvas, offset: (Double, Double, Double, Double)) = {
    val (x, y, w, h) = c.getEffectiveViewBox()

    val xViewBox = x + (offset(0) / offset(2)) * w
    val yViewBox = y + (offset(1) / offset(3)) * h

    (xViewBox, yViewBox)
  }

  def startStroke(e: dom.MouseEvent) = {
    val offset = canvas.getOffset(e)

    if (e.buttons == 1) {
      val cp = document
        .createElementNS("http://www.w3.org/2000/svg", "path")
        .asInstanceOf[dom.SVGPathElement]
      
      val posViewBox = offsetToViewBox(canvas, offset)
      cp.setAttribute("d", s"M ${posViewBox(0)} ${posViewBox(1)} ")
      cp.setAttribute("fill", "none")
      cp.setAttribute("stroke", color)
      cp.setAttribute("stroke-width", strokeWidth.toString())
      canvas.element.appendChild(cp)

      current_path = Some(cp)
    }
  }

  def endStroke(e: dom.MouseEvent) = {
    current_path = None
  }

  def pointerMove(e: dom.MouseEvent): Unit = {
    val offset = canvas.getOffset(e)
    current_path match
      case Some(cp: dom.SVGPathElement) => {
        if (e.buttons == 1) {
          val posViewBox = offsetToViewBox(canvas, offset)
          val current_d = cp.getAttribute("d")
          val next_d = current_d.concat(s"L ${posViewBox(0)} ${posViewBox(1)} ")
          cp.setAttribute("d", next_d)
        }
      }
      case _ => {}
  }

}

class PanTool(val canvas: Canvas, id: String) extends Tool {
  val element = document.getElementById(id)
  override val cursor: String = "grab"

  val dragCanvasJs: scala.scalajs.js.Function1[dom.MouseEvent, Unit] = dragCanvas
  override def doActivationLogic(): Unit = {
    canvas.element.addEventListener("pointermove", dragCanvasJs)
  }

  override def doDeactivationLogic(): Unit = {
    canvas.element.removeEventListener("pointermove", dragCanvasJs)
  }

  def dragCanvas(e: dom.MouseEvent) = {
    val viewBox = canvas.getEffectiveViewBox()
    if (e.buttons == 1) {
      val canvasRect = canvas.element.getBoundingClientRect()
      val proportionalDx = viewBox(2) / canvasRect.width
      val proportionalDy = viewBox(3) / canvasRect.height
      canvas.move(-e.movementX * proportionalDx, -e.movementY * proportionalDy)
    }
  }
}

object BrushConfiguration {
  val element = document.getElementById("brush-options").asInstanceOf[dom.HTMLElement]
  var activeBrush: Option[BrushTool] = None


  val brushColorElement = element.querySelector("input[type=color]").asInstanceOf[dom.HTMLInputElement]
  brushColorElement.addEventListener("input", (e) => {
    activeBrush match {
      case Some(tool) => tool.setColor(brushColorElement.value)
      case None => {}
    }
  })

  val strokeWidthElement = element.querySelector("input[type=range]").asInstanceOf[dom.HTMLInputElement]
  strokeWidthElement.addEventListener("input", (e) => {
    activeBrush match {
      case Some(tool) => tool.strokeWidth = strokeWidthElement.value.toDouble
      case None => {}
    }
  })

  def show(bt: BrushTool) = {
    activeBrush = Some(bt)
    brushColorElement.value = bt.getColor()
    strokeWidthElement.value = bt.strokeWidth.toString()

    val toolBBox = bt.element.getBoundingClientRect()
    element.style.display = "flex"
    element.style.top = toolBBox.bottom.toString() + "px"
    element.style.left = toolBBox.left.toString() + "px"
  }

  def hide() = {
    activeBrush = None
    element.style.display = "none"
  }
}