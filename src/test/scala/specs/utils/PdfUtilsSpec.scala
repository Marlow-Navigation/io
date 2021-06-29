package specs.utils

import com.marlow.io.utils.PdfUtils
import org.specs2.mutable.Specification

import java.io.File
import java.nio.file.{Files, Paths}
import scala.io.Source

class PdfUtilsSpec extends Specification {
  val dest = "./src/test/resources/test3.pdf"
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

}
