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
import java.nio.CharBuffer
import scala.annotation.tailrec

/**
 * A typeclass for percent encoding values.
 *
 * The exact set of characters which are required to be percent encoded is dependent on the Uri
 * component and the Uri scheme in play.
 *
 * @see
 *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-2.1]]
 */
trait PercentEncoder[A] extends Renderable[A] { self =>

  /**
   * Encode the given value as a percent encoded `String`.
   */
  def encode(a: A): String

  final override def renderAsString(a: A): String =
    encode(a)
}

object PercentEncoder {
  implicit def apply[A](implicit ev: PercentEncoder[A]): PercentEncoder[A] =
    ev

  private val PercentUnicodeCodePoint: Int = 0x25

  private[this] val HexChars: Array[Char] =
    "0123456789ABCDEF".toArray

  /**
   * Percent encode a `String` value.
   *
   * Percent encoding for URI values is always defined to use the UTF-8 byte representation of
   * encoded characters. For this reason, all `String` values are safe to encode.
   *
   * The set of characters which need to be encoded is dependent on the specific URI component
   * being encoded as well as the scheme. This function is a general utility function and end
   * users should generally prefer instances of the [[PercentEncoder]] class specific to some
   * data type. This function may be of use to library authors implementing rules for a specific
   * scheme, lacking native support in cats-uri.
   *
   * @note
   *   The unicode code point corresponding to '%' will ''always'' be encoded, even if the
   *   provided predicate returns `false` for this value. This is because there is no `String`
   *   which can be considered "percent encoded" if it contains unencoded '%' values.
   */
  def encode(allowedCodePointPredicate: Int => Boolean)(value: String): String = {
    val len: Int = value.length
    val buffer: CharBuffer = CharBuffer.allocate(value.length * 12)

    @tailrec
    def loop(index: Int): String =
      if (index >= len) {
        buffer.flip.toString
      } else {
        val codePoint: Int = value.codePointAt(index)
        val indexIncrement: Int = if (codePoint >= 0x10000) 2 else 1

        if (allowedCodePointPredicate(codePoint) && codePoint =!= PercentUnicodeCodePoint) {
          buffer.put(Character.toChars(codePoint))
        } else {
          if (codePoint < 0x80) {
            // 1 byte
            buffer.put(Array('%', HexChars(codePoint >> 4 & 0x0f), HexChars(codePoint & 0x0f)))
          } else if (codePoint < 0x800) {
            // 2 bytes
            val byte1: Int = codePoint >> 6 | 0xc0
            val byte2: Int = (codePoint & 0x3f) | 0x80
            buffer.put(
              Array(
                '%',
                HexChars(byte1 >> 4 & 0x0f),
                HexChars(byte1 & 0x0f),
                '%',
                HexChars(byte2 >> 4 & 0x0f),
                HexChars(byte2 & 0x0f)
              )
            )
          } else if (codePoint < 0x10000) {
            // 3 bytes
            val byte1: Int = codePoint >> 12 | 0xe0
            val byte2: Int = (codePoint >> 6 & 0x3f) | 0x80
            val byte3: Int = (codePoint & 0x3f) | 0x80
            buffer.put(
              Array(
                '%',
                HexChars(byte1 >> 4 & 0x0f),
                HexChars(byte1 & 0x0f),
                '%',
                HexChars(byte2 >> 4 & 0x0f),
                HexChars(byte2 & 0x0f),
                '%',
                HexChars(byte3 >> 4 & 0x0f),
                HexChars(byte3 & 0x0f)
              )
            )
          } else {
            // 4 bytes
            val byte1: Int = codePoint >> 18 | 0xf0
            val byte2: Int = (codePoint >> 12 & 0x3f) | 0x80
            val byte3: Int = (codePoint >> 6 & 0x3f) | 0x80
            val byte4: Int = (codePoint & 0x3f) | 0x80
            buffer.put(
              Array(
                '%',
                HexChars(byte1 >> 4 & 0x0f),
                HexChars(byte1 & 0x0f),
                '%',
                HexChars(byte2 >> 4 & 0x0f),
                HexChars(byte2 & 0x0f),
                '%',
                HexChars(byte3 >> 4 & 0x0f),
                HexChars(byte3 & 0x0f),
                '%',
                HexChars(byte4 >> 4 & 0x0f),
                HexChars(byte4 & 0x0f)
              )
            )
          }
        }

        loop(index + indexIncrement)
      }

    loop(0)
  }

  /**
   * As [[#encode]], but encodes all characters in the `String`.
   *
   * @note
   *   Use of this method is usually wrong. RFC-3986 states that URI producing applications
   *   should only encode those character required by the component in question.
   */
  def encodeAll(value: String): String =
    encode(_ => false)(value)

  /**
   * As [[#encode]], but only encodes '%' in the `String`. This is the minimal encoding which
   * can be considered a valid percent encoded `String`.
   *
   * @note
   *   Use of this method is usually wrong. Most URI components require some additional
   *   characters to be percent encoded.
   */
  def encodeMinimal(value: String): String =
    encode(_ => true)(value)
}
