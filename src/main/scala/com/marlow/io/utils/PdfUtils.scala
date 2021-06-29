package com.marlow.io.utils

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.itextpdf.html2pdf.{ConverterProperties, HtmlConverter}
import com.itextpdf.kernel.colors.DeviceGray
import com.itextpdf.kernel.events.{Event, PdfDocumentEvent}
import com.itextpdf.kernel.font.{PdfFont, PdfFontFactory}
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.pdf.{PdfDocument, PdfReader, PdfWriter}
import com.itextpdf.layout.borders.{Border => ItextBorder}
import com.itextpdf.layout.element._
import com.itextpdf.layout.property.{TextAlignment, UnitValue, VerticalAlignment}
import com.itextpdf.layout.{Canvas, Document}
import com.marlow.io.config.IOConfig
import com.marlow.io.misc.Loggie
import com.marlow.io.model.Pdf._
import com.marlow.io.model.{Footer, Header}
import com.marlow.io.utils.StringUtils.{EmptyString, isEmpty, trim}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import java.io.File
import scala.util.{Failure, Success, Try}

object PdfUtils extends Loggie {
  private val config: IOConfig = ConfigSource.default.loadOrThrow[IOConfig]
  private val DefaultTableHeaderBgColor = new DeviceGray(config.tableHeaderBgColor)
  private val DefaultTableRowBgColor = new DeviceGray(config.tableRowBgColor)

