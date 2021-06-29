package com.marlow.io.model

import com.fasterxml.jackson.databind.JsonNode
import com.itextpdf.kernel.events.{Event, IEventHandler}
import com.marlow.io.utils.PdfUtils

object Pdf {
  val Main = "main"
  val PageProperties = "pageProperties"
  val ColumnWidths = "columnWidths"
  val KeepTogether = "keepTogether"
  val KeepWithNext = "keepWithNext"
  val Cells = "cells"
  val PageNumbers = "pageNumbers"
  val PageNumbersFontSize = "pageNumbersFontSize"
  val Orientation = "orientation"
  val Justify = "JUSTIFY"
  val A4 = "A4"
  val Letter = "LETTER"
  val Rowspan = "rowspan"
  val Colspan = "colspan"
  val Border = "border"
  val TextRepeat = "textRepeat"
  val DefaultFileExtension = ".pdf"
  val DefaultTempFileName = "itext-"
}

case class Header(jn: JsonNode) extends IEventHandler {
  override def handleEvent(event: Event): Unit = {
    PdfUtils.addHeaderFooter(jn, event, true)
  }
}

case class Footer(jn: JsonNode) extends IEventHandler {
  override def handleEvent(event: Event): Unit = {
    PdfUtils.addHeaderFooter(jn, event, false)
  }
}
