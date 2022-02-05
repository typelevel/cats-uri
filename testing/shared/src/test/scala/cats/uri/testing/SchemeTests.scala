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
import munit._
import cats.uri.syntax.scheme._
import cats.uri.scalacheck.parsers.Rfc3986ScalacheckInstances
import org.scalacheck._
import org.scalacheck.Prop._

final class SchemeTests extends ScalaCheckSuite {

  test("The empty string is not a valid Scheme") {
    assert(Scheme.fromString("").isLeft)
  }

  test("Scheme literals should not yield errors") {
    scheme"http" ?= Scheme.unsafeFromString("http")
  }

  // TODO: Replace with property test.
  test("Invalid schemes strings should yield Scheme values") {
    assert(Scheme.fromString("0").isLeft)
    assert(Scheme.fromString("+").isLeft)
    assert(Scheme.fromString("$").isLeft)
    assert(Scheme.fromString("\ufffd").isLeft)
  }

  test("Invalid scheme literals do not compile") {
    assert(
      compileErrors("scheme\"+\"").startsWith(
        "error: Invalid URI scheme: +. A URI Scheme must be at least one alpha character, followed by zero or more [A-Za-z0-9+-.] characters."
      )
    )
    assert(
      compileErrors("scheme\"\"").startsWith(
        "error: Invalid URI scheme: . A URI Scheme must be at least one alpha character, followed by zero or more [A-Za-z0-9+-.] characters."
      )
    )
  }

  property("Valid scheme strings are valid Scheme values") {
    forAllNoShrink(Rfc3986ScalacheckInstances.genSchemeString) { (str: String) =>
      Prop(Scheme.fromString(str).isRight)
    }
  }
}
