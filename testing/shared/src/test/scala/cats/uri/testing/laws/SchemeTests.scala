package cats.uri.testing.laws

import cats.kernel.laws.discipline._
import munit._
import cats.uri.scalacheck.scheme._
import cats.uri._

final class SchemeTests extends DisciplineSuite {
  checkAll("Hash[Scheme]", HashTests[Scheme].hash)
  checkAll("Order[Scheme]", OrderTests[Scheme].order)
}
