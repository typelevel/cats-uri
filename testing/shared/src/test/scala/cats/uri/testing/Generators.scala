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

package cats.uri.testing

import org.scalacheck._
import scala.annotation.tailrec
import scala.collection.immutable.BitSet

private[testing] object Generators {

  val MaxUnicodeCodePoint: Int = 0x10ffff
  val ValidHexUnicodeCodePoints: BitSet =
    "0123456789abcdefABCDEF".toList.foldLeft(BitSet.empty) {
      case (acc, value) => acc + value.toInt
    }

  /**
   * Utility method for converting a byte into a percent encoded representation.
   *
   * This does not imply that it is valid. In fact we use it to create invalid UTF-8 byte
   * sequences and ensure the PercentDecoder handles them properly.
   */
  def byteToPercentHexString(b: Byte): String = {
    val hi: Int = b >> 4 & 0x0f
    val low: Int = b & 0x0f

    def intToHexChar(i: Int): Char =
      if (i >= 0 && i <= 9) {
        (i + '0'.toInt).toChar
      } else {
        ('A'.toInt + i - 10).toChar
      }

    s"%${intToHexChar(hi)}${intToHexChar(low)}"
  }

  def unvalidatedCodePointToBytes(codePoint: Int): Vector[Int] =
    if (codePoint <= 0x7f) {
      // 1 byte
      Vector(codePoint)
    } else if (codePoint <= 0x7ff) {
      // 2 bytes
      Vector((codePoint >> 6) | 0xc0, (codePoint & 0x3f) | 0x80)
    } else if (codePoint <= 0xffff) {
      // 3 bytes
      Vector(
        (codePoint >> 12) | 0xe0,
        ((codePoint >> 6) & 0x3f) | 0x80,
        (codePoint & 0x3f) | 0x80)
    } else {
      // 4 bytes
      //
      // This can generate invalid UTF-8 byte representations, which is what
      // we want when testing.
      Vector(
        (codePoint >> 18) | 0xf0,
        ((codePoint >> 12) & 0x3f) | 0x80,
        ((codePoint >> 6) & 0x3f) | 0x80,
        (codePoint & 0x3f) | 0x80)
    }

  def stringToSequenceOfCodePoints(value: String): Vector[Int] = {
    @tailrec
    def loop(index: Int, acc: Vector[Int]): Vector[Int] = {
      if (index < value.length) {
        val codePoint: Int = value.codePointAt(index)
        loop(
          index + Character.charCount(codePoint),
          acc ++ Vector(codePoint)
        )
      } else {
        acc
      }
    }

    loop(0, Vector.empty[Int])
  }

  def shuffleCaseCodePointGen(codePoint: Int): Gen[Int] =
    Gen.oneOf(
      Character.toLowerCase(codePoint),
      Character.toUpperCase(codePoint),
      Character.toTitleCase(codePoint)
    )

  def shuffleCaseGen(value: Gen[String]): Gen[String] =
    value.flatMap { value =>
      stringToSequenceOfCodePoints(value)
        .foldLeft(Gen.const(Vector.empty[Int])) {
          case (acc, value) =>
            for {
              acc <- acc
              caseSwitch <- shuffleCaseCodePointGen(value)
            } yield acc ++ Vector(caseSwitch)
        }
        .map(codePoints => new String(codePoints.toArray, 0, codePoints.length))
    }

  val genUnicodeCodePoints: Gen[Int] =
    Gen.choose(0, MaxUnicodeCodePoint)

  val genNonHexCodePointStrings: Gen[String] =
    shuffleCaseGen(
      genUnicodeCodePoints
        .filterNot(
          ValidHexUnicodeCodePoints.contains
        )
        .map(codePoint => new String(Character.toChars(codePoint)))
    )

  val genOverlongUTF8Encodings: Gen[String] = {
    val genOverlongEncodingsFor1ByteValues: Gen[List[Byte]] =
      Gen
        .choose(0, 0x7f)
        .flatMap(codePoint =>
          Gen.oneOf(
            // The overlong two byte encoding.
            List((codePoint >> 6) | 0xc0, (codePoint & 0x3f) | 0x80),
            // The overlong three byte encoding.
            List(0xe0, (codePoint >> 6) | 0x80, (codePoint & 0x3f) | 0x80),
            // The overlong four byte encoding.
            List(0xf0, 0x80, (codePoint >> 6) | 0x80, (codePoint & 0x3f) | 0x80)
          ))
        .map(_.map(_.toByte))

    val genOverlongEncodingsFor2ByteValues: Gen[List[Byte]] =
      Gen
        .choose(0x80, 0x7ff)
        .flatMap(codePoint =>
          Gen.oneOf(
            // The overlong three byte encoding.
            List(0xe0, (codePoint >> 6) | 0x80, ((codePoint & 0x3f) | 0x80)),
            // The overlong four byte encoding.
            List(0xf0, 0x80, (codePoint >> 6) | 0x80, ((codePoint & 0x3f) | 0x80))
          ))
        .map(_.map(_.toByte))

    val genOverlongEncodingsFor3ByteValues: Gen[List[Byte]] =
      Gen
        .choose(0x800, 0xffff)
        .flatMap(codePoint =>
          // The overlong four byte encoding.
          List(
            0xf0,
            (codePoint >> 12) | 0x80,
            ((codePoint >> 6) & 0x3f) | 0x80,
            (codePoint & 0x3f) | 0x80))
        .map(_.map(_.toByte))

    shuffleCaseGen(
      Gen
        .oneOf(
          genOverlongEncodingsFor1ByteValues,
          genOverlongEncodingsFor2ByteValues,
          genOverlongEncodingsFor3ByteValues
        )
        .map(_.map(byteToPercentHexString).mkString)
    )
  }

  val genInvalidUnicodeByteSequence: Gen[List[Byte]] = {
    val genByte: Gen[Byte] =
      Arbitrary.arbitrary[Byte]

    val genInvalid1ByteSequences: Gen[Byte] =
      genByte.map(_ | 0x80).map(_.toByte)

    val genInvalidNonLeadingByte: Gen[Byte] =
      genByte.flatMap(b =>
        Gen
          .oneOf(
            b & 0x7f,
            b | 0xc0
          )
          .map(_.toByte))

    val genInvalid2ByteSequences: Gen[(Byte, Byte)] = {
      val genValidByte1: Gen[Byte] =
        genByte.map(b => ((b | 0xc0) & 0xdf).toByte)

      genValidByte1.flatMap(a => genInvalidNonLeadingByte.map(b => (a, b)))
    }

    val genInvalid3ByteSequences: Gen[(Byte, Byte, Byte)] = {
      val genValidByte1: Gen[Byte] =
        genByte.map(b => ((b | 0xe0) & 0xef).toByte)

      Gen.oneOf(
        genValidByte1.flatMap(a =>
          genInvalidNonLeadingByte.flatMap(b => genByte.map(c => (a, b, c)))),
        genValidByte1.flatMap(a =>
          genInvalidNonLeadingByte.flatMap(b => genInvalidNonLeadingByte.map(c => (a, b, c)))),
        genValidByte1.flatMap(a =>
          genByte.flatMap(b => genInvalidNonLeadingByte.map(c => (a, b, c))))
      )
    }

    val genInvalid4ByteSequences: Gen[(Byte, Byte, Byte, Byte)] = {
      val genValidByte1: Gen[Byte] =
        genByte.map(b => ((b | 0xf0) & 0xf7).toByte)

      Gen.oneOf(
        genValidByte1.flatMap(a =>
          genInvalidNonLeadingByte.flatMap(b =>
            genByte.flatMap(c => genByte.map(d => (a, b, c, d))))),
        genValidByte1.flatMap(a =>
          genInvalidNonLeadingByte.flatMap(b =>
            genInvalidNonLeadingByte.flatMap(c => genByte.map(d => (a, b, c, d))))),
        genValidByte1.flatMap(a =>
          genInvalidNonLeadingByte.flatMap(b =>
            genInvalidNonLeadingByte.flatMap(c =>
              genInvalidNonLeadingByte.map(d => (a, b, c, d))))),
        genValidByte1.flatMap(a =>
          genByte.flatMap(b =>
            genInvalidNonLeadingByte.flatMap(c =>
              genInvalidNonLeadingByte.map(d => (a, b, c, d))))),
        genValidByte1.flatMap(a =>
          genByte.flatMap(b =>
            genInvalidNonLeadingByte.flatMap(c => genByte.map(d => (a, b, c, d))))),
        genValidByte1.flatMap(a =>
          genByte.flatMap(b =>
            genByte.flatMap(c => genInvalidNonLeadingByte.map(d => (a, b, c, d)))))
      )
    }

    Gen.oneOf(
      genInvalid1ByteSequences.map(b => List(b)),
      genInvalid2ByteSequences.map { case (a, b) => List(a, b) },
      genInvalid3ByteSequences.map { case (a, b, c) => List(a, b, c) },
      genInvalid4ByteSequences.map { case (a, b, c, d) => List(a, b, c, d) }
    )
  }

  val genInvalidPercentEncodedString: Gen[String] =
    shuffleCaseGen(
      genInvalidUnicodeByteSequence.map(bytes => bytes.map(byteToPercentHexString).mkString)
    )

  /**
   * Generates `String` values which are percent prefix, then either contain one valid hex char,
   * or no valid hex chars.
   */
  val genNonHexPercentPrefixString: Gen[String] = {
    val genHexThenNonHex: Gen[String] =
      for {
        c <- Gen.hexChar
        nonHexCodePoint <- genNonHexCodePointStrings
      } yield s"${c}${nonHexCodePoint}"

    val genNonHexThenHex: Gen[String] =
      for {
        nonHexCodePoint <- genNonHexCodePointStrings
        c <- Gen.hexChar
      } yield s"${nonHexCodePoint}${c}"

    val genNonHexThenNonHex: Gen[String] =
      for {
        nonHexCodePointA <- genNonHexCodePointStrings
        nonHexCodePointB <- genNonHexCodePointStrings
      } yield nonHexCodePointA ++ nonHexCodePointB

    shuffleCaseGen(
      Gen
        .oneOf(
          genHexThenNonHex,
          genNonHexThenHex,
          genNonHexThenNonHex,
          Gen.hexChar.map(_.toString)
        )
        .map(value => s"%${value}")
    )
  }

  val genLargerThanUTF8Range: Gen[String] =
    shuffleCaseGen(
      Gen
        .choose(0x110000, 0x1fffff)
        .map(invalidCodePoint =>
          unvalidatedCodePointToBytes(invalidCodePoint)
            .map(i => byteToPercentHexString(i.toByte))
            .mkString)
    )
}