  def jsonToPdf(json: String, dest: String): Unit = {
    val tempFile = File.createTempFile(DefaultTempFileName, DefaultFileExtension)
    val pdfWriter = new PdfWriter(tempFile)
    val pdfDoc = new PdfDocument(pdfWriter)
    val oMap = new ObjectMapper
    val jNode = oMap.readTree(json)
    val jnPageProperties = jNode.get(PageProperties)
    val pageOrientation =
      jnStringValue(jnPageProperties, Orientation, config.defaultOrientation)
    val doc = new Document(
      pdfDoc,
      getPageSize(
        jnStringValue(jnPageProperties, config.keyPageSize, config.defaultPageSize),
        pageOrientation
      ),
      true
    )

    Try {
      val font =
        PdfFontFactory.createFont(
          jnStringValue(jnPageProperties, config.keyFont, config.defaultFont)
        )
      val fontBold = PdfFontFactory.createFont(
        jnStringValue(jnPageProperties, config.keyFontBold, config.defaultFontBold)
      )
      val fontSize = jnFloatValue(jnPageProperties, config.keyFontSize, config.defaultFontsize)

      doc.setMargins(
        config.defaultMarginTop,
        config.defaultMarginRight,
        config.defaultMarginBottom,
        config.defaultMarginLeft
      )

      if (!jNode.get(config.keyHeader).isNull) {
        val jnHeader = jNode.get(config.keyHeader)
        pdfDoc.addEventHandler(PdfDocumentEvent.START_PAGE, Header(jnHeader))
      }

      if (!jNode.get(config.keyFooter).isNull) {
        val jnFooter = jNode.get(config.keyFooter)
        pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, Footer(jnFooter))
      }

      val jnMain = jNode.get(Main)
      if (!jnMain.get(config.keyTables).isNull) {
        val anTables = jnMain.get(config.keyTables).asInstanceOf[ArrayNode]
        val nodeTable = anTables.elements
        while (nodeTable.hasNext) {
          val jnTable = nodeTable.next.get(config.keyTable)
          val columnWidths =
            oMap.readValue(jnTable.get(ColumnWidths).toString, classOf[Array[Float]])
          val t = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth
          t.setKeepTogether(jnBooleanValue(jnTable, KeepTogether))
          t.setKeepWithNext(jnBooleanValue(jnTable, KeepWithNext))
          val arrayNode = jnTable.get(Cells).asInstanceOf[ArrayNode]
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
      val docFinal = new Document(
        pdfDocFinal,
        getPageSize(
          jnStringValue(jnPageProperties, config.keyPageSize, config.defaultPageSize),
          pageOrientation
        ),
        true
      )
      if (jnBooleanValue(jnPageProperties, PageNumbers, config.defaultPageNumbers)) {
        docFinal.setFontSize(
          jnFloatValue(
            jnPageProperties,
            PageNumbersFontSize,
            config.defaultPageNumbersFontsize
          )
        )

        val numberOfPages = pdfDocFinal.getNumberOfPages
        for (pageNo <- 1 to numberOfPages) {
          docFinal.showTextAligned(
            new Paragraph(s"Page $pageNo of $numberOfPages"),
            pdfDocFinal.getPage(pageNo).getPageSize.getWidth - docFinal.getRightMargin,
            docFinal.getBottomMargin,
            pageNo,
            TextAlignment.RIGHT,
            VerticalAlignment.TOP,
            0
          )
        }
      }
      Seq(docFinal, pdfDocFinal, doc, pdfDoc).foreach(c => Try(c.close()))
      Try(tempFile.delete)
    } match {
      case Failure(exception) => logger.error(exception.getMessage, exception)
      case Success(_)         => logger.info("Done")
    }
  }

  def getTextAlignment(align: String): TextAlignment =
    Try { TextAlignment.valueOf(align) }.getOrElse(TextAlignment.LEFT)

  def getTextAlignmentHtml(align: String): String =
    if (isEmpty(trim(align)) || align == TextAlignment.JUSTIFIED
          .name() || align == TextAlignment.JUSTIFIED_ALL.name())
      Justify
    else align

  def getPageSize(pageSize: String, pageOrientation: String): PageSize = {
    val landscape =
      !isEmpty(trim(pageOrientation)) && !pageOrientation.equals(config.defaultOrientation)
    if (isEmpty(trim(pageSize)) || pageSize.equals(A4)) {
      if (landscape) PageSize.A4.rotate() else PageSize.A4
    } else if (pageSize.equals(Letter)) {
      if (landscape) PageSize.LETTER.rotate() else PageSize.LETTER
    } else {
      if (landscape) PageSize.A4.rotate() else PageSize.A4
    }
  }

  def getHtmlText(jn: JsonNode, font: PdfFont, fontSize: Float): String = {
    "<div align=" + getTextAlignmentHtml(
      jnStringValue(jn, config.keyAlignment, TextAlignment.CENTER.name())
    ) +
      " style=\"font-family:" + font.getFontProgram.toString + ";" +
      " font-size:" + fontSize + "pt;\">" + jnStringValue(jn, config.keyText) +
      "</div>"
  }

  def addCell(
      t: Table,
      jnTable: JsonNode,
      jn: JsonNode,
      font: PdfFont,
      fontBold: PdfFont,
      fontSize: Float
  ): Unit = {
    val rowspan = jnIntValue(jn, Rowspan)
    val colspan = jnIntValue(jn, Colspan)
    val headerCell = jnBooleanValue(jn, config.keyHeader)
    val c = new Cell(rowspan, colspan)
    if (jnIntValue(jnTable, Border) == 0)
      c.setBorder(ItextBorder.NO_BORDER)

    c.setFont(if (headerCell) fontBold else font)
    c.setFontSize(fontSize)
    c.setTextAlignment(getTextAlignment(jnStringValue(jn, config.keyAlignment)))
    if (jnBooleanValue(jn, config.keyHtml)) {
      val cp = new ConverterProperties
      cp.setImmediateFlush(true)
      HtmlConverter
        .convertToElements(getHtmlText(jn, font, fontSize), cp)
        .forEach((element: IElement) => {
          c.add(element.asInstanceOf[IBlockElement])
        })
    } else {
      c.add(new Paragraph(jnStringValue(jn, config.keyText)))
    }
    if (headerCell) {
      t.addHeaderCell(c)
      c.setBackgroundColor(DefaultTableHeaderBgColor)
    } else {
      t.addCell(c)
      if (rowspan == 0 && (c.getRow % 2) == 1) c.setBackgroundColor(DefaultTableRowBgColor)
    }
  }

  def addHeaderFooter(jn: JsonNode, event: Event, headerFlag: Boolean): Unit = {
    val font = PdfFontFactory.createFont(jn.get(config.keyFont).textValue)
    val docEvent = event.asInstanceOf[PdfDocumentEvent]
    val pdf = docEvent.getDocument
    val page = docEvent.getPage
    if (!jn.get(TextRepeat).booleanValue && pdf.getPageNumber(page) != 1) return
    val pageSize = page.getPageSize
    val pdfCanvas = new PdfCanvas(page.getLastContentStream, page.getResources, pdf)
    val cp = new ConverterProperties
    cp.setImmediateFlush(true)
    val p = new Paragraph
    if (headerFlag) p.setHeight(100)
    p.setFont(font)
    p.setFontSize(jn.get(config.keyFontSize).floatValue)
    if (jnBooleanValue(jn, config.keyHtml)) {
      HtmlConverter
        .convertToElements(getHtmlText(jn, font, jn.get(config.keyFontSize).floatValue), cp)
        .forEach((element: IElement) => {
          p.add(element.asInstanceOf[IBlockElement])
        })
    } else p.add(jnStringValue(jn, config.keyText))
    val canvas = new Canvas(pdfCanvas, pageSize)
    canvas.showTextAligned(
      p,
      pageSize.getWidth / 2,
      if (headerFlag) pageSize.getTop - p.getHeight.getValue - 3 else 0,
      TextAlignment.CENTER
    )
  }

  def jnIntValue(jn: JsonNode, key: String, defaultValue: Int = 0): Int =
    jnValueOrDefault(jn, key, defaultValue)

  def jnFloatValue(jn: JsonNode, key: String, defaultValue: Float = 0.0f): Float =
    jnValueOrDefault(jn, key, defaultValue)

  def jnStringValue(jn: JsonNode, key: String, defaultValue: String = EmptyString): String =
    jnValueOrDefault(jn, key, defaultValue)

  def jnBooleanValue(jn: JsonNode, key: String, defaultValue: Boolean = false): Boolean =
    jnValueOrDefault(jn, key, defaultValue)

  def jnValueOrDefault[T](jn: JsonNode, key: String, defaultValue: T): T = {
    Try {
      defaultValue match {
        case _: Int     => jn.get(key).intValue
        case _: Float   => jn.get(key).floatValue
        case _: Boolean => jn.get(key).booleanValue
        case _          => jn.get(key).textValue
      }
    } match {
      case Failure(_)     => defaultValue
      case Success(value) => value.asInstanceOf[T]
    }
  }

}
