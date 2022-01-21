package cats.uri

/**
 * A very light weight typeclass for rendering values as `String` values.
 *
 * "Rendering" in this context means converting the value into a `String` which might be parsed
 * as a valid Uri representation of the given component. This often means percent encoding a
 * value and can include some forms of normalization.
 *
 * @note
 *   This is similar to the cats typeclass `Show`, but in the context of cats-uri, `Show`
 *   instances (and toString) values yield a dbug value, not a rendered URI component. For
 *   example, consider scheme.
 *
 * {{{
 * scala> scheme"http"
 * val res0: cats.uri.Scheme = Scheme(value = http)
 *
 * scala> res0.show
 * val res1: String = Scheme(value = http)
 *
 * scala> res0.renderAsString
 * val res2: String = http
 * }}}
 */
trait Renderable[A] extends Serializable { self =>

  /**
   * Render the given value as a `String`.
   */
  def renderAsString(a: A): String
}

object Renderable {
  def const[A](value: String): Renderable[A] =
    _ => value
}
