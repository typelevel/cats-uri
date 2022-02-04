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

package cats.uri

package object syntax {
  object all
      extends SchemeSyntax
      with UserSyntax
      with PasswordSyntax
      with UserInfoSyntax
      with RenderableSyntax
      with PercentEncoderSyntax
  object scheme extends SchemeSyntax
  object user extends UserSyntax
  object password extends PasswordSyntax
  object userinfo extends UserInfoSyntax
  object renderable extends RenderableSyntax
  object percentEncoder extends PercentEncoderSyntax
}
