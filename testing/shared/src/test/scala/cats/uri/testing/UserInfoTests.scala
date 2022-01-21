package cats.uri.testing

import cats.uri._
import cats.uri.scalacheck.userinfo._
import cats.uri.syntax.userinfo._
import munit._
import org.scalacheck.Prop
import org.scalacheck.Prop._

final class UserInfoTests extends ScalaCheckSuite {
  test("UserInfo should not permit empty userinfo values") {
    assert(UserInfo.fromPercentEncodedString("").isLeft)
  }

  test("The userInfo value ':' should have no user or password, just a delimiter.") {
    assertEquals(UserInfo.fromPercentEncodedString(":"), Right(UserInfo.OnlyColonDelimiter))
  }

  test("The userInfo value \"user%3A:\" should have a user and a delimiter and no password") {
    assertEquals(
      UserInfo.fromPercentEncodedString("user%3A:"),
      Right(UserInfo(User.unsafeFromString("user:"), true)))
  }

  test(
    "The userInfo values \"user%3A:\" and \"user%3A\" are not equal, but have the same user.") {
    val a: UserInfo = UserInfo.unsafeFromPercentEncodedString("user%3A:")
    val b: UserInfo = UserInfo.unsafeFromPercentEncodedString("user%3A")

    assertNotEquals(a, b)
    assertEquals(a.user, b.user)
  }

  test("The userInfo value \"::\" has a password of \":\"") {
    assertEquals(
      UserInfo.unsafeFromPercentEncodedString("::").password,
      Some(Password.unsafeFromString(":")))
  }

  test("The userInfo value \"::\" has a password of \":\"") {
    assertEquals(
      UserInfo.unsafeFromPercentEncodedString("::").password,
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
