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

package cats.uri.testing.parsers

import org.scalacheck.Prop._
import munit._
import cats.uri.scalacheck.parsers._
import cats.uri.parsers._

final class Rfc3986Tests extends ScalaCheckSuite {

  property("scheme strings should parse") {
    forAll(Rfc3986ScalacheckInstances.genSchemeString) { (str: String) =>
      Rfc3986.schemeStr.parseAll(str) ?= Right(str)
    }
  }

  property("userinfo strings should parse") {
    forAll(Rfc3986ScalacheckInstances.genUserinfoUserString) { (str: String) =>
      Rfc3986.userinfoUserStr.parseAll(str) ?= Right(str)
    }
  }

  property("percent encoded strings should parse") {
    forAll(Rfc3986ScalacheckInstances.genPercentOctetString) { (str: String) =>
      Rfc3986.percentEncoded.string.parseAll(str) ?= Right(str)
    }
  }

  property("unreserved characters should parse") {
    forAll(Rfc3986ScalacheckInstances.genUnreservedChar) { (char: Char) =>
      Rfc3986.unreservedChar.parseAll(char.toString) ?= Right(char)
    }
  }

  property("unreserved character strings should parse") {
    forAll(Rfc3986ScalacheckInstances.genUnreservedString) { (str: String) =>
      Rfc3986.unreservedChar.rep0.string.parseAll(str) ?= Right(str)
    }
  }

  property("sub-delims characters should parse") {
    forAll(Rfc3986ScalacheckInstances.genSubDelimChar) { (char: Char) =>
      Rfc3986.subDelimsChar.parseAll(char.toString) ?= Right(char)
    }
  }

  property("sub-delims character strings should parse") {
    forAll(Rfc3986ScalacheckInstances.genSubDelimString) { (str: String) =>
      Rfc3986.subDelimsChar.rep0.string.parseAll(str) ?= Right(str)
    }
  }
}
