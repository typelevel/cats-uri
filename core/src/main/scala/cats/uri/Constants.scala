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

package cats.uri

import cats.syntax.all._
import scala.collection.immutable.SortedSet

private[uri] object Constants {

  /**
   * The set of unreserved characters. These characters are never required to be percent
   * encoded.
   *
   * @note
   *   unreserved is ''not'' the compliment of reserved.
   *
   * {{{
   * unreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~"
   * }}}
   *
   * @see
   *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-2.3]]
   */
  val unreservedChars: SortedSet[Char] =
    (('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9'))
      .toVector
      .foldMap(c => SortedSet(c)) ++ SortedSet('-', '.', '_', '~')

  /**
   * The set of percent encoded characters.
   *
   * {{{
   * pct-encoded   = "%" HEXDIG HEXDIG
   * }}}
   */
  val percentEncodedChars: SortedSet[Char] =
    ('0' to '9').toVector.foldMap(c => SortedSet(c)) + '%'

  /**
   * The set of sub-delims characters.
   *
   * {{{
   * sub-delims    = "!" / "$" / "&" / "'" / "(" / ")"
   *                 / "*" / "+" / "," / ";" / "="
   * }}}
   *
   * @see
   *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-2.2]]
   */
  val subDelimsChars: SortedSet[Char] =
    SortedSet('!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=')

  /**
   * The set of characters which are permitted in the user section of the userinfo production.
   *
   * @note
   *   The ABNF shows a ':' character, but the user section is defined to be the sequence of
   *   characters until the first ':' or the end of the userinfo. That is, the user section can
   *   not itself contain an literal ':'.
   *
   * {{{
   * userinfo    = *( unreserved / pct-encoded / sub-delims / ":" )
   * }}}
   *
   * @see
   *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1]]
   */
  val userChars: SortedSet[Char] =
    unreservedChars ++ percentEncodedChars ++ subDelimsChars

  /**
   * The set of characters which are permitted in the deprecated password section of the
   * userinfo production.
   *
   * {{{
   * userinfo    = *( unreserved / pct-encoded / sub-delims / ":" )
   * }}}
   *
   * @see
   *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1]]
   */
  val passwordChars: SortedSet[Char] =
    userChars + ':'
}
