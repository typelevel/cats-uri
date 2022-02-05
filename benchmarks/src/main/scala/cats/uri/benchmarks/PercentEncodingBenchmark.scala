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

import cats.uri._
import org.openjdk.jmh.annotations._
import com.google.common.net.PercentEscaper
import scala.collection.immutable.BitSet
import org.http4s.Uri
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput, Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class PercentEncodingBenchmark {
  import PercentEncodingBenchmark._

  var currentSeed: Long = Long.MinValue // scalafix:ok

  def nextSeed: Long = {
    val s: Long = currentSeed
    currentSeed = currentSeed + 1L
    s
  }

  def nextString: String =
    DataGenerators.generateString(nextSeed)

  @Benchmark
  def catsUriPercentEncoder: String =
    PercentEncoder.encode(guavaNeverEncodedCodepoints.contains)(nextString)

  @Benchmark
  def guavaUriPercentEncoder: String =
    guavaPercentEscaper.escape(nextString)

  @Benchmark
  def http4sUriEncoder: String =
    Uri.encode(
      nextString,
      java.nio.charset.StandardCharsets.UTF_8,
      false,
      c => guavaNeverEncodedCodepoints.contains(c.toInt))
}

object PercentEncodingBenchmark {

  private val guavaNeverEncodedCodepoints: BitSet =
    (('a' to 'z') ++ ('0' to '9')).foldLeft(BitSet.empty) {
      case (acc, value) =>
        (acc + value.toInt) + value.toUpper.toInt
    }

  private val guavaPercentEscaper: PercentEscaper =
    new PercentEscaper("", false)
}
