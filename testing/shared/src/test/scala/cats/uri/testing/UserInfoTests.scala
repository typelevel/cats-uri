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
import cats.uri.scalacheck.userinfo._
import cats.uri.syntax.userinfo._
import munit._
import org.scalacheck.Prop
import org.scalacheck.Prop._

final class UserInfoTests extends ScalaCheckSuite {
  test("UserInfo should not permit empty userinfo values") {
    assert(UserInfo.parseFromPercentEncodedString("").isLeft)
  }

  test("The userInfo value ':' should have no user or password, just a delimiter.") {
    assertEquals(UserInfo.parseFromPercentEncodedString(":"), Right(UserInfo.OnlyColonDelimiter))
  }

  test("The userInfo value \"user%3A:\" should have a user and a delimiter and no password") {
    assertEquals(
      UserInfo.parseFromPercentEncodedString("user%3A:"),
      Right(UserInfo(User.unsafeFromString("user:"), true)))
  }

  test(
    "The userInfo values \"user%3A:\" and \"user%3A\" are not equal, but have the same user.") {
    val a: UserInfo = UserInfo.unsafeParseFromPercentEncodedString("user%3A:")
    val b: UserInfo = UserInfo.unsafeParseFromPercentEncodedString("user%3A")

    assertNotEquals(a, b)
    assertEquals(a.user, b.user)
  }

  test("The userInfo value \"::\" has a password of \":\"") {
    assertEquals(
      UserInfo.unsafeParseFromPercentEncodedString("::").password,
      Some(Password.unsafeFromString(":")))
  }

  test("The userInfo value \"::\" has a password of \":\"") {
    assertEquals(
      UserInfo.unsafeParseFromPercentEncodedString("::").password,
      Some(Password.unsafeFromString(":")))
  }

  test("UserInfo encoded literals should not yield errors") {
    assertEquals(
      userInfoEncoded"%3A::",
      UserInfo(User.unsafeFromString(":"), Password.unsafeFromString(":")))
  }

  test("Invalid userInfoEncoded literals do not compile") {
    assert(
      compileErrors("userInfoEncoded\"\"").startsWith(
        "error: The empty string is not a valid UserInfo value."
      )
    )
  }

  property("All userInfo values with passwords have a delimiter")(
    forAll((userInfo: UserInfo) =>
      Prop(userInfo.password.isEmpty || userInfo.hasColonDelimiter))
  )
}
