package cats.uri.parsers

import cats.parse._

/**
 * Parsers for RFC-3986 grammar productions.
 *
 * @see
 *   [[https://datatracker.ietf.org/doc/html/rfc3986#appendix-A]]
 */
object Rfc3986 {

  // Parsers which do not emit modeled types

  /**
   * An unreserved character according to RFC-3986.
   *
   * @note
   *   unreserved is ''not'' the compliment of reserved.
   *
   * {{{
   * unreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~"
   * }}}
   *
   * @see
   *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-2.3]]
   */
  val unreservedChar: Parser[Char] =
    Rfc5234.alpha | Rfc5234.digit | Parser.charIn('-', '.', '_', '~')

  /**
   * A percent encoded (also commonly refered to as URL encoded) value.
   *
   * @note
   *   Warning! It is common for characters in URIs to be percent encoded even though they are
   *   not required to be, ''however'' this does not mean that a percent encoded character is
   *   valid in ''any'' portion of the URI. For this reason, normalization of the URI must take
   *   place ''after'' parsing. This is why this parser yields the literal input `String` on
   *   success, and not a `Byte` or `Char`.
   *
   * {{{
   * pct-encoded   = "%" HEXDIG HEXDIG
   * }}}
   *
   * @see
   *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-2.1]]
   * @see
   *   [[https://en.wikipedia.org/wiki/Percent-encoding]]
   */
  val percentEncoded: Parser[(Char, Char)] =
    Parser.char('%') *> (Rfc5234.hexdig ~ Rfc5234.hexdig)

  /**
   * A parser for a sub delimiter character.
   *
   * {{{
   * sub-delims    = "!" / "$" / "&" / "'" / "(" / ")"
   *                 / "*" / "+" / "," / ";" / "="
   * }}}
   *
   * @see
   *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-2.2]]
   */
  val subDelimsChar: Parser[Char] =
    Parser.charIn('!', '$', '&', '\'', '(', ')', '*', '+', ',', ';', '=')

  // Parsers for modeled types

  /**
   * A parser for a `String` which reprsents a Scheme.
   *
   * {{{
   * scheme      = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
   * }}}
   *
   * @see
   *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-3.1]]
   */
  val schemeStr: Parser[String] =
    (Rfc5234.alpha *> (Rfc5234.alpha | Rfc5234.digit | Parser.charIn(
      '+',
      '-',
      '.')).rep0).string

  /**
   * A parser for the user section of the userinfo in the authority section of a URI.
   *
   * @note
   *   Unlike many of the parsers here, the user is not explicitly a standalone ABNF production
   *   of RFC-3986. It is modeled separately because the userinfo is comprised of a user moniker
   *   and optionally a password value. The use of this format, e.g. "user:password", is
   *   deprecated, but if encountered requires special handling. For this reason, we parse the
   *   user and password sections of the userinfo separately.
   *
   * {{{
   * userinfo    = *( unreserved / pct-encoded / sub-delims / ":" )
   * }}}
   *
   * @see
   *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1]]
   */
  val userinfoUserStr: Parser0[String] =
    (unreservedChar | percentEncoded | subDelimsChar).rep0.string

  /**
   * A parser for the password section of the userinfo in the authority section of a URI. This
   * field is deprecated in RFC-3986.
   *
   * @note
   *   Unlike many of the parsers here, the password is not explicitly a standalone ABNF
   *   production of RFC-3986. It is modeled separately because the userinfo is comprised of a
   *   user moniker and optionally a password value. The use of this format, e.g.
   *   "user:password", is deprecated, but if encountered requires special handling. For this
   *   reason, we parse the user and password sections of the userinfo separately.
   *
   * {{{
   * userinfo    = *( unreserved / pct-encoded / sub-delims / ":" )
   * }}}
   *
   * @see
   *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1]]
   */
  val userinfoPasswordStr: Parser0[String] =
    (unreservedChar | percentEncoded | subDelimsChar | Parser.char(':')).rep0.string

  /**
   * Lifts a Parser0[String] into a Parser0[Option[String]], yielding `None` if the Parser0
   * parsed no input.
   */
  private def nonEmptyStringParser(p: Parser0[String]): Parser0[Option[String]] =
    p.map(s =>
      if (s.nonEmpty) {
        Some(s)
      } else {
        None
      })

  /**
   * A parser for user info values.
   *
   * The result will yield the user value, if present, whether or not there is a ':' separator
   * character, and the password if present.
   *
   * The second value of the result, `Option[Unit]`, indicates if there was a ':' present in the
   * input. It is `Some(())` if there was one, and `None` if not.
   *
   * According to RFC-3986 the presence or absence of a ':' character in userinfo, assuming the
   * userinfo represents a user and password, should be treated as significant to the
   * interpretation. Thus "username" and "username:" are distinct userinfo values having
   * identical user components.
   *
   * @note
   *   Though not modeled directly as part of this parser's type, it is not possible for there
   *   to be a user value and a password value ''without'' a ':' delimiter. In other words, the
   *   result will never be `(Some(user), None, Some(password))`.
   *
   * See [[UserInfo]] for a higher level model of this and more information.
   *
   * @see
   *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-3.2.1]]
   */
  val userinfo: Parser0[(Option[String], Option[Unit], Option[String])] =
    (nonEmptyStringParser(userinfoUserStr) ~ Parser.char(':').? ~ nonEmptyStringParser(
      userinfoPasswordStr)).map { case ((a, b), c) => (a, b, c) }
}
