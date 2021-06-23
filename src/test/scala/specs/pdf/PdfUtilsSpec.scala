package specs.pdf

import com.marlow.io.pdf.PdfUtils
import org.specs2.mutable.Specification

class PdfUtilsSpec extends Specification {
  "PdfUtils" should {
    "generate report" in {
      PdfUtils.generateReport mustEqual "generateReport"
    }
  }
}
