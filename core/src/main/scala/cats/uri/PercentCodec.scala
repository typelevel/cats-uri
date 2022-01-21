package cats.uri

/**
 * A class which provides both percent encoding and decoding.
 *
 * @note
 *   You should ''never'' demand this class implicitly. It only exists to make it easier to
 *   write percent encoder/decoder definitions. You should ''always'' demand [[PercentEncoder]]
 *   and [[PercentDecoder]] as separate implicit constraints.
 */
trait PercentCodec[A] extends PercentDecoder[A] with PercentEncoder[A]

object PercentCodec {

  /**
   * Create a [[PercentCodec]] from a [[PercentDecoder]] and a [[PercentEncoder]].
   */
  def from[A](decoder: PercentDecoder[A], encoder: PercentEncoder[A]): PercentCodec[A] =
    new PercentCodec[A] {
      override def encode(a: A): String =
        encoder.encode(a)

      override def decode(value: String): Either[DecodingError, A] =
        decoder.decode(value)
    }
}
