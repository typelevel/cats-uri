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

  private def userLiteralExpr(sc: Expr[StringContext], args: Expr[Seq[Any]])(
      using q: Quotes): Expr[User] =
    sc.value match {
      case Some(sc) if sc.parts.size == 1 =>
        val value: String = sc.parts.head
        User
          .fromString(value)
          .fold(
            e => {
              quotes.reflect.report.throwError(e)
              // quotes.reflect.report.error(e)
            },
            _ => '{ User.unsafeFromString(${ Expr(value) }) }
          )
      case Some(_) =>
        quotes.reflect.report.throwError("StringContext must be a single string literal")
      case None =>
        quotes.reflect.report.throwError("StringContext args must be statically known")
    }

  inline def userLiteral(inline sc: StringContext, inline args: Any*): User =
    ${ userLiteralExpr('sc, 'args) }

  private def userEncodedExpr(sc: Expr[StringContext], args: Expr[Seq[Any]])(
      using q: Quotes): Expr[User] =
    sc.value match {
      case Some(sc) if sc.parts.size == 1 =>
        val value: String = sc.parts.head
        User
          .fromPercentEncodedString(value)
          .fold(
            e => {
              quotes.reflect.report.throwError(e.sanitizedMessage)
            },
            _ => '{ User.unsafeFromPercentEncodedString(${ Expr(value) }) }
          )
      case Some(_) =>
        quotes.reflect.report.throwError("StringContext must be a single string literal")
      case None =>
        quotes.reflect.report.throwError("StringContext args must be statically known")
    }

  inline def userEncodedLiteral(inline sc: StringContext, inline args: Any*): User =
    ${ userEncodedExpr('sc, 'args) }
}
