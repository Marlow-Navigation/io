package com.marlow.io.model

trait Csv

case class CsvHeader(columns: Seq[String], hidden: Boolean)
case class CsvRow[T](value: T)

object CsvReport {
  import scala.reflect.runtime.{universe => ru}
  import ru._

  def apply[T: TypeTag: reflect.ClassTag](
      ds: Seq[T],
      headerColumns: Option[Seq[String]] = None
  ): CsvReport = {
    val csvHeader = headerColumns.exists(x => x.nonEmpty) match {
      case true  => CsvHeader(headerColumns.getOrElse(Seq()), true)
      case false => CsvHeader(Seq(), false)
    }
    val csvRows = ds.map(CsvRow(_))
    new CsvReport(csvRows, csvHeader)
  }
}

case class CsvReport(
    rows: Seq[CsvRow[_]],
    header: CsvHeader
)
