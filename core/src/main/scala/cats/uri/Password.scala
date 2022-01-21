package cats.uri

import cats._
import cats.syntax.all._
import scala.collection.immutable.BitSet

/**
 * The password component of the passwordinfo in the authority of a URI.
 *
 * @note
 *   A [[Password]] value is not permitted to be empty. If it could be empty then
 *   `Option[Password]` and `Password("")` would create ambiguity as they could both reasonably
 *   be represented by the passwordinfo string ":password".
 *
 * @see
 *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1]]
 */
sealed abstract class Password extends Product with Serializable {

  /**
   * The [[Password]] value as a raw `String`. Warning this value is sensitive.
   */
  def unsafeValue: String

  /**
   * The percent encoded representation of a [[Password]].
   */
  final def encode: String =
    PercentEncoder.encode(Password.passwordAllowedCodepoints.contains)(unsafeValue)

  /**
   * The [[Password]] rendered in the canonical `String` format. This is an alias for
   * [[#encode]].
   */
  final def renderAsString: String =
    encode

  final override def toString: String = "Password(<REDACTED>)"
}

object Password {

  final private[this] case class PasswordImpl(override val unsafeValue: String) extends Password

  /**
   * The set of Unicode code points which do not require percent encoding for a [[Password]]
   * value.
   */
  private val passwordAllowedCodepoints: BitSet =
    Constants.passwordChars.foldMap(c => BitSet(c.toInt))

  implicit val passwordPercentCodec: PercentCodec[Password] =
    PercentCodec.from(
      PercentDecoder.fromDecodedString(value =>
        fromString(value).leftMap(DecodingError.sanitizedMessage)),
      _.encode
    )

  implicit val passwordAppendable: Appendable[Password] =
    Appendable.fromRenderableString[Password]

  implicit val hashAndOrderForPassword: Hash[Password] with Order[Password] =
    new Hash[Password] with Order[Password] {
      override def hash(x: Password): Int =
        x.hashCode

      override def compare(x: Password, y: Password): Int =
        x.unsafeValue.compare(y.unsafeValue)
    }

  implicit val ordering: Ordering[Password] =
    hashAndOrderForPassword.toOrdering

  /**
   * Attempt to create a [[Password]] from a `String`. [[Password]] values must be non-empty to
   * disambiguate them from `Option[Password]`.
   */
  def fromString(value: String): Either[String, Password] =
    if (value.length > 0) {
      Right(PasswordImpl(value))
    } else {
      Left("Password values can not be the empty string.")
    }

  /**
   * Attempt to create a [[Password]] from a percent encoded `String`. [[Password]] values must
   * be non-empty to disambiguate them from `Option[Password]`.
   */
  def fromPercentEncodedString(value: String): Either[DecodingError, Password] =
    PercentDecoder[Password].decode(value)

  /**
   * As [[#fromString]], but will throw on invalid `Strings`.
   *
   * @note
   *   In general, it is recommended that you not use this method outside of test code or the
   *   REPL.
   */
  def unsafeFromString(value: String): Password =
    fromString(value).fold(
      e => throw new IllegalArgumentException(e),
      identity
    )

  /**
   * As [[#fromPercentEncodedString]], but will throw on invalid `Strings`.
   *
   * @note
   *   In general, it is recommended that you not use this method outside of test code or the
   *   REPL.
   */
  def unsafeFromPercentEncodedString(value: String): Password =
    fromPercentEncodedString(value).fold(
      e => throw new IllegalArgumentException(e),
      identity
    )

  def unapply(value: Password): Some[String] =
    Some(value.unsafeValue)
}
