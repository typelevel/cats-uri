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

/**
 * Scalacheck instances for [[User]].
 */
private[scalacheck] trait UserScalacheckInstances {

  implicit final val arbUser: Arbitrary[User] =
    Arbitrary(NonEmptyStringGen.genNonEmptyString.map(User.unsafeFromString))

  implicit final val cogenUser: Cogen[User] =
    Cogen[String].contramap(_.value)
}

private[scalacheck] object UserScalacheckInstances extends UserScalacheckInstances
