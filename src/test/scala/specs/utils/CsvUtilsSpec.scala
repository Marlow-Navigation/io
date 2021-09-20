package specs.utils

import com.marlow.io.utils.{CsvUtils, PdfUtils}
import kantan.csv._
import org.specs2.mutable.Specification

import java.nio.charset.StandardCharsets

case class CsvPerson(id: Int, name: String, age: Int)

class CsvUtilsSpec extends Specification {
  val ps = List(CsvPerson(0, "Nicolas", 38), CsvPerson(1, "Kazuma", 1), CsvPerson(2, "John", 18))
  implicit val CsvPersonEncoder: RowEncoder[CsvPerson] =
    RowEncoder.encoder(0, 2, 1)((p: CsvPerson) => (p.id, p.name, p.age))
  val header: Seq[String] = PdfUtils.extractColumns[CsvPerson]().map(_.text)

  "CsvUtilsSpec" should {
    "extractRows from provided object with headers" in {
      val rows = CsvUtils
        .generate(ps, Some(header))
      rows mustEqual
        "ID,NAME,AGE\r\n0,38,Nicolas\r\n1,1,Kazuma\r\n2,18,John\r\n".getBytes(
          StandardCharsets.UTF_8
        )
    }
    "extractRows from provided object without headers" in {
      val rows = CsvUtils
        .generate(ps)
      rows mustEqual
        "\r\n0,38,Nicolas\r\n1,1,Kazuma\r\n2,18,John\r\n".getBytes(
          StandardCharsets.UTF_8
        )
    }
  }
}
