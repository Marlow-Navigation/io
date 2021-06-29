package com.marlow.io.pdf

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.itextpdf.html2pdf.{ConverterProperties, HtmlConverter}
import com.itextpdf.kernel.colors.DeviceGray
import com.itextpdf.kernel.events.{Event, IEventHandler, PdfDocumentEvent}
import com.itextpdf.kernel.font.{PdfFont, PdfFontFactory}
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.pdf.{PdfDocument, PdfReader, PdfWriter}
import com.itextpdf.layout.{Canvas, Document}
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.{Cell, IBlockElement, IElement, Paragraph, Table}
import com.itextpdf.layout.property.{TextAlignment, UnitValue, VerticalAlignment}
import com.marlow.io.misc.Loggie

import java.io.File

object PdfUtils extends Loggie {
  private val DEFAULT_PAGE_SIZE: String = "A4"
  private val DEFAULT_ORIENTATION: String = "portrait"
  private val DEFAULT_PAGENUMBERS: Boolean = true
  private val DEFAULT_PAGENUMBERS_FONTSIZE: Float = 10
  private val DEFAULT_FONT: String = "Helvetica"
  private val DEFAULT_FONT_BOLD: String = "Helvetica-Bold"
  private val DEFAULT_FONTSIZE: Float = 12
  private val DEFAULT_ALIGNMENT: String = "JUSTIFIED"
  private val DEFAULT_MARGIN_TOP: Float = 36
  private val DEFAULT_MARGIN_BOTTOM: Float = 36
  private val DEFAULT_MARGIN_LEFT: Float = 36
  private val DEFAULT_MARGIN_RIGHT: Float = 36
  private val KEY_PAGESIZE: String = "pageSize"
  private val KEY_FONT: String = "font"
  private val KEY_FONT_BOLD: String = "fontBold"
  private val KEY_FONT_SIZE: String = "fontSize"
  private val KEY_ALIGNMENT: String = "alignment"
  private val KEY_TEXT: String = "text"
  private val KEY_HEADER: String = "header"
  private val KEY_FOOTER: String = "footer"
  private val KEY_TABLES: String = "tables"
  private val KEY_TABLE: String = "table"
  private val KEY_HTML: String = "html"
  private val TABLE_HEADER_BG_COLOR = new DeviceGray(0.65f)
  private val TABLE_ROW_BG_COLOR = new DeviceGray(0.90f)
  private val EMPTY_STRING = null
  private val NOT_FOUND = null

