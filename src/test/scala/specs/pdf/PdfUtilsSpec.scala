package specs.pdf

import com.marlow.io.pdf.PdfUtils
import org.specs2.mutable.Specification

import java.awt.Desktop
import java.io.File
import java.nio.file.{Files, Paths}
import scala.io.Source

class PdfUtilsSpec extends Specification {
  val tempFile: File = File.createTempFile("itext-pdf-", ".pdf")
  tempFile.deleteOnExit()

  val dest: String = tempFile.getPath
  val source: Source = Source.fromFile("src/test/resources/test.json")
  try {
    val json = source.getLines().mkString
    PdfUtils.jsonToPdf(json, dest)
  } finally {
    try {
      source.close
    } catch {
      case e: Exception =>
    }
  }

  "PdfUtils" should {
    "generate a pdf report from json string" in {
      Files.exists(Paths.get(dest)) mustEqual true
      new File(dest).length() mustEqual 6154
    }
  }

  if (Desktop.isDesktopSupported) {
    Desktop.getDesktop.open(new File(dest))
  }
}
