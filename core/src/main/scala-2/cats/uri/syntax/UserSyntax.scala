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
import org.typelevel.literally.Literally

private[syntax] trait UserSyntax {
  implicit class UserContext(val sc: StringContext) {
    def user(args: Any*): User = macro UserSyntax.user.make

    def userEncoded(args: Any*): User = macro UserSyntax.userEncoded.make
  }
}

private object UserSyntax {

  private object user extends Literally[User] {
    def validate(c: Context)(s: String): Either[String, c.Expr[User]] = {
      import c.universe._

      User.fromString(s).map(_ => c.Expr(q"User.unsafeFromString($s)"))
    }

    def make(c: Context)(args: c.Expr[Any]*): c.Expr[User] =
      apply(c)(args: _*)
  }

  private object userEncoded extends Literally[User] {
    def validate(c: Context)(s: String): Either[String, c.Expr[User]] = {
      import c.universe._

      User.parseFromPercentEncodedString(s) match {
        case Left(e) => Left(e.getLocalizedMessage)
        case _ =>
          Right(c.Expr(q"User.unsafeParseFromPercentEncodedString($s)"))
      }
    }

    def make(c: Context)(args: c.Expr[Any]*): c.Expr[User] =
      apply(c)(args: _*)
  }
}
