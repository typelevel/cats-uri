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

package cats.uri.benchmarks

import cats.syntax.all._
import org.scalacheck._

object DataGenerators {

  val genString: Gen[String] =
    Arbitrary.arbitrary[String]

  val genPred: Gen[Int => Boolean] =
    Arbitrary.arbitrary[Int => Boolean]

  def generateString(seed: Long): String = {
    genString(Gen.Parameters.default, rng.Seed(seed))
      .getOrElse(throw new AssertionError("Failed to generate string"))
  }

  def generateEncoderPredicate(seed: Long): Int => Boolean = {
    val f: Int => Boolean = genPred(Gen.Parameters.default, rng.Seed(seed))
      .getOrElse(throw new AssertionError("Failed to generate encoder predicate"))

    // We must ensure that any encoder predicate always encodes '%'.
    (codePoint: Int) => {
      (codePoint =!= '%'.toInt) && f(codePoint)
    }
  }
}
