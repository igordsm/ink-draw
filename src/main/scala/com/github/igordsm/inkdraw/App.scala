package com.github.igordsm.inkdraw

import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.HTMLParagraphElement
import org.scalajs.dom.SVGElement
import org.scalajs.dom.SVGPathElement
import org.scalajs.dom.SVGCircleElement
import org.scalajs.dom.HTMLElement
import org.scalajs.dom.HTMLInputElement
import org.scalajs.dom.URL
import scala.languageFeature.postfixOps


object App {
  var cv = new Canvas("canvas")

  document
    .getElementById("save-button")
    .addEventListener(
      "click",
      (_) => {
        val a = document.createElement("a").asInstanceOf[dom.HTMLAnchorElement]
        val blob = new dom.Blob(scala.scalajs.js.Array(cv.element.outerHTML))
        a.href = URL.createObjectURL(blob)
        a.setAttribute("download", "draw.svg")
        a.click()
        URL.revokeObjectURL(a.href)
      }
    )
  
  document.getElementById("open-button").addEventListener("change", (e: dom.MouseEvent) => {
    val fileInput = e.target.asInstanceOf[HTMLInputElement]
    val selectedFile = fileInput.files(0)
    selectedFile.text().`then`((s: String) => {
      // element.outerHTML = s
    })
  })

  def main(args: Array[String]): Unit = {}
}
