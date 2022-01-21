package cats.uri.testing

import cats.uri._
import org.scalacheck.Prop._
import scala.collection.immutable.BitSet
import munit._
import scala.scalajs.js.URIUtils

abstract private[testing] class PercentEncoderPlatformTests extends ScalaCheckSuite {
  import PercentEncoderPlatformTests._

  property("PercentEncoder.encode should agree with ECMAScript's encodeURIComponent") {
    forAll { (str: String) =>
      PercentEncoder.encode(c => defaultAllowedCharacterSet.contains(c))(str) ?= URIUtils
        .encodeURIComponent(str)
    }
  }
}

private[testing] object PercentEncoderPlatformTests {
  private def defaultAllowedCharacterSet: BitSet =
    (('0' to '9') ++ ('a' to 'z') ++ ('A' to 'Z')).foldLeft(BitSet.empty) {
      case (acc, value) =>
        acc + value.toInt
    }
}
