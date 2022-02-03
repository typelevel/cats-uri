package cats.uri.syntax

import cats.uri._
import scala.language.future
import scala.quoted.*

private[syntax] trait UserInfoSyntax {
  extension (inline ctx: StringContext) {
    inline def userInfoEncoded(inline args: Any*): UserInfo =
      UserInfoSyntax.userInfoEncodedLiteral(ctx, args)
  }
}

private object UserInfoSyntax {

  private def userInfoEncodedExpr(sc: Expr[StringContext], args: Expr[Seq[Any]])(using q: Quotes): Expr[UserInfo] =
    sc.value match {
      case Some(sc) if sc.parts.size == 1 =>
        val value: String = sc.parts.head
        UserInfo.fromPercentEncodedString(value).fold(
          e => {
            quotes.reflect.report.errorAndAbort(e.sanitizedMessage)
          },
          _ => '{UserInfo.unsafeFromPercentEncodedString(${Expr(value)})}
        )
      case Some(_) =>
        quotes.reflect.report.errorAndAbort("StringContext must be a single string literal")
      case None =>
        quotes.reflect.report.errorAndAbort("StringContext args must be statically known")
    }

  inline def userInfoEncodedLiteral(inline sc: StringContext, inline args: Any*): UserInfo =
    ${userInfoEncodedExpr('sc, 'args)}
}
