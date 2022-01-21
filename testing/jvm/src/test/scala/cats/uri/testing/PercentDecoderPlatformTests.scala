package cats.uri.testing

import cats.syntax.all._
import cats.uri._
import munit._
import org.scalacheck.Prop._
import org.scalacheck._
import java.net.URLDecoder

abstract private[testing] class PercentDecoderPlatformTests extends ScalaCheckSuite {
  import Generators._

  property("PercentDecoder.decode should agree with java.net.URLDecoder.decode") {
    forAll { (str: String) =>
      val encoded: String                        = PercentEncoder.encodeAll(str)
      val decoded: Either[DecodingError, String] = PercentDecoder.decode(encoded)
      val javaDecoded: String                    = URLDecoder.decode(encoded, "UTF-8")
      (decoded ?= Right(javaDecoded)) && (decoded ?= Right(str))
    }
  }

  property(
    "PercentDecoder.decode should agree with java.net.URLDecoder.decode if it decodes an arbitrary (possibly invalid) String") {
    forAll(Arbitrary.arbitrary[String].map(_.replace("+", "%20"))) { (str: String) =>
      val encoded: String     = PercentEncoder.encodeAll(str)
      val javaDecoded: String = URLDecoder.decode(encoded, "UTF-8")
      PercentDecoder
        .decode(encoded)
        .fold(
          _ =>
            javaDecoded.contains(
              '\ufffd') :| "JRE URLDecoder.decode contains at least one replacement character, implying our failure is valid.",
          decoded => decoded ?= javaDecoded
        )
    }
  }

  property(
    "Overlong UTF-8 percent encoded values should decode to the replacement character with java.net.URLDecoder.decode") {
    forAllNoShrink(genOverlongUTF8Encodings) { (str: String) =>
      val javaDecoded: String = URLDecoder.decode(str, "UTF-8")
      if (str.length === 6) {
        javaDecoded ?= "\ufffd\ufffd"
      } else if (str.length === 9) {
        javaDecoded ?= "\ufffd\ufffd\ufffd"
      } else {
        javaDecoded ?= "\ufffd\ufffd\ufffd\ufffd"
      }
    }
  }

  property(
    "Byte sequences which would represent code points > 0x10ffff should decode to replacement characters with java.net.URLDecoder.decode") {
    forAllNoShrink(genLargerThanUTF8Range) { (str: String) =>
      val javaDecoded: String = URLDecoder.decode(str, "UTF-8")
      javaDecoded ?= "\ufffd\ufffd\ufffd\ufffd"
    }
  }
}

private[testing] object PercentDecoderPlatformTests
