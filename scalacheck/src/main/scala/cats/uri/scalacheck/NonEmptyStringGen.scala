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

package cats.uri.scalacheck

import cats.syntax.all._
import org.scalacheck._

private[uri] object NonEmptyStringGen {

  /**
   * A very simple generator for making non-empty string values.
   */
  val genNonEmptyString: Gen[String] =
    Arbitrary
      .arbitrary[String]
      .flatMap(value =>
        if (value.length === 0) {
          Arbitrary.arbitrary[Char].map(_.toString)
        } else {
          Gen.const(value)
        })
}
