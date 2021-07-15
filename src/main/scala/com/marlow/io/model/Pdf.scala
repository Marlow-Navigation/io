package com.marlow.io.model

import com.itextpdf.kernel.events.{Event, IEventHandler}
import com.itextpdf.kernel.font.{PdfFont, PdfFontFactory}
import com.itextpdf.kernel.geom.{PageSize, Rectangle}
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.pdf.{PdfDocument, PdfPage}
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.TextAlignment
import com.marlow.io.utils.PdfUtils

object Pdf {
  val DefaultFileExtension = ".pdf"
  val DefaultTempFileName = "mn-pdf-"
}

sealed trait Orientation {
  def value: String = this.toString.toLowerCase
}
case object Portrait extends Orientation
case object Landscape extends Orientation

sealed trait HtmlTextAlignment {
  def value: String = this.toString.toLowerCase
}
case object Left extends HtmlTextAlignment
case object Center extends HtmlTextAlignment
case object Right extends HtmlTextAlignment
case object Justify extends HtmlTextAlignment

case class PageProperties(
    pageSize: PageSize,
    orientation: Orientation,
    pageNumbers: Boolean,
    pageNumbersFontSize: Int,
    font: PdfFont,
    fontBold: PdfFont,
    fontSize: Int,
    alignment: TextAlignment
) {
  def pageSizeWithOrientation: PageSize = orientation match {
    case Portrait  => pageSize
    case Landscape => pageSize.rotate()
  }
}

case class HtmlDetails(
    text: String,
    font: PdfFont,
    fontSize: Float,
    alignment: HtmlTextAlignment = Justify
)

sealed trait PageHF extends IEventHandler {
  def text: String
  def textRepeat: Boolean
  def font: PdfFont
  def fontSize: Float
  def html: Boolean
  def alignment: HtmlTextAlignment = Justify
  def height: Option[Int] = None
  def yRotationOffset(pageSize: Rectangle, paragraph: Paragraph): Float
  def canvas(pdf: PdfDocument, page: PdfPage, paragraph: Paragraph): Canvas = {
    val pdfCanvas = new PdfCanvas(page.getLastContentStream, page.getResources, pdf)
    val canvas = new Canvas(pdfCanvas, page.getPageSize)
    canvas.showTextAligned(
      paragraph,
      page.getPageSize.getWidth / 2,
      yRotationOffset(page.getPageSize, paragraph),
      TextAlignment.CENTER
    )
  }
  def asHtmlDetails: HtmlDetails = HtmlDetails(
    text,
    font,
    fontSize,
    alignment
  )
}

object Header {
  def apply(text: String): Header = new Header(
    text = text,
    textRepeat = true,
    font = PdfFontFactory.createFont("Helvetica"),
    fontSize = 7,
    html = false
  )
}

case class Header(
    text: String,
    textRepeat: Boolean,
    font: PdfFont,
    fontSize: Float,
    html: Boolean
) extends PageHF {
  override def height: Option[Int] = Some(100)
  override def yRotationOffset(pageSize: Rectangle, paragraph: Paragraph): Float =
    pageSize.getTop - paragraph.getHeight.getValue - 3
  override def handleEvent(event: Event): Unit = PdfUtils.pageHeaderFooter(this, event)
}

object Footer {
  def apply(text: String): Footer = new Footer(
    text = text,
    textRepeat = true,
    font = PdfFontFactory.createFont("Helvetica"),
    fontSize = 7,
    html = false
  )
}

case class Footer(
    text: String,
    textRepeat: Boolean,
    font: PdfFont,
    fontSize: Float,
    html: Boolean
) extends PageHF {
  def yRotationOffset(pageSize: Rectangle, paragraph: Paragraph): Float = 0
  override def handleEvent(event: Event): Unit = PdfUtils.pageHeaderFooter(this, event)
}

case class TableDetails(
    border: Int,
    keepTogether: Boolean,
    keepWithNext: Boolean,
    columns: Seq[ColumnDetails],
    cells: Seq[RowDetails]
)

sealed trait CellType
case object CellHeader extends CellType
case object CellRow extends CellType

sealed trait CellDetails {
  def text: String
  def alignment: TextAlignment
  def html: Boolean
  def colspan: Int
  def rowspan: Int
  def cellType: CellType
  def isHeader: Boolean = cellType match {
    case CellHeader => true
    case _          => false
  }
  def htmlAlignment: HtmlTextAlignment
  def asHtmlDetails(font: PdfFont, fontSize: Float): HtmlDetails = HtmlDetails(
    text,
    font,
    fontSize,
    htmlAlignment
  )
}

object ColumnDetails {
  def apply(name: String): ColumnDetails = new ColumnDetails(
    text = name,
    html = false,
    width = 1
  )
}

case class ColumnDetails(
    text: String,
    alignment: TextAlignment = TextAlignment.LEFT,
    htmlAlignment: HtmlTextAlignment = Justify,
    html: Boolean,
    colspan: Int = 0,
    rowspan: Int = 0,
    width: Float
) extends CellDetails {
  override def cellType: CellType = CellHeader
}

case object RowDetails {
  def apply(text: String): RowDetails = RowDetails(text = text, html = false)
}

case class CellRaw(name: String, value: String)

case class RowDetails(
    text: String,
    alignment: TextAlignment = TextAlignment.LEFT,
    htmlAlignment: HtmlTextAlignment = Justify,
    html: Boolean,
    colspan: Int = 0,
    rowspan: Int = 0
) extends CellDetails {
  override def cellType: CellType = CellRow
}

object PdfReport {
  import scala.reflect.runtime.{universe => ru}
  import ru._

  def apply[T: TypeTag: reflect.ClassTag](
      ds: Seq[T],
      dest: String,
      headerContent: String,
      footerContent: String
  ): PdfReport = {
    val pageProperties = PageProperties(
      pageSize = PageSize.A4,
      orientation = Portrait,
      pageNumbers = true,
      pageNumbersFontSize = 8,
      font = PdfFontFactory.createFont("Helvetica"),
      fontBold = PdfFontFactory.createFont("Helvetica-Bold"),
      fontSize = 8,
      alignment = TextAlignment.JUSTIFIED
    )
    val columnDetails: Seq[ColumnDetails] = PdfUtils.extractColumns[T]()
    val cellDetails: Seq[RowDetails] = PdfUtils.extractCells(ds)
    val tableDetails: TableDetails = TableDetails(
      border = 0,
      keepTogether = false,
      keepWithNext = false,
      columns = columnDetails,
      cells = cellDetails
    )
    PdfReport(
      pageProperties = pageProperties,
      dest = dest,
      details = Seq(tableDetails),
      header = None,
      footer = None
    )
  }
}

case class PdfReport(
    pageProperties: PageProperties,
    dest: String,
    details: Seq[TableDetails],
    header: Option[Header],
    footer: Option[Footer]
) {
  def withFooter(text: String, html: Boolean = false): PdfReport =
    this.copy(footer = Some(Footer(text).copy(html = html)))

  def withHeader(text: String, html: Boolean = false): PdfReport =
    this.copy(header = Some(Header(text).copy(html = html)))
}
