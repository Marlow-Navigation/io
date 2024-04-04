package specs.utils

import com.marlow.io.utils.StringUtils._
import org.specs2.mutable.Specification

class StringUtils extends Specification {

  "isImageUrl" in {
    "returns correct results for image url strings" in {
      isImageUrl(None) mustEqual false
      isImageUrl(
        Some("test")
      ) mustEqual false
      isImageUrl(Some("MTIzNDU2fG5hbWV8ZW1haWxAdGVzdC5jb218bGV2ZWwxX3JvbGU=")) mustEqual false
      isImageUrl(Some("data:image/png;base64,MTIzNDU2fG5hbWV8ZW1haWxAdGVzdC5jb218bGV2ZWwxX3JvbGU=")) mustEqual true
      isImageUrl(Some("data:application/pdf;base64,1123123123123123123123123123123123123123123")) mustEqual false
    }
  }

  "isBase64" in {
    "returns correct results for base64 strings" in {
      isBase64(None) mustEqual false
      isBase64(
        Some("test")
      ) mustEqual true
      isBase64(Some("MTIzNDU2fG5hbWV8ZW1haWxAdGVzdC5jb218bGV2ZWwxX3JvbGU=")) mustEqual true
    }
  }
  "fromBase64" in {
    "returns correct results for base64 strings" in {
      fromBase64("MTIzNDU2fG5hbWV8ZW1haWxAdGVzdC5jb218bGV2ZWwxX3JvbGU=") mustEqual "123456|name|email@test.com|level1_role"
    }
  }
  "isEmpty" in {
    "returns correct case for given strings" in {
      isEmpty("ThisFakeString") mustEqual false
      isEmpty(null) mustEqual true
      isEmpty("") mustEqual true
      isEmpty(" ") mustEqual false
    }
  }

}
