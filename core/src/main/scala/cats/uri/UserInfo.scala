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

/**
 * The userinfo from the authority of a Uri.
 *
 * @note
 *   In order to avoid ambiguous representations, this userinfo model can not model the empty
 *   `String`. This makes `UserInfo` distinct from `Option[UserInfo]`.
 *
 * @note
 *   In the deprecated format `user:password` the first non-percent encoded ':' character is a
 *   delimiter between the username and the password. In case where the password is "" this will
 *   yield a userinfo value with a user, no password, ''with a colon delimiter''. This is a
 *   unique value from just having a username.
 *
 * {{{
 * scala> import cats.uri._, cats.uri.syntax.userinfo._
 *
 * scala> userInfoEncoded"myUserName:"
 * val res0: cats.uri.UserInfo = UserInfo(user = Some(User(value = myUserName)), password = None, hasColonDelimiter = true)
 *
 * scala> userInfoEncoded"myUserName"
 * val res1: cats.uri.UserInfo = UserInfo(user = Some(User(value = myUserName)), password = None, hasColonDelimiter = false)
 *
 * scala> res0 == res1
 * val res2: Boolean = false
 *
 * scala> res0.render
 * val res3: String = myUserName:
 *
 * scala> res1.render
 * val res4: String = myUserName
 * }}}
 *
 * @note
 *   If the [[Password]] is present, then `hasColonDelimiter` ''must'' be true.
 *
 * @see
 *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1]]
 */
sealed abstract class UserInfo extends Product with Serializable {

  /**
   * The [[User]] defined in this [[UserInfo]].
   */
  def user: Option[User]

  /**
   * The [[Password]] defined in this [[UserInfo]].
   *
   * @note
   *   WARNING! This field is deprecated in RFC-3986. It is only modeled for interoperability
   *   purposes. If your application is generating URI values you should ''not'' use this.
   */
  def password: Option[Password]

  /**
   * From RFC-3986.
   *
   * {{{
   * the presence or absence of delimiters within a userinfo subcomponent is
   * usually significant to its interpretation.
   * }}}
   *
   * Thus the userinfo string "foo:" is not the same as "foo", which is why this is modeled
   * here.
   *
   * This ''must'' be true if user and password are both empty or if user is empty and password
   * is non-empty. It ''may'' be true if user is non-empty, but it may also be false.
   *
   * @see
   *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-6.2.3]]
   */
  def hasColonDelimiter: Boolean

  /**
   * Percent encode the value.
   */
  final def encode: String =
    UserInfo.userInfoPercentCodec.encode(this)

  /**
   * Render the value in the canonical `String` form. For types like [[UserInfo]] which also
   * support percent encoding, this is just an alias for [[#encode]].
   */
  final def renderAsString: String =
    encode

  final override def toString: String =
    s"UserInfo(user = ${user}, password = ${password}, hasColonDelimiter = ${hasColonDelimiter})"
}

object UserInfo {
  final private[this] case class UserInfoImpl(
      override val user: Option[User],
      override val password: Option[Password],
      override val hasColonDelimiter: Boolean)
      extends UserInfo

  implicit val hashAndOrderForUserInfo: Hash[UserInfo] with Order[UserInfo] =
    new Hash[UserInfo] with Order[UserInfo] {
      override def hash(x: UserInfo): Int =
        x.hashCode

      override def compare(x: UserInfo, y: UserInfo): Int =
        x.user.compare(y.user) match {
          case 0 =>
            x.password.compare(y.password) match {
              case 0 =>
                x.hasColonDelimiter.compare(y.hasColonDelimiter)
              case otherwise =>
                otherwise
            }
          case otherwise =>
            otherwise
        }
    }

  implicit val ordering: Ordering[UserInfo] =
    hashAndOrderForUserInfo.toOrdering

  implicit val showForUserInfo: Show[UserInfo] =
    Show.fromToString

  implicit val userInfoAppendableInstance: Appendable[UserInfo] =
    (a: UserInfo, appender: Appendable.Appender) => {
      a.user.foreach(user => Appendable[User].append(user, appender))
      if (a.hasColonDelimiter) {
        appender.appendChar(':')
      }
      a.password.foreach(password => Appendable[Password].append(password, appender))

      appender
    }

