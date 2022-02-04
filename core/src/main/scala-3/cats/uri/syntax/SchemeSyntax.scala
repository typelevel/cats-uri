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

  private def schemeExpr(sc: Expr[StringContext], args: Expr[Seq[Any]])(
      using q: Quotes): Expr[Scheme] =
    sc.value match {
      case Some(sc) if sc.parts.size == 1 =>
        val value: String = sc.parts.head
        Scheme
          .fromString(value)
          .fold(
            e => {
              quotes.reflect.report.throwError(e)
            },
            _ => '{ Scheme.unsafeFromString(${ Expr(value) }) }
          )
      case Some(_) =>
        quotes.reflect.report.throwError("StringContext must be a single string literal")
      case None =>
        quotes.reflect.report.throwError("StringContext args must be statically known")
    }

  inline def literal(inline sc: StringContext, inline args: Any*): Scheme =
    ${ schemeExpr('sc, 'args) }
}
