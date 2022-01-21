package cats.uri.testing

import cats._
import cats.syntax.all._
import cats.uri._
import org.scalacheck._
import org.scalacheck.Prop._
import munit._
import scala.scalajs.js.URIUtils

abstract private[testing] class PercentDecoderPlatformTests extends ScalaCheckSuite {
  import PercentDecoderPlatformTests._

  property("PercentDecoder.decode should agree with decodeURIComponent") {
    forAll { (str: String) =>
      val encoded: String = PercentEncoder.encodeAll(str)
      val decoded: Either[String, String] =
        PercentDecoder.decode(encoded).leftMap(_.getLocalizedMessage)
      val jsDecoded: Either[String, String] = jsDecode(encoded)
      (decoded ?= jsDecoded) && (decoded ?= Right(str))
    }
  }

  property("PercentDecoder.decode should fail when decodeURIComponent fails") {
    forAllNoShrink(genFailingJsDecodeString) { (str: String) =>
      Prop(PercentDecoder.decode(str).isLeft)
    }
  }
}

private[testing] object PercentDecoderPlatformTests {
  def jsDecode(value: String): Either[String, String] =
    ApplicativeError[Either[Throwable, *], Throwable]
      .catchNonFatal(
        URIUtils.decodeURIComponent(value)
      )
      .leftMap(_.getLocalizedMessage)

  val genFailingJsDecodeString: Gen[String] =
    Generators.genInvalidPercentEncodedString.filter(str => jsDecode(str).isLeft)
}