  implicit val userInfoPercentCodec: PercentCodec[UserInfo] = {
    val decoder: PercentDecoder[UserInfo] =
      (value: String) =>
        Rfc3986
          .userinfo
          .parseAll(value)
          .leftMap(_ =>
            DecodingError(
              "Invalid percent encoded UserInfo String.",
              s"Invalid value: ${value}"))
          .flatMap {
            case (user, colon, password)
                if user.isDefined || colon.isDefined || password.isDefined =>

              // We avoid going through the PercentDecoder[User] here as an
              // optimization to avoid reparsing the results.
              for {
                decodedUserString <- user.traverse(PercentDecoder.decode)
                decodedPasswordString <- password.traverse(PercentDecoder.decode)
                u <- decodedUserString.traverse(value => User.fromString(value).leftMap(DecodingError.sanitizedMessage))
                p <- decodedPasswordString.traverse(value => Password.fromString(value).leftMap(DecodingError.sanitizedMessage))
              } yield UserInfoImpl(u, p, colon.isDefined)
            case _ =>
              Left(DecodingError(0, "The empty string is not a valid UserInfo value."))
          }

    val encoder: PercentEncoder[UserInfo] =
      new PercentEncoder[UserInfo] {
        override def encode(a: UserInfo): String = {
          val allocationForColon: Int = if (a.hasColonDelimiter) { 1 }
          else { 0 }
          val sizeHint: Int = (a.user.fold(0)(_.value.size) + a
            .password
            .fold(0)(_.unsafeValue.size) + allocationForColon) * 2
          val appender: Appendable.Appender = Appendable.Appender.instance(sizeHint)

          Appendable[UserInfo].append(a, appender).renderAsString
        }
      }

    PercentCodec.from(
      decoder,
      encoder
    )
  }

  /**
   * [[UserInfo]] with no username or password, but with a ':' character, e.g. ":".
   */
  val OnlyColonDelimiter: UserInfo =
    UserInfoImpl(None, None, true)

  /**
   * Create a [[UserInfo]] from a [[User]] with an optional colon delimiter.
   *
   * {{{
   * scala> import cats.uri._, cats.uri.syntax.all._
   *
   * scala> UserInfo(user"user", true).encode
   * val res0: String = user:
   *
   * scala> UserInfo(user"user", false).encode
   * val res1: String = user
   * }}}
   */
  def apply(user: User, hasColonDelimiter: Boolean): UserInfo =
    UserInfoImpl(Some(user), None, hasColonDelimiter)

  /**
   * Create a [[UserInfo]] from a [[User]].
   *
   * [[UserInfo]] values created via this method ''will not'' have a colon delimiter.
   */
  def apply(user: User): UserInfo =
    apply(user, false)

  /**
   * Create [[UserInfo]] from only [[Password]].
   *
   * @note
   *   WARNING! Use of the password field is deprecated in RFC-3986. It is only modeled for
   *   interoperability purposes. If your application is generating URI values you should
   *   ''not'' use this.
   */
  def apply(password: Password): UserInfo =
    UserInfoImpl(None, Some(password), true)

  /**
   * Create [[UserInfo]] from a [[User]] and [[Password]].
   *
   * @note
   *   WARNING! Use of the password field is deprecated in RFC-3986. It is only modeled for
   *   interoperability purposes. If your application is generating URI values you should
   *   ''not'' use this.
   */
  def apply(user: User, password: Password): UserInfo =
    UserInfoImpl(Some(user), Some(password), true)

  /**
   * Attempt to parse a [[UserInfo]] value from a percent encoded `String`.
   *
   * @note
   *   Unlike some other data structures in cats-uri, there is no corresponding `fromString`
   *   constructor. This is because [[UserInfo]] can not be represented as an unencoded `String`
   *   in any canonical format. This is due to the fact that the ':' character in the [[User]]
   *   portion of the `String` would create ambiguity with the ':' used as a delimiter between
   *   the user and password fields. If you wish to create a [[UserInfo]] value from a [[User]]
   *   and/or [[Password]] value, you can simply do the following.
   *
   * {{{
   * scala> User.fromString("user").map(u => UserInfo(u))
   * val res0: Either[String, cats.uri.UserInfo] = Right(UserInfo(user = Some(User(value = user)), password = None, hasColonDelimiter = false))
   *
   * scala> UserInfo(user"user") // If you are using the literal syntax from cats.uri.syntax.all._
   * val res1: cats.uri.UserInfo = UserInfo(user = Some(User(value = user)), password = None, hasColonDelimiter = false)
   * }}}
   */
  def fromPercentEncodedString(value: String): Either[DecodingError, UserInfo] =
    PercentDecoder[UserInfo].decode(value)

  /**
   * As [[#fromPercentEncodedString]], but will throw on invalid values.
   *
   * @note
   *   Use of this method is discouraged outside of testing and the REPL.
   */
  def unsafeFromPercentEncodedString(value: String): UserInfo =
    fromPercentEncodedString(value).fold(
      e => throw new IllegalArgumentException(e),
      identity
    )
}
