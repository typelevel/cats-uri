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

package cats.uri.laws

import org.scalacheck._
import org.scalacheck.Prop._
import cats.uri._

trait PercentCodecLaws[A] {
  implicit def encoder: PercentEncoder[A]
  implicit def decoder: PercentDecoder[A]

  final def percentCodecRoundTrip(a: A): Prop =
    decoder.parseAndDecode(encoder.encode(a)) ?= Right(a)
}

object PercentCodecLaws {
  def apply[A](implicit D: PercentDecoder[A], E: PercentEncoder[A]): PercentCodecLaws[A] =
    new PercentCodecLaws[A] {
      implicit final override val decoder: PercentDecoder[A] = D
      implicit final override val encoder: PercentEncoder[A] = E
    }
}
