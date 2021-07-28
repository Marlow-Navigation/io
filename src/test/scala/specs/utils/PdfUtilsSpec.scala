package specs.utils

import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.layout.property.TextAlignment
import com.marlow.io.model._
import com.marlow.io.utils.PdfUtils
import org.specs2.mutable.Specification

import java.io.File
import java.util.UUID

class PdfUtilsSpec extends Specification {
  val destination = s"./src/test/resources/mn-pdf-${UUID.randomUUID().toString}.pdf"
  val header: Header = Header(
    text =
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\n" +
        "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.\n" +
        "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.\n" +
        "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
    textRepeat = true,
    font = PdfFontFactory.createFont("Helvetica"),
    fontSize = 7,
    html = false
  )

  val footer: Footer = Footer(
    text =
      "<b>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.<br />" +
        "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.<br />" +
        "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.<br />" +
        "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</b>",
    textRepeat = true,
    font = PdfFontFactory.createFont("Helvetica"),
    fontSize = 7,
    html = true
  )

  "PdfUtils" should {
    "extractCells from provided object" in {
      val person = Person(
        UUID.fromString("480d1460-3380-4e89-9c95-ac9190f5749f"),
        "M",
        "K",
        "1990-01-12",
        120190
      )
      PdfUtils.extractCells(Seq(person), Seq("id", "name")) mustEqual List(
        CellProperties(
          "M",
          TextAlignment.LEFT,
          Justify,
          false,
          0,
          0
        ),
        CellProperties(
          "480d1460-3380-4e89-9c95-ac9190f5749f",
          TextAlignment.LEFT,
          Justify,
          false,
          0,
          0
        )
      )
      PdfUtils.extractCells(Seq(person), Seq("name")) mustEqual List(
        CellProperties("M", TextAlignment.LEFT, Justify, false, 0, 0)
      )
    }
    "extractColumns from provide type" in {
      PdfUtils.extractColumns[Person]().map(_.text) mustEqual Seq(
        "BALANCE",
        "DOB",
        "SURNAME",
        "NAME",
        "ID"
      )
      PdfUtils.extractColumns[Person](Seq("id", "name")).map(_.text) mustEqual Seq(
        "NAME",
        "ID"
      )
      PdfUtils.extractColumns[Person](Seq("name")) mustEqual List(
        ColumnDetails("NAME", TextAlignment.LEFT, Justify, false, 0, 0, 1.0f)
      )
    }
    "generate a pdf report from data" in {
      val personList: Seq[Person] = DataGen.gen(11)
      val pdfReportPerson =
        PdfReport(personList, destination, header.text, footer.text)
          .withFooter(footer.text, true)
          .withHeader(header.text)
          .withCellsAlignment(TextAlignment.LEFT)
      PdfUtils.generate(pdfReportPerson)
      val file = new File(destination)
      file.exists() mustEqual true
      file.length() mustEqual 16103
    }
  }
}
