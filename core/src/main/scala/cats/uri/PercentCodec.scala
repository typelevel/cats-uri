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

/**
 * A class which provides both percent encoding and decoding.
 *
 * @note
 *   You should ''never'' demand this class implicitly. It only exists to make it easier to
 *   write percent encoder/decoder definitions. You should ''always'' demand [[PercentEncoder]]
 *   and [[PercentDecoder]] as separate implicit constraints.
 */
trait PercentCodec[A] extends PercentDecoder[A] with PercentEncoder[A]

object PercentCodec {

  /**
   * Create a [[PercentCodec]] from a [[PercentDecoder]] and a [[PercentEncoder]].
   */
  def from[A](decoder: PercentDecoder[A], encoder: PercentEncoder[A]): PercentCodec[A] =
    new PercentCodec[A] {
      override def encode(a: A): String =
        encoder.encode(a)

      override def parseAndDecode(value: String): Either[DecodingError, A] =
        decoder.parseAndDecode(value)
    }
}
