package cats.uri.laws

import org.scalacheck._
import org.scalacheck.Prop._
import cats.uri._

trait PercentCodecLaws[A] {
  implicit def encoder: PercentEncoder[A]
  implicit def decoder: PercentDecoder[A]

  final def percentCodecRoundTrip(a: A): Prop =
    decoder.decode(encoder.encode(a)) ?= Right(a)
}

object PercentCodecLaws {
  def apply[A](implicit D: PercentDecoder[A], E: PercentEncoder[A]): PercentCodecLaws[A] =
    new PercentCodecLaws[A] {
      implicit final override val decoder: PercentDecoder[A] = D
      implicit final override val encoder: PercentEncoder[A] = E
    }
}