  def jsonToPdf(json: String, dest: String): Unit = {
    val tempFile = File.createTempFile("itext-", ".pdf")
    val pdfWriter = new PdfWriter(tempFile)
    val pdfDoc = new PdfDocument(pdfWriter)
    val oMap = new ObjectMapper
    val jNode = oMap.readTree(json)
    val jnPageProperties = jNode.get("pageProperties")
    val pageOrientation =
      jnStringValue(jnPageProperties, "orientation", PdfUtils.DEFAULT_ORIENTATION)
    val doc = new Document(
      pdfDoc,
      getPageSize(
        jnStringValue(jnPageProperties, KEY_PAGESIZE, PdfUtils.DEFAULT_PAGE_SIZE),
        pageOrientation
      ),
      true
    )
    try {
      val font =
        PdfFontFactory.createFont(jnStringValue(jnPageProperties, KEY_FONT, PdfUtils.DEFAULT_FONT))
      val fontBold = PdfFontFactory.createFont(
        jnStringValue(jnPageProperties, KEY_FONT_BOLD, PdfUtils.DEFAULT_FONT_BOLD)
      )
      val fontSize = jnFloatValue(jnPageProperties, KEY_FONT_SIZE, PdfUtils.DEFAULT_FONTSIZE)

      doc.setMargins(
        DEFAULT_MARGIN_TOP,
        DEFAULT_MARGIN_RIGHT,
        DEFAULT_MARGIN_BOTTOM,
        DEFAULT_MARGIN_LEFT
      )

      if (jNode.get(KEY_HEADER) != NOT_FOUND) {
        val jnHeader = jNode.get(KEY_HEADER)
        pdfDoc.addEventHandler(PdfDocumentEvent.START_PAGE, Header(jnHeader))
      }

      if (jNode.get(KEY_FOOTER) != NOT_FOUND) {
        val jnFooter = jNode.get(KEY_FOOTER)
        pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, Footer(jnFooter))
      }

      val jnMain = jNode.get("main")
      if (jnMain.get(KEY_TABLES) != NOT_FOUND) {
        val anTables = jnMain.get(KEY_TABLES).asInstanceOf[ArrayNode]
        val nodeTable = anTables.elements
        while (nodeTable.hasNext) {
          val jnTable = nodeTable.next.get(KEY_TABLE)
          val columnWidths =
            oMap.readValue(jnTable.get("columnWidths").toString, classOf[Array[Float]])
          val t = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth
          t.setKeepTogether(jnBooleanValue(jnTable, "keepTogether"))
          t.setKeepWithNext(jnBooleanValue(jnTable, "keepWithNext"))
          val arrayNode = jnTable.get("cells").asInstanceOf[ArrayNode]
          val node = arrayNode.elements
          while (node.hasNext) {
            addCell(t, jnTable, node.next, font, fontBold, fontSize)
          }
          doc.add(t)
        }
      }
      doc.close()
      pdfDoc.close()

      val pdfDocFinal = new PdfDocument(new PdfReader(tempFile), new PdfWriter(dest))
      try {
        val docFinal = new Document(
          pdfDocFinal,
          getPageSize(
            jnStringValue(jnPageProperties, KEY_PAGESIZE, PdfUtils.DEFAULT_PAGE_SIZE),
            pageOrientation
          ),
          true
        )
        if (jnBooleanValue(jnPageProperties, "pageNumbers", PdfUtils.DEFAULT_PAGENUMBERS)) {
          try {
            docFinal.setFontSize(
              jnFloatValue(
                jnPageProperties,
                "pageNumbersFontSize",
                PdfUtils.DEFAULT_PAGENUMBERS_FONTSIZE
              )
            )
            val numberOfPages = pdfDocFinal.getNumberOfPages
            for (i <- 1 to numberOfPages) {
              docFinal.showTextAligned(
                new Paragraph(String.format("Page %s of %s", i, numberOfPages)),
                pdfDocFinal.getPage(i).getPageSize.getWidth - docFinal.getRightMargin,
                docFinal.getBottomMargin,
                i,
                TextAlignment.RIGHT,
                VerticalAlignment.TOP,
                0
              )
            }
          } finally {
            try {
              docFinal.close()
            } catch {
              case e: Exception => throw e
            }
          }
        }
      } finally {
        try {
          pdfDocFinal.close()
        } catch {
          case e: Exception => throw e
        }
      }
    } catch {
      case e: Exception => throw e
    } finally {
      try {
        doc.close()
      } catch {
        case e: Exception =>
      }
      try {
        pdfDoc.close()
      } catch {
        case e: Exception =>
      }
      try {
        tempFile.delete
      } catch {
        case e: Exception =>
      }
    }
  }

  private def jnIntValue(jn: JsonNode, key: String): Int =
    jnIntValue(jn, key, 0)

  private def jnIntValue(jn: JsonNode, key: String, defaultValue: Int): Int =
    if (jn != NOT_FOUND && jn.get(key) != NOT_FOUND) jn.get(key).intValue else defaultValue

  private def jnFloatValue(jn: JsonNode, key: String): Float =
    jnFloatValue(jn, key, 0)

  private def jnFloatValue(jn: JsonNode, key: String, defaultValue: Float): Float =
    if (jn != NOT_FOUND && jn.get(key) != NOT_FOUND) jn.get(key).floatValue else defaultValue

  private def jnStringValue(jn: JsonNode, key: String): String =
    jnStringValue(jn, key, EMPTY_STRING)

  private def jnStringValue(jn: JsonNode, key: String, defaultValue: String): String =
    if (jn != NOT_FOUND && jn.get(key) != NOT_FOUND) jn.get(key).textValue else defaultValue

  private def jnBooleanValue(jn: JsonNode, key: String): Boolean =
    jnBooleanValue(jn, key, false)

  private def jnBooleanValue(jn: JsonNode, key: String, defaultValue: Boolean): Boolean =
    if (jn != NOT_FOUND && jn.get(key) != NOT_FOUND) jn.get(key).booleanValue else defaultValue

  private def getTextAlignment(align: String) =
    if (align == EMPTY_STRING) TextAlignment.LEFT
    else if (align == "CENTER") TextAlignment.CENTER
    else if (align == "RIGHT") TextAlignment.RIGHT
    else if (align == "JUSTIFIED") TextAlignment.JUSTIFIED
    else if (align == "JUSTIFIED_ALL") TextAlignment.JUSTIFIED_ALL
    else TextAlignment.LEFT

  private def getTextAlignmentHtml(align: String): String =
    if (align == EMPTY_STRING || align == "JUSTIFIED" || align == "JUSTIFIED_ALL") "JUSTIFY"
    else align

  private def getPageSize(pageSize: String, pageOrientation: String) = {
    val landscape = pageOrientation != EMPTY_STRING && !pageOrientation.equals(DEFAULT_ORIENTATION)
    if (pageSize == EMPTY_STRING || pageSize.equals("A4")) {
      if (landscape) PageSize.A4.rotate() else PageSize.A4
    } else if (pageSize.equals("LETTER")) {
      if (landscape) PageSize.LETTER.rotate() else PageSize.LETTER
    } else {
      if (landscape) PageSize.A4.rotate() else PageSize.A4
    }
  }

  private def getHtmlText(jn: JsonNode, font: PdfFont, fontSize: Float): String = {
    "<div align=" + getTextAlignmentHtml(jnStringValue(jn, KEY_ALIGNMENT, "CENTER")) +
      " style=\"font-family:" + font.getFontProgram.toString + ";" +
      " font-size:" + fontSize + "pt;\">" + jnStringValue(jn, KEY_TEXT) +
      "</div>"
  }

  private def addCell(
      t: Table,
      jnTable: JsonNode,
      jn: JsonNode,
      font: PdfFont,
      fontBold: PdfFont,
      fontSize: Float
  ): Unit = {
    val rowspan = jnIntValue(jn, "rowspan")
    val colspan = jnIntValue(jn, "colspan")
    val headerCell = jnBooleanValue(jn, KEY_HEADER)
    val c = new Cell(rowspan, colspan)
    if (jnIntValue(jnTable, "border") == 0) {
      c.setBorder(Border.NO_BORDER)
    }
    c.setFont(if (headerCell) fontBold else font)
    c.setFontSize(fontSize)
    c.setTextAlignment(getTextAlignment(jnStringValue(jn, KEY_ALIGNMENT)))
    if (jnBooleanValue(jn, KEY_HTML)) {
      val cp = new ConverterProperties
      cp.setImmediateFlush(true)
      HtmlConverter
        .convertToElements(getHtmlText(jn, font, fontSize), cp)
        .forEach((element: IElement) => {
          c.add(element.asInstanceOf[IBlockElement])
        })
    } else {
      c.add(new Paragraph(jnStringValue(jn, KEY_TEXT)))
    }
    if (headerCell) {
      t.addHeaderCell(c)
      c.setBackgroundColor(PdfUtils.TABLE_HEADER_BG_COLOR)
    } else {
      t.addCell(c)
      if (rowspan == 0 && (c.getRow % 2) == 1) c.setBackgroundColor(PdfUtils.TABLE_ROW_BG_COLOR)
    }
  }

  private def addHeaderFooter(jn: JsonNode, event: Event, headerFlag: Boolean): Unit = {
    val font = PdfFontFactory.createFont(jn.get(KEY_FONT).textValue)
    val docEvent = event.asInstanceOf[PdfDocumentEvent]
    val pdf = docEvent.getDocument
    val page = docEvent.getPage
    if (!jn.get("textRepeat").booleanValue && pdf.getPageNumber(page) != 1) return
    val pageSize = page.getPageSize
    val pdfCanvas = new PdfCanvas(page.getLastContentStream, page.getResources, pdf)
    val cp = new ConverterProperties
    cp.setImmediateFlush(true)
    val p = new Paragraph
    if (headerFlag) p.setHeight(100)
    p.setFont(font)
    p.setFontSize(jn.get(KEY_FONT_SIZE).floatValue)
    if (jnBooleanValue(jn, KEY_HTML)) {
      HtmlConverter
        .convertToElements(getHtmlText(jn, font, jn.get(KEY_FONT_SIZE).floatValue), cp)
        .forEach((element: IElement) => {
          p.add(element.asInstanceOf[IBlockElement])
        })
    } else p.add(jnStringValue(jn, KEY_TEXT))
    val canvas = new Canvas(pdfCanvas, pageSize)
    canvas.showTextAligned(
      p,
      pageSize.getWidth / 2,
      if (headerFlag) pageSize.getTop - p.getHeight.getValue - 3 else 0,
      TextAlignment.CENTER
    )
  }

  private case class Header(jn: JsonNode) extends IEventHandler {
    override def handleEvent(event: Event): Unit = {
      PdfUtils.addHeaderFooter(jn, event, true)
    }
  }

  private case class Footer(jn: JsonNode) extends IEventHandler {
    override def handleEvent(event: Event): Unit = {
      PdfUtils.addHeaderFooter(jn, event, false)
    }
  }

}
