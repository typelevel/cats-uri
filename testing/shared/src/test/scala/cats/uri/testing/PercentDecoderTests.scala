package cats.uri.testing

import org.scalacheck._
import org.scalacheck.Prop._
import cats.uri._
import java.nio.charset.StandardCharsets

final class PercentDecoderTests extends PercentDecoderPlatformTests {
  import Generators._

  test("PercentDecoder.decode should decode the empty string") {
    assertEquals(PercentDecoder.decode(""), Right(""))
  }

  property(
    "PercentDecoder.decode should reject strings contain non-hex percent sequences or partial percent sequences.") {
    forAll(genNonHexPercentPrefixString) { (str: String) =>
      Prop(PercentDecoder.decode(str).isLeft)
    }
  }

  property("PercentDecoder.decode should fail on invalid unicode byte sequences") {
    forAllNoShrink(genInvalidPercentEncodedString) { (str: String) =>
      PercentDecoder
        .decode(str)
        .fold(
          _ => Prop.passed,
          value =>
            Prop.falsified :| s"String ${str} should not have decoded, but it decoded into ${value} (bytes ${value.getBytes(StandardCharsets.UTF_8).toList})"
        )
    }
  }

  property(
    "PercentDecoder.decode should decode any String which has at least the '%' character encoded") {
    forAll { (str: String) =>
      PercentDecoder.decode(PercentEncoder.encodeMinimal(str)) ?= Right(str)
    }
  }

  property("PercentDecoder.decode should fail on overlong UTF-8 percent encoded byte strings") {
    forAllNoShrink(genOverlongUTF8Encodings) { (str: String) =>
      PercentDecoder
        .decode(str)
        .fold(
          _ => Prop.passed,
          decoded =>
            Prop.falsified :| s"Overlong UTF-8 byte string ${str} should not encode to ${decoded}. It should fail."
        )
    }
  }

  property(
    "Percent encoded byte sequences which represent code points > 0x10ffff are not valid UTF-8 and should fail decoding.") {
    forAllNoShrink(genLargerThanUTF8Range) { (str: String) =>
      PercentDecoder
        .decode(str)
        .fold(
          _ => Prop.passed,
          decoded =>
            Prop.falsified :| s"Percent encoded sequence ${str} reprsents a code point larger than is valid for a UTF-8 encoding. It should have failed decoding, but it decoded with a value of: ${decoded}"
        )
    }
  }
}
