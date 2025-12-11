package com.marlow.io.model

import com.itextpdf.layout.Document
import com.itextpdf.layout.element.{AreaBreak, Cell, Paragraph, Table}
import com.itextpdf.layout.property.{AreaBreakType, TextAlignment, UnitValue}
import com.marlow.io.utils.PdfUtils.{DefaultTableHeaderBgColor, DefaultTableRowBgColor}

case class RichPdfReport(
    override val pageProperties: PageProperties,
    details: Seq[PdfElement],
    override val header: Option[Header],
    override val footer: Option[Footer]
) extends GenericPdfReport

trait PdfElement {
  def add(doc: Document, pageProperties: PageProperties): Document
}

case class PdfTable(
    headers: Seq[ColumnDetails],
    values: Seq[CellProperties],
    border: BorderStyle,
    keepTogether: Boolean,
    keepWithNext: Boolean
) extends PdfElement {
  override def add(doc: Document, pageProperties: PageProperties): Document = {
    val colWidths = headers.map(_.width).toArray
    val table: Table = new Table(UnitValue.createPercentArray(colWidths)).useAllAvailableWidth
    table.setKeepTogether(keepTogether)
    table.setKeepWithNext(keepWithNext)

    headers.foreach(header =>
      table.addHeaderCell(
        new Cell()
          .add(new Paragraph(header.text).setBold().setBackgroundColor(DefaultTableHeaderBgColor))
      )
    )
    values.zipWithIndex.foreach {
      case (value, index) =>
        val cell = new Cell()
        cell.add(new Paragraph(value.text))
        if ((index / headers.size) % 2 == 1) {
          cell.setBackgroundColor(DefaultTableRowBgColor)
        }
        if (value.isBold) cell.setBold()
        value.backgroundColor.map(color => cell.setBackgroundColor(color))
        cell.setFontSize(pageProperties.fontSize)
        table.addCell(cell)
    }
    doc.add(table)
  }
}

case class PdfParagraph(
    text: String,
    fontSize: Float = 18,
    alignment: TextAlignment = TextAlignment.LEFT
) extends PdfElement {
  override def add(doc: Document, pageProperties: PageProperties): Document = {
    doc.add(
      new Paragraph(text)
        .setFontSize(fontSize)
        .setBold()
        .setMarginBottom(10)
        .setTextAlignment(alignment)
    )
  }
}

object PdfPageBreak extends PdfElement {
  override def add(doc: Document, pageProperties: PageProperties): Document = {
    doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE))
  }
}
