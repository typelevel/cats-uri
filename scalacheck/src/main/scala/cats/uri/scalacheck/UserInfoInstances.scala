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

package cats.uri.scalacheck

import cats.uri._
import org.scalacheck._
import cats.uri.scalacheck.UserScalacheckInstances._
import cats.uri.scalacheck.PasswordScalacheckInstances._

/**
 * Scalacheck instances for [[UserInfo]].
 */
private[scalacheck] trait UserInfoScalacheckInstances {

  implicit final val arbUserInfo: Arbitrary[UserInfo] = {
    val genOnlyUser: Gen[UserInfo] =
      for {
        u <- Arbitrary.arbitrary[User]
        hasColonDelimiter <- Arbitrary.arbitrary[Boolean]
      } yield UserInfo(u, hasColonDelimiter)

    val genUserAndPassword: Gen[UserInfo] =
      for {
        u <- Arbitrary.arbitrary[User]
        p <- Arbitrary.arbitrary[Password]
      } yield UserInfo(u, p)

    Arbitrary(
      Gen.oneOf(
        Gen.const(UserInfo.OnlyColonDelimiter),
        Arbitrary.arbitrary[Password].map(UserInfo.apply),
        genOnlyUser,
        genUserAndPassword
      )
    )
  }

  implicit final val cogenUserInfo: Cogen[UserInfo] =
    Cogen[(Option[User], Option[Password], Boolean)].contramap(value =>
      (value.user, value.password, value.hasColonDelimiter))
}
