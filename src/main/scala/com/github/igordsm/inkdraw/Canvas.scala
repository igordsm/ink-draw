package com.github.igordsm.inkdraw

import org.scalajs.dom
import org.scalajs.dom.SVGPathElement
import org.scalajs.dom.document
import org.scalajs.dom.SVGElement
import org.scalajs.dom.URL
import org.scalajs.dom.HTMLInputElement

class Canvas(id: String) {
  val element: SVGElement = document.getElementById(id).querySelector("svg").asInstanceOf[SVGElement]
  var current_path: Option[SVGPathElement] = None

  val brushTool = new BrushTool(this, "brush-tool", "black", 1)
  setUpTool(brushTool)
  val zoomIn = new ZoomTool(this, "zoom-in", 0.8)
  setUpTool(zoomIn)
  val zoomOut = new ZoomTool(this, "zoom-out", 1.2)
  setUpTool(zoomOut)
  val pan = new PanTool(this, "pan")
  setUpTool(pan)

  var current_tool: Tool = brushTool
  current_tool.activate()

  def reload(s: String) = {
    
  }

  def setUpTool(tool: Tool) = {
    val el = tool.element
      .querySelector("img.icon")
      .addEventListener(
        "click",
        (e: dom.MouseEvent) => {
          current_tool.deactivate()
          tool.activate()
          current_tool = tool
        }
      )
  }

  def getViewBox() = {
    val vboxAttr = element.getAttribute("viewBox").split(" ")
    val x = vboxAttr(0).toDouble
    val y = vboxAttr(1).toDouble
    val w = vboxAttr(2).toDouble
    val h = vboxAttr(3).toDouble

    (x, y, w, h)
  }

  def getOffset(e: dom.MouseEvent) = {
    val targetRect = element.getClientRects();
    val offsetX = e.clientX - targetRect(0).left;
    val offsetY = e.clientY - targetRect(0).top;
    (offsetX, offsetY, targetRect(0).width, targetRect(0).height)
  }

  def move(dx: Double, dy: Double) = {
    val (x, y, w, h) = getViewBox()
    element.setAttribute("viewBox", f"${x + dx} ${y + dy} $w $h")
  }

  def importFile() = {}

}
