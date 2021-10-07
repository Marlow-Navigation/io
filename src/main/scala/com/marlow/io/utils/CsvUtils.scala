package com.marlow.io.utils

import com.marlow.io.model.CsvReport
import kantan.csv.RowEncoder

import scala.reflect.runtime.{universe => ru}
import ru._
import kantan.csv._
import kantan.csv.ops._

import java.nio.charset.StandardCharsets

object CsvUtils {
  def generate[T: TypeTag: reflect.ClassTag](
      ds: List[T],
      headerColumns: Option[Seq[String]] = None,
      separator: Char = ',',
      quote: Char = '"'
  )(implicit encoder: RowEncoder[T]): Array[Byte] = {
    val csvReport = CsvReport(ds, headerColumns)
    val csvString = ds.asCsv(
      CsvConfiguration.rfc
        .withQuote(quote)
        .withCellSeparator(separator)
        .withHeader(csvReport.header.columns: _*)
    )
    csvString.replaceAll("\u0000", "").getBytes(StandardCharsets.UTF_8)
  }
}
