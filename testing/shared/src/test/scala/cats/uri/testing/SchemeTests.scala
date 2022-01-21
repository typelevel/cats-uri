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
