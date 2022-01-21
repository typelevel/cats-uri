package cats.uri.scalacheck.parsers

import org.scalacheck._

/**
 * Scalacheck generators for RFC 3986 grammar productions.
 *
 * @note
 *   These generators are tuned for parser testing. All generated values should be valid for
 *   parsing, but not necessarily valid under other domains. For example, the percent encoded
 *   octet generators here will generate octets which are ''not'' valid percent encoded UTF-8
 *   bytes. If you are looking for generators which are valid for something other than parsing,
 *   consider the other generators defined in `cats.uri.scalacheck`.
 */
object Rfc3986ScalacheckInstances {

  /**
   * Generate one of the unreserved characters from RFC-3986.
   *
   * These characters never require percent encoding.
   *
   * @see
   *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-2.3]]
   */
  val genUnreservedChar: Gen[Char] =
    Gen.oneOf(Gen.alphaChar, Gen.numChar, Gen.oneOf('-', '.', '_', '~'))

  /**
   * Generate an unreserved character, typed as a `String`. This is different than
   * `genUnreservedString`, which generates a `String` (of arbitrary length) of unreserved
   * characters. This generator always generate a `String` of exactly one character from the
   * unreserved set.
   *
   * @see
   *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-2.3]]
   */
  val genUnreservedCharString: Gen[String] =
    genUnreservedChar.map(_.toString)

  /**
   * Generate a `String` value of unreserved characters.
   *
   * @see
   *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-2.3]]
   */
  val genUnreservedString: Gen[String] =
    Gen.stringOf(genUnreservedChar)

  /**
   * Generate a `String` value of the form "%XY" where X and Y are arbitrary valid hexidecimal
   * characters.
   *
   * @note
   *   The octet represented by this generator is ''not'' guaranteed to be a valid UTF-8 byte
   *   sequence. This generator is only useful testing parsing semantics, which are separate
   *   from percent decoding.
   */
  val genPercentOctetString: Gen[String] =
    Gen.hexChar.flatMap(a => Gen.hexChar.map(b => s"%$a$b"))

  /**
   * Generate a character in the sub-delims set from RFC-3986.
   *
   * @see
   *   [[https://datatracker.ietf.org/doc/html/rfc3986/#appendix-A]]
   */
  val genSubDelimChar: Gen[Char] =
    Gen.oneOf(
      '!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '='
    )

  /**
   * Generate a sub-delim character, typed as a `String`. This is different than
   * `genSubDelimString`, which generates a `String` (of arbitrary length) of sub-delim
   * characters. This generator always generate a `String` of exactly one character from the
   * sub-delim set.
   */
  val genSubDelimCharString: Gen[String] =
    genSubDelimChar.map(_.toString)

  /**
   * Generate a `String` value of sub-delims character.
   */
  val genSubDelimString: Gen[String] =
    Gen.stringOf(genSubDelimChar)

  /**
   * Generates a string which is a valid user from the userinfo section of the authority of a
   * URI.
   */
  val genUserinfoUserString: Gen[String] =
    Gen.frequency(
      1 -> Gen.const(""),
      19 -> Gen
        .listOf(
          Gen.oneOf(
            genUnreservedCharString,
            genPercentOctetString,
            genSubDelimCharString
          )
        )
        .map(_.mkString)
    )

  /**
   * Generates a string which is a valid scheme.
   */
  val genSchemeString: Gen[String] =
    for {
      head <- Gen.alphaChar
      tail <- Gen.listOf(
        Gen.oneOf(Gen.alphaChar, Gen.numChar, Gen.const('+'), Gen.const('-'), Gen.const('.')))
    } yield s"${head}${tail.mkString}"
}
