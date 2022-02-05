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
import cats.uri.syntax.user._
import cats.uri.scalacheck.NonEmptyStringGen
import org.scalacheck.Prop._

final class UsersTests extends ScalaCheckSuite {

  test("User should not permit empty user values") {
    assert(User.fromString("").isLeft)
  }

  test("User literals should not yield errors") {
    assertEquals(user"user", User.unsafeFromString("user"))
  }

  test("Encoded user literals should not yield errors") {
    assertEquals(userEncoded"user%3A", User.unsafeFromString("user:"))
  }

  test("Invalid user literals do not compile") {
    assert(
      compileErrors("user\"\"").startsWith(
        "error: User values can not be the empty string."
      )
    )
  }

  test("Invalid userEncoded literals do not compile") {
    assert(
      compileErrors("userEncoded\"\"").startsWith(
        "error: User values can not be the empty string."
      )
    )
  }

  test("Percent encoded users which contain illegal characters from the Uri grammar should not decode"){
    assert(PercentDecoder[User].parseAndDecode("%3A:").isLeft)
  }

  property("All non-empty string values are valid user values") {
    forAllNoShrink(NonEmptyStringGen.genNonEmptyString) { (str: String) =>
      assert(User.fromString(str).isRight)
    }
  }
}
