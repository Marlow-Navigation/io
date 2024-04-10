package com.marlow.io.model

import com.itextpdf.kernel.events.{Event, IEventHandler}
import com.itextpdf.kernel.font.{PdfFont, PdfFontFactory}
import com.itextpdf.kernel.geom.{PageSize, Rectangle}
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.pdf.{PdfDocument, PdfPage}
import com.itextpdf.layout.Canvas
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.TextAlignment
import com.marlow.io.utils.{PdfUtils, StringUtils}
import com.itextpdf.layout.borders.{DoubleBorder, SolidBorder, Border => ItextBorder}

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

  val horizontalAlignment: TextAlignment = TextAlignment.CENTER
  def alignment: HtmlTextAlignment = Justify
  def height: Option[Int] = None
  def yRotationOffset(pageSize: Rectangle, paragraph: Paragraph): Float

  def xRotationOffset(pageSize: Rectangle, paragraph: Paragraph): Float =
    horizontalAlignment match {
      case TextAlignment.LEFT   => 0
      case TextAlignment.RIGHT  => pageSize.getWidth
      case TextAlignment.CENTER => pageSize.getWidth / 2
    }

  def canvas(pdf: PdfDocument, page: PdfPage, paragraph: Paragraph): Canvas = {
    val pdfCanvas = new PdfCanvas(page.getLastContentStream, page.getResources, pdf)
    val canvas = new Canvas(pdfCanvas, page.getPageSize)
    canvas.showTextAligned(
      paragraph,
      xRotationOffset(page.getPageSize, paragraph),
      yRotationOffset(page.getPageSize, paragraph),
      TextAlignment.LEFT
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
    html: Boolean,
    override val horizontalAlignment: TextAlignment = TextAlignment.CENTER
) extends PageHF {
  def yRotationOffset(pageSize: Rectangle, paragraph: Paragraph): Float = 0
  override def handleEvent(event: Event): Unit = PdfUtils.pageHeaderFooter(this, event)
}

sealed trait BorderType
case object TotalCell extends BorderType
case object NoBorder extends BorderType

object BorderStyle {
  def apply(borderType: BorderType): BorderStyle = borderType match {
    case TotalCell =>
      new BorderStyle(
        borderTopStyle = new SolidBorder(1),
        borderBottomStyle = new DoubleBorder(1),
        borderLeftStyle = ItextBorder.NO_BORDER,
        borderRightStyle = ItextBorder.NO_BORDER,
        borderType = TotalCell
      )
    case _ => BorderStyle()
  }
}

case class BorderStyle(
    borderTopStyle: ItextBorder = ItextBorder.NO_BORDER,
    borderBottomStyle: ItextBorder = ItextBorder.NO_BORDER,
    borderLeftStyle: ItextBorder = ItextBorder.NO_BORDER,
    borderRightStyle: ItextBorder = ItextBorder.NO_BORDER,
    borderType: BorderType = NoBorder
)

case class TableDetails(
    border: BorderStyle,
    keepTogether: Boolean,
    keepWithNext: Boolean,
    columns: Seq[ColumnDetails],
    cells: Seq[CellProperties],
    maxSpanColumn: Seq[Int] = Seq(),
    sumForColumn: Seq[Int] = Seq()
) {
  def addSumTotalBorder: TableDetails =
    this.copy(border = BorderStyle(TotalCell))
}

sealed trait CellType
case object CellHeader extends CellType
case object CellRow extends CellType

sealed trait CellDetails {
  def text: String
  def alignment: TextAlignment
  def html: Boolean
  def image: Boolean
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
    image = false,
    width = 1
  )
}

case class ColumnDetails(
    text: String,
    alignment: TextAlignment = TextAlignment.LEFT,
    htmlAlignment: HtmlTextAlignment = Justify,
    html: Boolean,
    image: Boolean,
    colspan: Int = 1,
    rowspan: Int = 1,
    width: Float
) extends CellDetails {
  override def cellType: CellType = CellHeader
}

case object CellProperties {
  def apply(text: String): CellProperties = {
    val (isImage, cellContent) = StringUtils.isImageUrl(Some(text)) match {
      case true  => (true, StringUtils.imageUrl(Some(text)))
      case false => (false, text)
    }
    CellProperties(text = cellContent, html = false, image = isImage)
  }
}

