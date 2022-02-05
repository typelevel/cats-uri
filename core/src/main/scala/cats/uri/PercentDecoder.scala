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
import scala.annotation.tailrec
import java.nio.charset.StandardCharsets
import java.nio.ByteBuffer
import java.nio.charset.CharsetDecoder

/**
 * A typeclass for percent decoding values.
 *
 * Unlike percent encoding, the fundamental decoding operation is actually
 * identical for all types. It merely perform the `String` based decoding
 * operation, use [[PercentDecoder#decode]]. Because this class goes from
 * `String` to `A`, it necessarily implies parsing as well as decoding.
 *
 * This class also allows for the definition of percent encoding laws, e.g. that any value
 * encoded, should always decode successfully to the original value.
 */
trait PercentDecoder[A] extends Serializable { self =>
  def parseAndDecode(value: String): Either[DecodingError, A]

  final def emap[B](f: A => Either[DecodingError, B]): PercentDecoder[B] =
    new PercentDecoder[B] {
      override def parseAndDecode(value: String): Either[DecodingError, B] =
        self.parseAndDecode(value).flatMap(f)
    }

  final def map[B](f: A => B): PercentDecoder[B] =
    emap[B](a => Right(f(a)))
}

/**
 * Functions in [[PercentDecoder]] provide an implementation of percent decoding.
 *
 * Percent encoded values are `String` values which have sequences of "%XX" where 'X' is a valid
 * hexidecimal character. These '%' prefixed are octets representing UTF-8 byte sequences.
 * Decoding percent encoded `String` values is the process of converting percent prefixed
 * sequences into their the unicode characters represented by the bytes.
 *
 * Percent decoding may fail. This is because not all byte sequences represent valid Unicode
 * characters and according to RFC-3986 percent encoded values should represent characters.
 * `String` values which contain incomplete '%' encoded values also fail percent decoding.
 *
 * A RFC-3986 encoded values is ''not'' the same as a "x-www-form-urlencoded" value, though
 * these two encodings are often treated as the same. The primary distinction between the
 * encodings is that "x-www-form-urlencoded" encoded values represent the space character, ' ',
 * as '+' while percent encoded values represent it using the percent encoded octet mapping to
 * the UTF-8 byte representation, in this case "%20".
 *
 * Another distinction between RFC-3986 percent encoded values and "x-www-form-urlencoded"
 * values is how they handle invalid data. Many "x-www-form-urlencoded" decoders do not fail,
 * handling invalid data by either passing it through, in the case of partial data such as
 * "abc%1g" (%1 is not complete) or by replacing invalid characters with the Unicode replacment
 * character \ufffd. The character replacement strategy used here is a valid when decoding
 * arbitrary textual data and referenced in the Unicode standard, but can not be used with URIs
 * as it can be used to promote an invalid URI into a valid one. This is because \ufffd ''can''
 * occur in a URI (if percent encoded) and if a decoder were to take a URI, or component
 * thereof, containing an invalid byte sequence and replace it with \ufffd, the result would be
 * a valid URI.
 *
 * @see
 *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-2.1]]
 * @see
 *   [[https://www.unicode.org/faq/utf_bom.html#gen8]]
 */
object PercentDecoder {

  def apply[A](implicit ev: PercentDecoder[A]): PercentDecoder[A] =
    ev

  /**
   * Attempt to decode a percent encoded `String` value.
   *
   * @note
   *   This operation should be performed ''after'' parsing a URI into its component sections.
   *   Performing it on a full URI can change the grammar of the URI causing a valid URI to
   *   become invalid or changing transforming a valid URI into a different valid URI, e.g. a
   *   URI which refers to a different resource.
   */
  def decode(value: String): Either[DecodingError, String] = {
    val in: ByteBuffer = ByteBuffer.wrap(value.getBytes(StandardCharsets.UTF_8))
    val out: ByteBuffer = ByteBuffer.allocate(in.remaining())

    def hexCharToByte(char: Byte): Int =
      char match {
        case c if c >= '0' && c <= '9' => (c - '0')
        case c if c >= 'A' && c <= 'F' => 10 + (c - 'A')
        case c if c >= 'a' && c <= 'f' => 10 + (c - 'a')
        case _ =>
          // A sentinel value for error states.
          Int.MaxValue
      }

    @tailrec
    def loop: Either[DecodingError, String] =
      if (in.hasRemaining) {
        in.get() match {
          case '%' =>
            if (in.remaining() >= 2) {
              val hiChar: Byte = in.get()
              hexCharToByte(hiChar) match {
                case Int.MinValue =>
                  Left(
                    DecodingError(
                      in.position() - 1,
                      "Expected hexidecimal character representing the high bits of a percent encoded byte, but byte did not match a valid hexidecimal character.",
                      s"Expected byte representing hexidecimal character, but got ${hiChar}"
                    ))
                case hi =>
                  val lowChar: Byte = in.get()
                  hexCharToByte(lowChar) match {
                    case Int.MinValue =>
                      Left(
                        DecodingError(
                          in.position() - 1,
                          "Expected hexidecimal character representing the low bits of a percent encoded byte, but byte did not match a valid hexidecimal character.",
                          s"Expected byte representing hexidecimal character, but got ${lowChar}"
                        ))
                    case low =>
                      out.put((hi << 4 | low).toByte)
                      loop
                  }
              }
            } else {
              Left(
                DecodingError(
                  in.position() - 1,
                  "Reached end of input after parsing '%'. Expected at least two more hexidecimal characters to decode a percent encoded value. '%' is not legal in a percent encoded String unless it is part of a percent encoded byte sequence."
                ))
            }
          case otherwise =>
            out.put(otherwise)
            loop
        }
      } else {
        // Note: I tried using a ThreadLocal[CharsetDecoder] here, but it
        // didn't show any significant difference in benchmarks.
        val decoder: CharsetDecoder = StandardCharsets.UTF_8.newDecoder()
        out.flip
        ApplicativeError[Either[Throwable, *], Throwable]
          .catchNonFatal(
            decoder.decode(out).toString
          )
          .fold(
            e => {
              val detailedMessage: Option[String] = {
                val message: String = e.getLocalizedMessage()
                if ((message eq null) || message.isEmpty()) {
                  None
                } else {
                  Some(message)
                }
              }

              Left(
                DecodingError(
                  None,
                  "Error encountered when attempting to decode String as UTF-8 bytes after percent decoding.",
                  detailedMessage,
                  Some(e)))
            },
            value => Right(value)
          )
      }

    loop
  }

  /**
   * As [[#decode]], but throws an error if the given `String` is not a valid percent encoded
   * `String`.
   */
  def unsafeDecode(value: String): String =
    decode(value).fold(
      e => throw e,
      identity
    )
}
