package cats.uri.laws.discipline

import cats.uri._
import cats.uri.laws._
import org.scalacheck.Prop._
import org.scalacheck._
import org.typelevel.discipline._

trait PercentCodecTests[A] extends Laws {
  def laws: PercentCodecLaws[A]

  def percentCodec(implicit A: Arbitrary[A]): RuleSet =
    new DefaultRuleSet(
      name = "percentCodec",
      parent = None,
      "percentCodec round trip" -> forAll((a: A) => laws.percentCodecRoundTrip(a))
    )
}

object PercentCodecTests {
  def apply[A](implicit D: PercentDecoder[A], E: PercentEncoder[A]): PercentCodecTests[A] =
    new PercentCodecTests[A] {
      override def laws: PercentCodecLaws[A] = PercentCodecLaws[A]
    }
}
