package specs.utils

import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.layout.property.TextAlignment
import com.marlow.io.model._
import com.marlow.io.utils.PdfUtils
import org.specs2.mutable.Specification

import java.nio.file.{Files, Paths}
import java.util.UUID

class PdfUtilsSpec extends Specification with DataGen {
  val destination = s"./src/test/resources/mn-pdf-${UUID.randomUUID().toString}.pdf"
  val header: Header = Header(
    text =
      "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\nUt enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.\nDuis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.\nExcepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
    textRepeat = true,
    font = PdfFontFactory.createFont("Helvetica"),
    fontSize = 7,
    html = false
  )

  val footer: Footer = Footer(
    text =
      "<b>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.<br />Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.<br />Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.<br />Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</b>",
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
        RowDetails(
          "M",
          TextAlignment.LEFT,
          Justify,
          false,
          0,
          0
        ),
        RowDetails("480d1460-3380-4e89-9c95-ac9190f5749f", TextAlignment.LEFT, Justify, false, 0, 0)
      )
      PdfUtils.extractCells(Seq(person), Seq("name")) mustEqual List(
        RowDetails("M", TextAlignment.LEFT, Justify, false, 0, 0)
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
    }
    "generate a pdf report from random data" in {
      val personList: Seq[Person] = random[Person](150).toList
      val pdfReportPerson =
        PdfReport(personList, destination, header.text, footer.text)
          .withFooter(footer.text, true)
          .withHeader(header.text)
      PdfUtils.generate(pdfReportPerson)
      Files.exists(Paths.get(destination)) mustEqual true
    }
  }
}
