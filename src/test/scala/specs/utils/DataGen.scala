package specs.utils

import java.util.UUID
import scala.io.Source

case class Person(
    id: UUID,
    name: String,
    surname: String,
    dob: String,
    balance: BigDecimal,
    profilePic: String
)

object DataGen {
  def base64Image: String = Source.fromResource("base64_sampleimage.txt").getLines.mkString
  private val surname: String =
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et " +
      "dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex " +
      "ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat " +
      "nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit " +
      "anim id est laborum."
  private val name: String =
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et " +
      "dolore magna aliqua."

  def gen(size: Int): List[Person] = {
    List.fill(size)(
      Person(
        UUID.fromString("90d08192-c32e-450b-b3c3-a3ec7d87bf83"),
        name,
        surname,
        "Fri Jul 09 07:51:28 EEST 2021",
        1111111111.111,
        base64Image
      )
    )
  }

}
