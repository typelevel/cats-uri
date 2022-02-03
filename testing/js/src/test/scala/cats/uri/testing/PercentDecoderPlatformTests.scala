/*
 * Copyright 2022 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
