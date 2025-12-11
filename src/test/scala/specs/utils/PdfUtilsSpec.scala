package specs.utils

import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.layout.property.TextAlignment
import com.marlow.io.model._
import com.marlow.io.utils.PdfUtils
import org.specs2.mutable.Specification
import java.nio.charset.Charset
import java.time.ZoneId
import java.util.Locale
import specs.utils.DataGen.base64Image

import java.util.UUID

class PdfUtilsSpec extends Specification {

  def dump(prefix: String = "ENV"): Unit = {
    def p(label: String, value: Any): Unit =
      println(s"[$prefix] $label = $value")

    p("java.version", System.getProperty("java.version"))
    p("java.vendor", System.getProperty("java.vendor"))
    p("os.name", System.getProperty("os.name"))
    p("os.arch", System.getProperty("os.arch"))
    p("user.language", System.getProperty("user.language"))
    p("user.country", System.getProperty("user.country"))
    p("file.encoding", java.nio.charset.Charset.defaultCharset().displayName())
    p("default.locale", Locale.getDefault)
    p("default.charset", Charset.defaultCharset())
    p("default.timezone", ZoneId.systemDefault())

    // Optional but useful
    p("itext.version", com.itextpdf.kernel.Version.getInstance.getVersion)

  }

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
//    "extractCells from provided object" in {
//      val person = Person(
//        UUID.fromString("480d1460-3380-4e89-9c95-ac9190f5749f"),
//        "M",
//        "K",
//        "1990-01-12",
//        120190,
//        base64Image
//      )
//      PdfUtils.extractCells(Seq(person), Seq("id", "name")) mustEqual List(
//        CellProperties(
//          "480d1460-3380-4e89-9c95-ac9190f5749f",
//          TextAlignment.LEFT,
//          Justify,
//          false,
//          Unknown,
//          0,
//          0
//        ),
//        CellProperties(
//          "M",
//          TextAlignment.LEFT,
//          Justify,
//          false,
//          Unknown,
//          0,
//          0
//        )
//      )
//      PdfUtils.extractCells(Seq(person), Seq("name")) mustEqual List(
//        CellProperties("M", TextAlignment.LEFT, Justify, false, Unknown, 0, 0)
//      )
//    }
//    "extractColumns from provide type" in {
//      PdfUtils.extractColumns[Person]().map(_.text) mustEqual Seq(
//        "ID",
//        "NAME",
//        "SURNAME",
//        "DOB",
//        "BALANCE",
//        "PROFILE PIC"
//      )
//      PdfUtils.extractColumns[Person](Seq("id", "name")).map(_.text) mustEqual Seq(
//        "ID",
//        "NAME"
//      )
//      PdfUtils.extractColumns[Person](Seq("name")) mustEqual List(
//        ColumnDetails("NAME", TextAlignment.LEFT, Justify, false, Unknown, 1, 1, 1.0f)
//      )
//    }
//    "override columns only when non empty string" in {
//      val personList: List[Person] = DataGen.gen(11)
//      val pdfReportPerson =
//        PdfReport(
//          Seq(personList),
//          header.text,
//          footer.text,
//          columnOverrides = Some(Seq("Person ID", "Name", "", "", "", ""))
//        )
//
//      pdfReportPerson.details.head.columns mustEqual List(
//        ColumnDetails("Person ID", TextAlignment.LEFT, Justify, false, Unknown, 1, 1, 1.0f),
//        ColumnDetails("Name", TextAlignment.LEFT, Justify, false, Unknown, 1, 1, 1.0f),
//        ColumnDetails("SURNAME", TextAlignment.LEFT, Justify, false, Unknown, 1, 1, 1.0f),
//        ColumnDetails("DOB", TextAlignment.LEFT, Justify, false, Unknown, 1, 1, 1.0f),
//        ColumnDetails("BALANCE", TextAlignment.LEFT, Justify, false, Unknown, 1, 1, 1.0f),
//        ColumnDetails("PROFILE PIC", TextAlignment.LEFT, Justify, false, Unknown, 1, 1, 1.0f)
//      )
//    }
    "generate a pdf report from data" in {
      dump()
      val personList: Seq[Person] = DataGen.gen(11)
      val pdfReportPerson =
        PdfReport(Seq(personList), header.text, footer.text)
          .withFooter(footer.text, true)
          .withHeader(header.text)
          .withCellsAlignment(TextAlignment.LEFT)
      val reportArrayBytes: Array[Byte] = PdfUtils.generate(pdfReportPerson)
      println("BINARY: HEADER" + reportArrayBytes.take(200).map("%02X" format _).mkString)
      println("BINARY: MIDDLE" + reportArrayBytes.slice(2000, 2200).map("%02X" format _).mkString)
      println("BINARY: FINAL" + reportArrayBytes.takeRight(200).map("%02X" format _).mkString)
      reportArrayBytes.length mustEqual 440620
    }
//    "generate a pdf report from data with column overrides" in {
//      val personList: Seq[Person] = DataGen.gen(11)
//      val pdfReportPerson =
//        PdfReport(
//          Seq(personList),
//          header.text,
//          footer.text,
//          Seq(),
//          Some(
//            Seq(
//              "IDD",
//              "NAMEE",
//              "SURNAMEE",
//              "DOBB",
//              "BALANCEE",
//              "PROFILE PIC"
//            )
//          )
//        ).withFooter(footer.text, true)
//          .withHeader(header.text)
//          .withCellsAlignment(TextAlignment.LEFT)
//      val reportArrayBytes: Array[Byte] = PdfUtils.generate(pdfReportPerson)
//      reportArrayBytes.length mustEqual 440629
//    }
//    "generate a pdf report from data with column span" in {
//      val personList: Seq[Person] = DataGen.gen(11)
//      val pdfReportPerson =
//        PdfReport(
//          Seq(personList),
//          header.text,
//          footer.text,
//          Seq(),
//          Some(
//            Seq(
//              "IDD",
//              "NAMEE",
//              "SURNAMEE",
//              "DOBB",
//              "BALANCEE",
//              "PROFILE PIC"
//            )
//          )
//        ).withFooter(footer.text, true)
//          .withHeader(header.text)
//          .withCellsAlignment(TextAlignment.LEFT)
//          .maxSpanForColumn(1)
//
//      val reportArrayBytes: Array[Byte] = PdfUtils.generate(pdfReportPerson)
//      reportArrayBytes.length mustEqual 439150
//    }
//    "generate a pdf report from data with column sum, multiple tables, column overrides" in {
//      val pdfReportPerson =
//        PdfReport(
//          Seq(DataGen.gen(11), DataGen.gen(5)),
//          header.text,
//          footer.text,
//          Seq(),
//          Some(
//            Seq(
//              "IDD",
//              "NAMEE",
//              "SURNAMEE",
//              "DOBB",
//              "BALANCEE",
//              "PROFILE PIC"
//            )
//          )
//        ).withFooter(footer.text, true)
//          .withHeader(header.text)
//          .withCellsAlignment(TextAlignment.LEFT)
//          .sumForColumn(5)
//          .maxSpanForColumn(1)
//
//      val reportArrayBytes: Array[Byte] = PdfUtils.generate(pdfReportPerson)
//      reportArrayBytes.length mustEqual 640524
//    }
  }
}
