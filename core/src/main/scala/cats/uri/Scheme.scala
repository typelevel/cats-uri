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
import cats.syntax.all._
import cats.parse._
import org.typelevel.ci._
import scala.collection.immutable.SortedMap
import scala.collection.immutable.SortedSet
import cats.uri.parsers._

/**
 * The scheme of a URI.
 *
 * Schemes are case-insensitive, but are canonically shown as lower case.
 *
 * @note
 *   Unlike many Uri components, [[Scheme]] values should not be percent encoded.
 *
 * {{{
 * scheme      = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
 * }}}
 *
 * @see
 *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-3.1]]
 */
sealed abstract class Scheme extends Product with Serializable {
  def value: CIString

  /**
   * Render the value in the canonical `String` form.
   */
  final def renderAsString: String =
    value.toString.toLowerCase

  final override def toString: String = s"Scheme(value = ${renderAsString})"
}

object Scheme {
  final private[this] case class SchemeImpl(override val value: CIString) extends Scheme

  private[this] object SchemeImpl {
    def from(value: CIString): Scheme =
      ianaSchemeMapping.get(value).getOrElse(SchemeImpl.apply(value))

    def from(value: String): Scheme =
      from(CIString(value))
  }

  implicit val hashAndOrderForScheme: Hash[Scheme] with Order[Scheme] =
    new Hash[Scheme] with Order[Scheme] {
      override def hash(x: Scheme): Int = x.hashCode

      override def compare(x: Scheme, y: Scheme): Int =
        x.value.compare(y.value)
    }

  implicit val ordering: Ordering[Scheme] =
    hashAndOrderForScheme.toOrdering

  implicit val showForScheme: Show[Scheme] =
    Show.fromToString

  implicit val schemeRenderable: Renderable[Scheme] =
    new Renderable[Scheme] {
      override def renderAsString(a: Scheme): String =
        a.renderAsString
    }

  implicit val schemeAppendable: Appendable[Scheme] =
    Appendable.fromRenderableString[Scheme]

  def unapply(value: Scheme): Some[CIString] =
    Some(value.value)

  /**
   * Parser for [[Scheme]].
   *
   * {{{
   * scheme      = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
   * }}}
   *
   * @see
   *   [[https://datatracker.ietf.org/doc/html/rfc3986#section-3.1]]
   */
  def parser: Parser[Scheme] =
    Rfc3986.schemeStr.map(value => SchemeImpl.from(CIString(value)))

  /**
   * A static mapping of known IANA schemes so that we can intern common schemes and skip
   * parsing ins some cases.
   */
  private val ianaSchemeMapping: SortedMap[CIString, Scheme] =
    SchemeDB.ianaSchemes.foldLeft(SortedMap.empty[CIString, Scheme]) {
      case (acc, value) =>
        // We go through the schemeStr here as a fail fast sanity check. We
        // have to be careful to bypass SchemeImpl.from, which would attempt
        // to intern the result, which would cause a loop.
        Rfc3986
          .schemeStr
          .parseAll(value.toString)
          .fold(
            _ =>
              throw new AssertionError(
                s"Static IANA scheme ${value} failed parsing. This is a cats-uri bug."),
            _ => acc + (value -> SchemeImpl(value))
          )
    }

  /**
   * A canonical set of schemes as registered with IANA.
   *
   * @see
   *   [[https://www.iana.org/assignments/uri-schemes/uri-schemes.xhtml]]
   */
  val ianaSchemes: SortedSet[Scheme] =
    ianaSchemeMapping.foldMap(value => SortedSet(value))

  /**
   * Attempt to create a [[Scheme]] from a `String`.
   */
  def fromString(value: String): Either[String, Scheme] = {
    val trimmed: String = value.trim
    ianaSchemeMapping
      .get(CIString(trimmed))
      .map(value => Right(value))
      .getOrElse(
        parser
          .parseAll(trimmed)
          .leftMap(_ =>
            s"Invalid URI scheme: ${value}. A URI Scheme must be at least one alpha character, followed by zero or more [A-Za-z0-9+-.] characters.")
      )
  }

  /**
   * Create a [[Scheme]] from a `String`, throwing an error if the `String` is not a valid URI
   * [[Scheme]].
   *
   * @note
   *   In general, it is recommended that you not use this method outside of test code or the
   *   REPL.
   */
  def unsafeFromString(value: String): Scheme =
    fromString(value).fold(
      e => throw new IllegalArgumentException(e),
      identity
    )
}
