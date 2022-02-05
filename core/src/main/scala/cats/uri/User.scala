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

import cats._
import cats.syntax.all._
import cats.uri.parsers._
import scala.collection.immutable.BitSet

/**
 * The user component of the userinfo in the authority of a URI.
 *
 * @note
 *   A [[User]] value is not permitted to be empty. If it could be empty then `Option[User]` and
 *   `User("")` would create ambiguity as they could both reasonably be represented by the
 *   userinfo string ":password".
 *
 * @see
 *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1]]
 */
sealed abstract class User extends Product with Serializable {

  /**
   * The value of the user field.
   *
   * @note
   *   This value is ''not'' percent encoded and thus is ''not'' suitable for rendering in the
   *   construction of Uri values. You should use [[#encode]] to render this [[User]] as a
   *   percent encoded `String`.
   */
  def value: String

  /**
   * The percent encoded representation of a [[User]].
   */
  final def encode: String =
    PercentEncoder.encode(User.userAllowedCodepoints.contains)(value)

  /**
   * The [[User]] rendered in the canonical `String` format. This is an alias for [[#encode]].
   */
  final def renderAsString: String =
    encode

  final override def toString: String = s"User(value = ${value})"
}

object User {

  final private[this] case class UserImpl(override val value: String) extends User

  /**
   * The set of Unicode code points which do not require percent encoding for a [[User]] value.
   */
  private val userAllowedCodepoints: BitSet =
    Constants.userChars.foldMap(c => BitSet(c.toInt))

  implicit val userPercentCodec: PercentCodec[User] = {
    val decoder: PercentDecoder[User] =
      value => Rfc3986.userinfoUserStr.parseAll(value).leftMap(e =>
        DecodingError("Failed to parse User.", e.toString)
      ).flatMap(value =>
        PercentDecoder.decode(value)
      ).flatMap(value =>
        fromString(value).leftMap(DecodingError.sanitizedMessage)
      )

    PercentCodec.from(
      decoder,
      _.encode
    )
  }

  implicit val userAppendable: Appendable[User] =
    Appendable.fromRenderableString[User]

  implicit val hashAndOrderForUser: Hash[User] with Order[User] =
    new Hash[User] with Order[User] {
      override def hash(x: User): Int =
        x.hashCode

      override def compare(x: User, y: User): Int =
        x.value.compare(y.value)
    }

  implicit val ordering: Ordering[User] =
    hashAndOrderForUser.toOrdering

  implicit val showForUser: Show[User] =
    Show.fromToString

  /**
   * Attempt to create a [[User]] from a `String`. [[User]] values must be non-empty to
   * disambiguate them from `Option[User]`.
   */
  def fromString(value: String): Either[String, User] =
    if (value.length > 0) {
      Right(UserImpl(value))
    } else {
      Left("User values can not be the empty string.")
    }

  /**
   * Attempt to create a [[User]] from a percent encoded `String`. [[User]] values must be
   * non-empty to disambiguate them from `Option[User]`.
   */
  def parseFromPercentEncodedString(value: String): Either[DecodingError, User] =
    PercentDecoder[User].parseAndDecode(value)

  /**
   * As [[#fromString]], but will throw on invalid `Strings`.
   *
   * @note
   *   In general, it is recommended that you not use this method outside of test code or the
   *   REPL.
   */
  def unsafeFromString(value: String): User =
    fromString(value).fold(
      e => throw new IllegalArgumentException(e),
      identity
    )

  /**
   * As [[#parseFromPercentEncodedString]], but will throw on invalid `Strings`.
   *
   * @note
   *   In general, it is recommended that you not use this method outside of test code or the
   *   REPL.
   */
  def unsafeParseFromPercentEncodedString(value: String): User =
    parseFromPercentEncodedString(value).fold(
      e => throw new IllegalArgumentException(e),
      identity
    )

  def unapply(value: User): Some[String] =
    Some(value.value)
}
