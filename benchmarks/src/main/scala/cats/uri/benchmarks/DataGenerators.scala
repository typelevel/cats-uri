package cats.uri.benchmarks

import cats.syntax.all._
import org.scalacheck._

object DataGenerators {

  val genString: Gen[String] =
    Arbitrary.arbitrary[String]

  val genPred: Gen[Int => Boolean] =
    Arbitrary.arbitrary[Int => Boolean]

  def generateString(seed: Long): String = {
    genString(Gen.Parameters.default, rng.Seed(seed))
      .getOrElse(throw new AssertionError("Failed to generate string"))
  }

  def generateEncoderPredicate(seed: Long): Int => Boolean = {
    val f: Int => Boolean = genPred(Gen.Parameters.default, rng.Seed(seed))
      .getOrElse(throw new AssertionError("Failed to generate encoder predicate"))

    // We must ensure that any encoder predicate always encodes '%'.
    (codePoint: Int) => {
      (codePoint =!= '%'.toInt) && f(codePoint)
    }
  }
}
