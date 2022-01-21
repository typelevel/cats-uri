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

  property("All non-empty string values are valid user values") {
    forAllNoShrink(NonEmptyStringGen.genNonEmptyString) { (str: String) =>
      assert(User.fromString(str).isRight)
    }
  }
}
