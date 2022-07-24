package com.github.igordsm.inkdraw

import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.Event


trait Tool {
  val canvas: Canvas
  val element: dom.Element

  def activate() = {}
  def deactivate() = {}

}

class ZoomTool(val canvas: Canvas, val zoomId: String, factor: Double) extends Tool {
  val element = document.getElementById(zoomId)


  val doZoomJs: scala.scalajs.js.Function1[dom.MouseEvent, Unit] = doZoom
  override def activate(): Unit = {
    canvas.element.addEventListener("click", doZoomJs)
  }

  override def deactivate(): Unit = {
    canvas.element.removeEventListener("click", doZoomJs)
  }

  def doZoom(e: dom.MouseEvent) = {
    val (x, y, w, h) = canvas.getViewBox()
    canvas.element.setAttribute("viewBox", f"$x $y ${w * factor } ${h *factor}")
  }
}

class BrushTool(val canvas: Canvas, val id: String)
    extends Tool {
  var current_path: Option[dom.SVGPathElement] = None
  val element = document.getElementById(id)
  var strokeWidth = 1.0

  val brushColorElement = document.getElementById("brush-color").asInstanceOf[dom.HTMLInputElement]
  brushColorElement.addEventListener("input", (e) => {
    color = brushColorElement.value
  })
  var color = brushColorElement.value


  val strokeWidthElement = document.getElementById("stroke-width").asInstanceOf[dom.HTMLInputElement]
  strokeWidthElement.addEventListener("input", (e) => {
    strokeWidth = strokeWidthElement.value.toDouble
  })
  strokeWidth = strokeWidthElement.value.toDouble

  document
    .querySelectorAll(".brush-width")
    .foreach({ brush =>
      brush.addEventListener(
        "click",
        _ =>
          strokeWidth = Integer.parseInt(
            brush.asInstanceOf[dom.HTMLElement].textContent.replace("pt", "")
          )
      )
    })


  val startStrokeJs: scala.scalajs.js.Function1[dom.MouseEvent, Unit] = start_stroke
  val endStrokeJs: scala.scalajs.js.Function1[dom.MouseEvent, Unit] = end_stroke
  val mouseMoveJS: scala.scalajs.js.Function1[dom.MouseEvent, Unit] = mouse_move_canvas
  override def activate() = {
    element.classList.add("active")
    element.classList.remove("inactive")

    canvas.element.addEventListener("pointerdown", startStrokeJs)
    canvas.element.addEventListener("pointerup", endStrokeJs)
    canvas.element.addEventListener("pointermove", mouseMoveJS)
  }

  override def deactivate(): Unit = {
    element.classList.add("inactive")
    element.classList.remove("active")

    canvas.element.removeEventListener("pointerdown", startStrokeJs)
    canvas.element.removeEventListener("pointerup", endStrokeJs)
    canvas.element.removeEventListener("pointermove", mouseMoveJS)
  }

  def offsetToViewBox(c: Canvas, offset: (Double, Double, Double, Double)) = {
    val (x, y, w, h) = c.getViewBox()

    val xViewBox = x + (offset(0) / offset(2)) * w
    val yViewBox = y + (offset(1) / offset(3)) * w

    (xViewBox, yViewBox)
  }

  def start_stroke(e: dom.MouseEvent) = {
    val offset = canvas.getOffset(e)

    if (e.buttons == 1) {
      val cp = document
        .createElementNS("http://www.w3.org/2000/svg", "path")
        .asInstanceOf[dom.SVGPathElement]
      
      val posViewBox = offsetToViewBox(canvas, offset)
      cp.setAttribute("d", s"M ${posViewBox(0)} ${posViewBox(1)} ")
      cp.setAttribute("fill", "transparent")
      cp.setAttribute("stroke", color)
      cp.setAttribute("stroke-width", strokeWidth.toString())
      canvas.element.appendChild(cp)

      current_path = Some(cp)
    }
  }

  def end_stroke(e: dom.MouseEvent) = {
    current_path = None
  }

  def mouse_move_canvas(e: dom.MouseEvent): Unit = {
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

  val dragCanvasJs: scala.scalajs.js.Function1[dom.MouseEvent, Unit] = dragCanvas
  override def activate(): Unit = {
    canvas.element.addEventListener("pointermove", dragCanvasJs)
  }

  override def deactivate(): Unit = {
    canvas.element.removeEventListener("pointermove", dragCanvasJs)
  }

  def dragCanvas(e: dom.MouseEvent) = {
    if (e.buttons == 1) {
      val viewBox = canvas.getViewBox()
      val canvasRect = canvas.element.getBoundingClientRect()
      val proportionalDx = viewBox(2) / canvasRect.width
      val proportionalDy = viewBox(3) / canvasRect.height
      canvas.move(-e.movementX * proportionalDx, -e.movementY * proportionalDy)
    }
  }
}
