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
import cats.uri._
import java.net.URLDecoder
import java.util.concurrent.TimeUnit
import org.http4s.Uri
import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput, Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class PercentDecodingBenchmark {

  var currentSeed: Long = Long.MinValue // scalafix:ok

  // Benchmarks decoding strings random strings where all Unicode code points
  // are encoded, even members of the unreserved set from RFC-3986..

  def nextSeed: Long = {
    val s: Long = currentSeed
    currentSeed = currentSeed + 1L
    s
  }

  def nextString: String =
    DataGenerators.generateString(nextSeed)

  def nextPred: Int => Boolean =
    DataGenerators.generateEncoderPredicate(nextSeed)

  @Benchmark
  def catsUriPercentDecoderAll: String = {
    val s: String      = nextString
    val result: String = PercentDecoder.unsafeDecode(PercentEncoder.encodeAll(s))
    assert(s === result)
    result
  }

  // Note the URLDecoder isn't actually doing the same operation in general,
  // but for many strings it does yield the same result. It is also commonly
  // used to perform percent encoding, which should be followed up with
  // repairing the result to actually match RFC-3986. Thus, it still serves as
  // a useful comparison benchmark.
  @Benchmark
  def javaStandardLibPercentDecoderAll: String = {
    val s: String      = nextString
    val result: String = URLDecoder.decode(PercentEncoder.encodeAll(s), "UTF-8")
    assert(s === result)
    result
  }

  @Benchmark
  def http4sUriDecoderAll: String = {
    val s: String      = nextString
    val result: String = Uri.decode(PercentEncoder.encodeAll(s))
    assert(s === result)
    result
  }

  // Benchmarks encoding random strings where only the minimal set of
  // characters are encoded, specifically only the '%' character.

  @Benchmark
  def catsUriPercentDecoderMin: String = {
    val s: String      = nextString
    val result: String = PercentDecoder.unsafeDecode(PercentEncoder.encodeMinimal(s))
    assert(s === result)
    result
  }

  // Note the URLDecoder isn't actually doing the same operation in general,
  // but for many strings it does yield the same result. It is also commonly
  // used to perform percent encoding, which should be followed up with
  // repairing the result to actually match RFC-3986. Thus, it still serves as
  // a useful comparison benchmark.
  @Benchmark
  def javaStandardLibPercentDecoderMin: String = {
    val s: String      = nextString
    val result: String = URLDecoder.decode(PercentEncoder.encodeMinimal(s), "UTF-8")
    assert(s === result)
    result
  }

  @Benchmark
  def http4sUriDecoderMin: String = {
    val s: String      = nextString
    val result: String = Uri.decode(PercentEncoder.encodeMinimal(s))
    assert(s === result)
    result
  }

  // Benchmarks decoding random string values where a random set of characters
  // are encoded.
  //
  // NOTE: These benchmarks are _much_ slower than the Min/All benchmarks
  // above for all the decoders tested here. This is almost certainly because
  // the generated predicate for determining which characters should be
  // encoded is going to be _much_ slower than the predicates used in
  // All/Min. It does _not_ imply that the decoders actually run slower here
  // in practice.

  @Benchmark
  def catsUriPercentDecoderMixed: String = {
    val s: String            = nextString
    val pred: Int => Boolean = nextPred
    val result: String       = PercentDecoder.unsafeDecode(PercentEncoder.encode(pred)(s))
    assert(s === result)
    result
  }

  // Note the URLDecoder isn't actually doing the same operation in general,
  // but for many strings it does yield the same result. It is also commonly
  // used to perform percent encoding, which should be followed up with
  // repairing the result to actually match RFC-3986. Thus, it still serves as
  // a useful comparison benchmark.
  @Benchmark
  def javaStandardLibPercentDecoderMixed: String = {
    val s: String            = nextString
    val pred: Int => Boolean = nextPred
    val result: String       = URLDecoder.decode(PercentEncoder.encode(pred)(s), "UTF-8")
    assert(s === result)
    result
  }

  @Benchmark
  def http4sUriDecoderMixed: String = {
    val s: String            = nextString
    val pred: Int => Boolean = nextPred
    val result: String       = Uri.decode(PercentEncoder.encode(pred)(s))
    assert(s === result)
    result
  }
}
