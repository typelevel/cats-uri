package cats.uri.testing

import org.scalacheck.Prop._
import cats.uri._

final class PercentEncoderTests extends PercentEncoderPlatformTests {
  test("% should always be encoded, even if the supplied predicate says it should not be.") {
    assertEquals(PercentEncoder.encode(_ => true)("%"), "%25")
  }

  property("Any String should able to be percent encoded") {
    forAll { (pred: Int => Boolean, str: String) =>
      val encoded: String                        = PercentEncoder.encode(pred)(str)
      val decoded: Either[DecodingError, String] = PercentDecoder.decode(encoded)
      (decoded ?= Right(str)) :| s"PercentEncoded $str, bytes (${str.getBytes.toList}): $encoded"
    }
  }
}
