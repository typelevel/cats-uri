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

  property("All non-empty string values are valid password values") {
    forAllNoShrink(NonEmptyStringGen.genNonEmptyString) { (str: String) =>
      assert(Password.fromString(str).isRight)
    }
  }

  property("Password values should never show their contents with .toString") {
    forAll((password: Password) => assertEquals(password.toString, "Password(<REDACTED>)"))
  }
}
