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
import cats.uri.scalacheck.NonEmptyStringGen
import cats.uri.scalacheck.password._
import cats.uri.syntax.password._
import munit._
import org.scalacheck.Prop._

final class PasswordTests extends ScalaCheckSuite {
  test("Password should not permit empty user values") {
    assert(Password.fromString("").isLeft)
  }

  test("Password literals should not yield errors") {
    password"password" ?= Password.unsafeFromString("password")
  }

  test("Encoded password literals should not yield errors") {
    passwordEncoded"password%3A" ?= Password.unsafeFromString("password:")
  }

  test("Invalid password literals do not compile") {
    assert(
      compileErrors("password\"\"").startsWith(
        "error: Password values can not be the empty string."
      )
    )
  }

  test("Invalid passwordEncoded literals do not compile") {
    assert(
      compileErrors("passwordEncoded\"\"").startsWith(
        "error: Password values can not be the empty string."
      )
    )
  }

  test("Percent encoded passwords which contain illegal characters from the Uri grammar should not decode"){
    assert(PercentDecoder[Password].parseAndDecode("%3A@").isLeft)
  }

  property("All non-empty string values are valid password values") {
    forAllNoShrink(NonEmptyStringGen.genNonEmptyString) { (str: String) =>
      assert(Password.fromString(str).isRight)
    }
  }

  property("Password values should never show their contents with .toString") {
    forAll((password: Password) => assertEquals(password.toString, "Password(<REDACTED>)"))
  }
}
