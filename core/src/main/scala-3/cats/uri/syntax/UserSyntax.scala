package cats.uri.syntax

import cats.uri.*
import scala.language.future
import scala.quoted.*
import scala.compiletime.*

private[syntax] trait UserSyntax {
  extension (inline ctx: StringContext) {
    inline def user(inline args: Any*): User =
      UserSyntax.userLiteral(ctx, args)

    inline def userEncoded(inline args: Any*): User =
      UserSyntax.userEncodedLiteral(ctx, args)
  }
}

private object UserSyntax {

  private def userLiteralExpr(sc: Expr[StringContext], args: Expr[Seq[Any]])(using q: Quotes): Expr[User] =
    sc.value match {
      case Some(sc) if sc.parts.size == 1 =>
        val value: String = sc.parts.head
        User.fromString(value).fold(
          e => {
            quotes.reflect.report.errorAndAbort(e)
            // quotes.reflect.report.error(e)
          },
          _ => '{User.unsafeFromString(${Expr(value)})}
        )
      case Some(_) =>
        quotes.reflect.report.errorAndAbort("StringContext must be a single string literal")
      case None =>
        quotes.reflect.report.errorAndAbort("StringContext args must be statically known")
    }

  inline def userLiteral(inline sc: StringContext, inline args: Any*): User =
    ${userLiteralExpr('sc, 'args)}

  private def userEncodedExpr(sc: Expr[StringContext], args: Expr[Seq[Any]])(using q: Quotes): Expr[User] =
    sc.value match {
      case Some(sc) if sc.parts.size == 1 =>
        val value: String = sc.parts.head
        User.fromPercentEncodedString(value).fold(
          e => {
            quotes.reflect.report.errorAndAbort(e.sanitizedMessage)
          },
          _ => '{User.unsafeFromPercentEncodedString(${Expr(value)})}
        )
      case Some(_) =>
        quotes.reflect.report.errorAndAbort("StringContext must be a single string literal")
      case None =>
        quotes.reflect.report.errorAndAbort("StringContext args must be statically known")
    }

  inline def userEncodedLiteral(inline sc: StringContext, inline args: Any*): User =
    ${userEncodedExpr('sc, 'args)}
}
