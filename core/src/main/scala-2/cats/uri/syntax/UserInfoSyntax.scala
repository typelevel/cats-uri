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

private[syntax] trait UserInfoSyntax {
  implicit class UserInfoContext(val sc: StringContext) {
    def userInfoEncoded(args: Any*): UserInfo = macro UserInfoSyntax.userInfoEncoded.make
  }
}

private object UserInfoSyntax {

  private object userInfoEncoded extends Literally[UserInfo] {
    def validate(c: Context)(s: String): Either[String, c.Expr[UserInfo]] = {
      import c.universe._

      UserInfo.fromPercentEncodedString(s) match {
        case Left(e) => Left(e.getLocalizedMessage)
        case _ =>
          Right(c.Expr(q"UserInfo.unsafeFromPercentEncodedString($s)"))
      }
    }

    def make(c: Context)(args: c.Expr[Any]*): c.Expr[UserInfo] =
      apply(c)(args: _*)
  }
}
