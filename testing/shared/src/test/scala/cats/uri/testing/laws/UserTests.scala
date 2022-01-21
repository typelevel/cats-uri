package cats.uri.testing.laws

import cats.kernel.laws.discipline._
import cats.uri._
import cats.uri.laws.discipline._
import cats.uri.scalacheck.user._
import munit._

final class UserTests extends DisciplineSuite {
  checkAll("Hash[User]", HashTests[User].hash)
  checkAll("Order[User]", OrderTests[User].order)
  checkAll("PercentCodec[User]", PercentCodecTests[User].percentCodec)
}
