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

import cats.uri._
import org.scalacheck.Prop._
import scala.collection.immutable.SortedSet
import com.google.common.net.PercentEscaper
import munit._

abstract private[testing] class PercentEncoderPlatformTests extends ScalaCheckSuite {
  import PercentEncoderPlatformTests._

  property("PercentEncoder.encode should agree with Guava's PercentEscaper") {
    forAll { (str: String) =>
      PercentEncoder.encode(c => guavaCharacterSetCodePoints.contains(c))(
        str) ?= minimalGuavaEncoder.escape(str)
    }
  }
}

private[testing] object PercentEncoderPlatformTests {
  private def guavaCharacterSet: SortedSet[Char] =
    (('0' to '9') ++ ('a' to 'z') ++ ('A' to 'Z')).foldLeft(SortedSet.empty[Char]) {
      case (acc, value) =>
        acc + value
    }

  private def guavaCharacterSetCodePoints: SortedSet[Int] =
    guavaCharacterSet.map(_.toInt)

  private def minimalGuavaEncoder: PercentEscaper =
    new PercentEscaper("%", false)
}
