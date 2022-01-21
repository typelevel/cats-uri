package cats.uri.testing.laws

import cats.kernel.laws.discipline._
import cats.uri._
import cats.uri.laws.discipline._
import cats.uri.scalacheck.password._
import munit._

final class PasswordTests extends DisciplineSuite {
  checkAll("Hash[Password]", HashTests[Password].hash)
  checkAll("Order[Password]", OrderTests[Password].order)
  checkAll("PercentCodec[Password]", PercentCodecTests[Password].percentCodec)
}
