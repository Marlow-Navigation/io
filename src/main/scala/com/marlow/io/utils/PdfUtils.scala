package com.marlow.io.utils

import com.itextpdf.html2pdf.{ConverterProperties, HtmlConverter}
import com.itextpdf.kernel.colors.DeviceGray
import com.itextpdf.kernel.events.{Event, PdfDocumentEvent}
import com.itextpdf.kernel.font.{PdfFont, PdfFontFactory}
import com.itextpdf.kernel.pdf.{PdfDocument, PdfPage, PdfReader, PdfWriter}
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.{Border => ItextBorder}
import com.itextpdf.layout.element._
import com.itextpdf.layout.property.{TextAlignment, UnitValue, VerticalAlignment}
import com.marlow.io.config.IOConfig
import com.marlow.io.misc.Loggie
import com.marlow.io.model._
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.reflect.runtime.{universe => ru}
import ru._
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import scala.util.{Failure, Success, Try}

object PdfUtils extends Loggie {
  private val config: IOConfig = ConfigSource.default.loadOrThrow[IOConfig]
  private val DefaultTableHeaderBgColor = new DeviceGray(config.tableHeaderBgColor)
  private val DefaultTableRowBgColor = new DeviceGray(config.tableRowBgColor)

  def addCell(
      tDetails: TableDetails,
      table: Table,
      cellDetails: CellDetails,
      font: PdfFont,
      fontBold: PdfFont,
      fontSize: Int
  ): Unit = {
    val rowspan = cellDetails.rowspan
    val colspan = cellDetails.colspan
    val headerCell = cellDetails.isHeader
    val cell = new Cell(rowspan, colspan)
    if (tDetails.border == 0)
      cell.setBorder(ItextBorder.NO_BORDER)

    cell.setFont(
      if (headerCell) PdfFontFactory.createFont(fontBold.getFontProgram.getFontNames.getFontName)
      else PdfFontFactory.createFont(font.getFontProgram.getFontNames.getFontName)
    )
    cell.setFontSize(
      if (headerCell) fontSize - 1
      else fontSize
    )
    cell.setTextAlignment(cellDetails.alignment)
    if (cellDetails.html) {
      val cp = new ConverterProperties
      cp.setImmediateFlush(true)
      HtmlConverter
        .convertToElements(toHtml(cellDetails.asHtmlDetails(font, fontSize)), cp)
        .forEach((element: IElement) => {
          cell.add(element.asInstanceOf[IBlockElement])
        })
    } else {
      cell.add(new Paragraph(cellDetails.text))
    }
    if (headerCell) {
      table.addHeaderCell(cell)
      cell.setBackgroundColor(DefaultTableHeaderBgColor)
    } else {
      table.addCell(cell)
      if (rowspan == 0 && (cell.getRow % 2) == 1) cell.setBackgroundColor(DefaultTableRowBgColor)
    }
  }

  def generate(
      pdfReport: PdfReport
  ): Array[Byte] = {
    val outputStream = new ByteArrayOutputStream()
    val tempBAOS = new ByteArrayOutputStream()
    val pdfWriter = new PdfWriter(tempBAOS)
    val pdfDoc = new PdfDocument(pdfWriter)
    val doc = new Document(
      pdfDoc,
      pdfReport.pageProperties.pageSizeWithOrientation,
      true
    )

    Try {
      doc.setMargins(
        config.defaultMarginTop,
        config.defaultMarginRight,
        config.defaultMarginBottom,
        config.defaultMarginLeft
      )

      pdfReport.header.foreach(h => pdfDoc.addEventHandler(PdfDocumentEvent.START_PAGE, h))
      pdfReport.footer.foreach(f => pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, f))
      pdfReport.details.foreach { tableDetails =>
        if (tableDetails.columns.isEmpty)
          throw new Exception("The provided table has no columns")
        val colWidths = tableDetails.columns.map(_.width).toArray
        val table: Table = new Table(UnitValue.createPercentArray(colWidths)).useAllAvailableWidth
        table.setKeepTogether(tableDetails.keepTogether)
        table.setKeepWithNext(tableDetails.keepWithNext)
        def cells: Seq[CellDetails] = tableDetails.columns ++ tableDetails.cells
        cells.foreach { cell =>
          addCell(
            tableDetails,
            table,
            cell,
            pdfReport.pageProperties.font,
            pdfReport.pageProperties.fontBold,
            pdfReport.pageProperties.fontSize
          )
        }
        doc.add(table)
      }

      doc.close()
      pdfDoc.close()
      tempBAOS.close()

      val inputStream = new ByteArrayInputStream(tempBAOS.toByteArray)
      val pdfDocFinal =
        new PdfDocument(
          new PdfReader(inputStream),
          new PdfWriter(outputStream)
        )
      val docFinal = new Document(
        pdfDocFinal,
        pdfReport.pageProperties.pageSizeWithOrientation,
        true
      )
      if (pdfReport.pageProperties.pageNumbers) {
        docFinal.setFontSize(pdfReport.pageProperties.pageNumbersFontSize)
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

      Seq(docFinal, pdfDocFinal, doc, pdfDoc, tempBAOS, outputStream).foreach(c => Try(c.close()))
      Try {
        outputStream.flush()
      }
    } match {
      case Failure(exception) =>
        logger.error(exception.getMessage, exception)
        throw exception
      case Success(_) =>
        logger.info("Done")
        outputStream.toByteArray
    }
  }

