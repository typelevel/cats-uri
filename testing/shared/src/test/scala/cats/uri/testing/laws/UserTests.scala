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

package cats.uri.testing.laws

import cats.kernel.laws.discipline._
import cats.uri._
import cats.uri.laws.discipline._
import cats.uri.scalacheck.user._
import munit._

final class UserTests extends DisciplineSuite {
  checkAll("Hash[User]", HashTests[User].hash)
  checkAll("Order[User]", OrderTests[User].order)
  checkAll("PercentCodec[User]", PercentCodecTests[User].percentCodec)
}
