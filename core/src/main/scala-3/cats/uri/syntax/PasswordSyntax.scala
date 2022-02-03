package cats.uri.syntax

import cats.uri._
import scala.language.future
import scala.quoted.*

private[syntax] trait PasswordSyntax {
  extension (inline ctx: StringContext) {
    inline def password(inline args: Any*): Password =
      PasswordSyntax.passwordLiteral(ctx, args)

    inline def passwordEncoded(inline args: Any*): Password =
      PasswordSyntax.passwordEncodedLiteral(ctx, args)
  }
}

private object PasswordSyntax {

  private def passwordLiteralExpr(sc: Expr[StringContext], args: Expr[Seq[Any]])(using q: Quotes): Expr[Password] =
    sc.value match {
      case Some(sc) if sc.parts.size == 1 =>
        val value: String = sc.parts.head
        Password.fromString(value).fold(
          e => {
            quotes.reflect.report.errorAndAbort(e)
          },
          _ => '{Password.unsafeFromString(${Expr(value)})}
        )
      case Some(_) =>
        quotes.reflect.report.errorAndAbort("StringContext must be a single string literal")
      case None =>
        quotes.reflect.report.errorAndAbort("StringContext args must be statically known")
    }

  inline def passwordLiteral(inline sc: StringContext, inline args: Any*): Password =
    ${passwordLiteralExpr('sc, 'args)}

  private def passwordEncodedExpr(sc: Expr[StringContext], args: Expr[Seq[Any]])(using q: Quotes): Expr[Password] =
    sc.value match {
      case Some(sc) if sc.parts.size == 1 =>
        val value: String = sc.parts.head
        Password.fromPercentEncodedString(value).fold(
          e => {
            quotes.reflect.report.errorAndAbort(e.sanitizedMessage)
          },
          _ => '{Password.unsafeFromPercentEncodedString(${Expr(value)})}
        )
      case Some(_) =>
        quotes.reflect.report.errorAndAbort("StringContext must be a single string literal")
      case None =>
        quotes.reflect.report.errorAndAbort("StringContext args must be statically known")
    }

  inline def passwordEncodedLiteral(inline sc: StringContext, inline args: Any*): Password =
    ${passwordEncodedExpr('sc, 'args)}
}
