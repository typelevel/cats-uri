package cats.uri

import cats._
import scala.util.control.NoStackTrace

/**
 * An error which can occur during a decoding operation. It is specialized to provide both an
 * error message which should always be safe to render to logs, the console, etc. and optionally
 * (but usually) a detailed error message which might have sensitive data if the decoding
 * operation was decoding sensitive value.
 */
sealed trait DecodingError
    extends RuntimeException
    with NoStackTrace
    with Product
    with Serializable {

  /**
   * The position in the input at which the error occurred.
   */
  def position: Option[Int]

  /**
   * A description of the error which is always safe to render, e.g. print to the console, log
   * out, etc.
   */
  def sanitizedMessage: String

  /**
   * A detailed error message which may include specific parts of the input string and thus,
   * depending on the context, may not be safe to render.
   */
  def detailedMessage: Option[String]

  /**
   * The underlying exception which triggered the [[DecodingError]]. This is only present when
   * some part of the decoding operation is deferring to another exception based API.
   */
  def cause: Option[Throwable]

  // final

  final override def getMessage: String = sanitizedMessage

  final override def getCause: Throwable =
    cause.getOrElse(null)

  final override def toString: String =
    s"DecodingError(position = ${position}, sanitizedMessage = ${sanitizedMessage})"
}

object DecodingError {
  final private[this] case class DecodingErrorImpl(
      override val position: Option[Int],
      override val sanitizedMessage: String,
      override val detailedMessage: Option[String],
      override val cause: Option[Throwable])
      extends DecodingError

  implicit val showForDecodingError: Show[DecodingError] =
    Show.fromToString

  def apply(
      position: Option[Int],
      sanitizedMessage: String,
      detailedMessage: Option[String],
      cause: Option[Throwable]
  ): DecodingError =
    DecodingErrorImpl(position, sanitizedMessage, detailedMessage, cause)

  def apply(
      position: Int,
      sanitizedMessage: String,
      detailedMessage: Option[String]
  ): DecodingError =
    apply(Some(position), sanitizedMessage, detailedMessage, None)

  def apply(
      position: Int,
      sanitizedMessage: String,
      detailedMessage: String
  ): DecodingError =
    apply(position, sanitizedMessage, Some(detailedMessage))

  def apply(
      position: Int,
      sanitizedMessage: String
  ): DecodingError =
    apply(position, sanitizedMessage, None)

  def apply(
      sanitizedMessage: String,
      detailedMessage: String
  ): DecodingError =
    apply(None, sanitizedMessage, Some(detailedMessage), None)

  def sanitizedMessage(value: String): DecodingError =
    apply(None, value, None, None)

  def unapply(
      value: DecodingError): Some[(Option[Int], String, Option[String], Option[Throwable])] =
    Some((value.position, value.sanitizedMessage, value.detailedMessage, value.cause))
}
