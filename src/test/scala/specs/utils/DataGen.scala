package specs.utils

import com.danielasfregola.randomdatagenerator.RandomDataGenerator
import org.scalacheck.{Arbitrary, Gen}

import java.util.UUID

case class Person(id: UUID, name: String, surname: String, dob: String, balance: BigDecimal)

trait DataGen extends RandomDataGenerator {
  implicit val arbitraryPerson: Arbitrary[Person] = Arbitrary {
    for {
      id <- Gen.uuid
      name <- Gen.alphaStr
      surname <- Gen.alphaStr
      dob <- Gen.calendar
      balance <- Arbitrary.arbBigDecimal.arbitrary
    } yield {
      Person(
        id,
        name,
        surname,
        dob.getTime.toString,
        balance
      )
    }
  }
}