case class CellRaw(name: String, value: String)

case class CellProperties(
    text: String,
    alignment: TextAlignment = TextAlignment.LEFT,
    htmlAlignment: HtmlTextAlignment = Justify,
    html: Boolean,
    image: Boolean,
    colspan: Int = 0,
    rowspan: Int = 0
) extends CellDetails {
  override def cellType: CellType = CellRow
  def empty: CellProperties = this.copy(text = "")
  def setText(text: String): CellProperties = this.copy(text = text)
}

object PdfReport {
  import scala.reflect.runtime.{universe => ru}
  import ru._

  def apply[T: TypeTag: reflect.ClassTag](
      data: Seq[Seq[T]],
      headerContent: String,
      footerContent: String,
      sumTotalColumn: Seq[Int] = Seq(),
      columnOverrides: Option[Seq[String]] = None
  ): PdfReport = {
    val pageProperties = PageProperties(
      pageSize = PageSize.A4,
      orientation = Portrait,
      pageNumbers = true,
      pageNumbersFontSize = 7,
      font = PdfFontFactory.createFont("Helvetica"),
      fontBold = PdfFontFactory.createFont("Helvetica-Bold"),
      fontSize = 7,
      alignment = TextAlignment.JUSTIFIED
    )
    val allTableDetails = data.map { ds =>
      val columnDetails: Seq[ColumnDetails] = PdfUtils.extractColumns[T]()
      val maybeColumnWithOverrides = columnOverrides.fold(columnDetails) { details =>
        details.size == columnDetails.size match {
          case true =>
            columnDetails.zipWithIndex.map { columnWithIndex =>
              val (column, index) = columnWithIndex
              if (details(index).isEmpty) column
              else column.copy(text = details(index))
            }
          case false => columnDetails
        }
      }

      val cellDetails: Seq[CellProperties] = PdfUtils.extractCells(ds)
      TableDetails(
        border = BorderStyle(),
        keepTogether = false,
        keepWithNext = false,
        columns = maybeColumnWithOverrides,
        cells = cellDetails,
        sumForColumn = sumTotalColumn
      )
    }

    new PdfReport(
      pageProperties = pageProperties,
      details = allTableDetails,
      header = Some(Header(headerContent)),
      footer = Some(Footer(footerContent))
    )
  }
}

case class PdfReport(
    pageProperties: PageProperties,
    details: Seq[TableDetails],
    header: Option[Header],
    footer: Option[Footer]
) {
  def isMultiTableReport: Boolean = details.size > 1

  def withFooter(text: String, html: Boolean = false): PdfReport =
    this.copy(footer = Some(Footer(text).copy(html = html)))

  def withHeader(text: String, html: Boolean = false): PdfReport =
    this.copy(header = Some(Header(text).copy(html = html)))

  def withCellsAlignment(
      alignment: TextAlignment = TextAlignment.LEFT,
      htmlAlignment: HtmlTextAlignment = Justify
  ): PdfReport =
    this.copy(
      details = this.details.map(td =>
        td.copy(
          columns = td.columns
            .map(cd =>
              cd.copy(alignment = alignment)
                .copy(htmlAlignment = htmlAlignment)
            ),
          cells = td.cells
            .map(cp =>
              cp.copy(alignment = alignment)
                .copy(htmlAlignment = htmlAlignment)
            )
        )
      )
    )

  def portrait: PdfReport =
    this.copy(pageProperties = this.pageProperties.copy(orientation = Portrait))

  def landScape: PdfReport =
    this.copy(pageProperties = this.pageProperties.copy(orientation = Landscape))

  def setFontSize(size: Int): PdfReport =
    this.copy(pageProperties = this.pageProperties.copy(fontSize = size))

  def maxSpanForColumn(index: Int): PdfReport =
    this.copy(details = details.map { tDetails =>
      tDetails.copy(maxSpanColumn = tDetails.maxSpanColumn :+ index)
    })

  def sumForColumn(index: Int): PdfReport =
    this
      .copy(details = details.map { tDetails =>
        tDetails.copy(sumForColumn = tDetails.sumForColumn :+ index)
      })
}

case class ColumnSum(column: Int, sum: BigDecimal) {
  def matchIndex(index: Int): Boolean = index == this.column - 1
}
