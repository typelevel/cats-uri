package cats.uri.syntax

import cats.uri._
import scala.language.future
import scala.quoted.*

private[syntax] trait SchemeSyntax {
  extension (inline ctx: StringContext) {
    inline def scheme(inline args: Any*): Scheme =
      SchemeSyntax.literal(ctx, args)
  }
}

private object SchemeSyntax {

  private def schemeExpr(sc: Expr[StringContext], args: Expr[Seq[Any]])(using q: Quotes): Expr[Scheme] =
    sc.value match {
      case Some(sc) if sc.parts.size == 1 =>
        val value: String = sc.parts.head
        Scheme.fromString(value).fold(
          e => {
            quotes.reflect.report.throwError(e)
          },
          _ => '{Scheme.unsafeFromString(${Expr(value)})}
        )
      case Some(_) =>
        quotes.reflect.report.throwError("StringContext must be a single string literal")
      case None =>
        quotes.reflect.report.throwError("StringContext args must be statically known")
    }

  inline def literal(inline sc: StringContext, inline args: Any*): Scheme =
    ${schemeExpr('sc, 'args)}
}
