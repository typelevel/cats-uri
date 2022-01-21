package cats.uri

import java.lang.StringBuilder

trait Appendable[A] extends Serializable {
  import Appendable._

  def append(a: A, appender: Appender): Appender
}

object Appendable {

  def apply[A](implicit ev: Appendable[A]): Appendable[A] = ev

  /**
   * Create an [[Appendable]] instance from any type which is [[Renderable]].
   *
   * @note
   *   You should only use this if your type can only generate a single `String` value. For
   *   example, if your type is composed of more than one sub types, each of which can be
   *   rendered independently, then you should directly implement [[Appendable]] so that you can
   *   add each sub type's value individually to the [[Appender]].
   */
  def fromRenderableString[A](implicit A: Renderable[A]): Appendable[A] =
    new Appendable[A] {
      override def append(a: A, appender: Appender): Appender =
        appender.appendString(A.renderAsString(a))
    }

  /**
   * A very small builder like class, specialized to `String`. It is effectively a very basic
   * wrapper over a `StringBuilder`.
   */
  sealed abstract class Appender {
    protected def stringBuilder: StringBuilder

    /**
     * Append the given unicode codepoint to the [[Appender]].
     */
    final def appendCodePoint(value: Int): Appender = {
      stringBuilder.appendCodePoint(value)
      this
    }

    /**
     * Append the given `String` to the [[Appender]].
     */
    final def appendString(value: String): Appender = {
      stringBuilder.append(value)
      this
    }

    /**
     * Append the given `Char` to the [[Appender]].
     */
    final def appendChar(value: Char): Appender = {
      stringBuilder.append(value)
      this
    }

    /**
     * Render the current value of the [[Appender]] to a `String`.
     */
    final def renderAsString: String =
      stringBuilder.toString
  }

  object Appender {

    /**
     * Create a new empty [[Appender]] value with the given size hint for the pre-allocated
     * buffer.
     */
    def instance(sizeHint: Int): Appender =
      new Appender {
        override val stringBuilder: StringBuilder = new StringBuilder(sizeHint)
      }

    /**
     * Create a new empty [[Appender]] value.
     *
     * @note
     *   If you have a notion of the size of the output, you might consider using the
     *   constructor which takes a size hint.
     */
    def instance: Appender =
      new Appender {
        override val stringBuilder: StringBuilder = new StringBuilder()
      }
  }
}
