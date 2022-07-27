package com.github.igordsm.inkdraw

import org.scalajs.dom
import org.scalajs.dom.SVGPathElement
import org.scalajs.dom.document
import org.scalajs.dom.SVGElement
import org.scalajs.dom.URL
import org.scalajs.dom.HTMLInputElement
import org.scalajs.dom.HTMLElement

class Canvas(id: String) {
  var element = document.getElementById(id).querySelector("svg").asInstanceOf[HTMLElement]

  val brushTool = new BrushTool(this, "brush-tool")
  setUpTool(brushTool)
  val zoomIn = new ZoomTool(this, "zoom-in", 0.8)
  setUpTool(zoomIn)
  val zoomOut = new ZoomTool(this, "zoom-out", 1.2)
  setUpTool(zoomOut)
  val pan = new PanTool(this, "pan")
  setUpTool(pan)
  val eraser = new EraserTool(this, "eraser")
  setUpTool(eraser)

  var currentTool: Tool = brushTool
  activateTool(currentTool)

  move(0, 0)

  def activateTool(tool: Tool) = {
    currentTool.deactivate()
    tool.activate()
    currentTool = tool
  }

  def loadExternalSVG(s: String) = {
    element.outerHTML = s
    element = document.getElementById(id).querySelector("svg").asInstanceOf[HTMLElement]
    move(0, 0)
  }

  def setUpTool(tool: Tool) = {
    val el = tool.element
      .querySelector(".icon")
      .addEventListener(
        "click",
        (e: dom.MouseEvent) => {
          activateTool(tool)
        }
      )
  }

  def getEffectiveViewBox() = {
    val vboxAttr = element.getAttribute("viewBox").split(" ")
    val x = vboxAttr(0).toDouble
    val y = vboxAttr(1).toDouble
    val w = vboxAttr(2).toDouble
    val h = vboxAttr(3).toDouble

    val rect = element.getBoundingClientRect()
    if (rect.height > rect.width) {
      val ratioW = w / rect.width 
      val effectiveH = rect.height * ratioW
      (x, y, w, effectiveH)
    } else {
      val ratioH = h / rect.height
      val effectiveW = rect.width * ratioH
      (x, y, effectiveW, h)
    }
  }
  
  def getOffset(e: dom.MouseEvent) = {
    val targetRect = element.getClientRects();
    val offsetX = e.clientX - targetRect(0).left;
    val offsetY = e.clientY - targetRect(0).top;
    (offsetX, offsetY, targetRect(0).width, targetRect(0).height)
  }

  def offsetToViewBox(offset: (Double, Double, Double, Double)) = {
    val (x, y, w, h) = getEffectiveViewBox()

    val xViewBox = x + (offset(0) / offset(2)) * w
    val yViewBox = y + (offset(1) / offset(3)) * h

    (xViewBox, yViewBox)
  }


  def move(dx: Double, dy: Double) = {
    val (x, y, w, h) = getEffectiveViewBox()
    element.setAttribute("viewBox", f"${x + dx} ${y + dy} $w $h")
  }

  def importFile() = {}

}