  def pageHeaderFooter(pageHF: PageHF, event: Event): Unit = {
    val docEvent = event.asInstanceOf[PdfDocumentEvent]
    val pdf: PdfDocument = docEvent.getDocument
    val page: PdfPage = docEvent.getPage
    if (!pageHF.textRepeat && pdf.getPageNumber(page) != 1) return
    val cp = new ConverterProperties
    cp.setImmediateFlush(true)
    val paragraph = new Paragraph
    pageHF.height.map(h => paragraph.setHeight(h))
    paragraph.setFont(
      PdfFontFactory.createFont(pageHF.font.getFontProgram.getFontNames.getFontName)
    )
    paragraph.setFontSize(pageHF.fontSize)
    if (pageHF.html) {
      HtmlConverter
        .convertToElements(toHtml(pageHF.asHtmlDetails), cp)
        .forEach((element: IElement) => {
          paragraph.add(element.asInstanceOf[IBlockElement])
        })
    } else paragraph.add(pageHF.text)
    pageHF.canvas(pdf, page, paragraph)
  }

  def toHtml(htmlDetails: HtmlDetails): String =
    "<div align=" + htmlDetails.alignment.value +
      " style=\"font-family:" + htmlDetails.font.getFontProgram.toString + ";" +
      " font-size:" + htmlDetails.fontSize + "pt;\">" + htmlDetails.text +
      "</div>"

  def extractCells[T: TypeTag: reflect.ClassTag](
      dataset: Seq[T],
      columns: Seq[String] = Seq()
  ): Seq[CellProperties] = {
    dataset.flatMap { ds =>
      val rm = runtimeMirror(getClass.getClassLoader)
      val im = rm.reflect(ds)
      val rawCells: Seq[CellRaw] = typeOf[T].members
        .collect {
          case m: MethodSymbol if m.isCaseAccessor =>
            val name = m.name.toString
            val value = im.reflectMethod(m).apply().toString
            CellRaw(name, value)
        }
        .toSeq
        .reverse
      columns.isEmpty match {
        case true => rawCells.map { fkv => CellProperties(fkv.value) }
        case false =>
          rawCells.filter(kv => columns.contains(kv.name)).map { fkv => CellProperties(fkv.value) }
      }
    }
  }

  def extractColumns[T: TypeTag: reflect.ClassTag](
      selection: Seq[String] = Seq()
  ): Seq[ColumnDetails] = {
    val rawColumns = typeOf[T].members
      .collect {
        case m: MethodSymbol if m.isCaseAccessor =>
          ColumnDetails(m.name.toString)
      }
      .toSeq
      .reverse
    val filteredColumns = selection.isEmpty match {
      case true => rawColumns
      case false =>
        rawColumns.filter(kv => selection.contains(kv.text))
    }
    filteredColumns.map(c =>
      c.copy(text = c.text.split("(?=\\p{Lu})").map(_.toUpperCase).mkString(" "))
    )
  }

}
