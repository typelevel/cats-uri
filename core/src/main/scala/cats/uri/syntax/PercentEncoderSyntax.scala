package cats.uri.syntax

import cats.uri._

private[syntax] trait PercentEncoderSyntax extends RenderableSyntax {
  import PercentEncoderSyntax._

  implicit final def percentEncoderSyntax[A](target: A)(
      implicit tc: PercentEncoder[A]): PercentEncoderOps[A] =
    new PercentEncoderOps(target)
}

private object PercentEncoderSyntax {
  final class PercentEncoderOps[A](private val a: A)(implicit tc: PercentEncoder[A]) {
    def encode: String =
      tc.encode(a)
  }
}
