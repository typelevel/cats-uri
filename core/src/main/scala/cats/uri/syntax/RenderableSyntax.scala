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

// TODO: Maybe use simulacrum for this. I note that the generated ops object
// is deprecated all over the place in cats, but simulacrum still seems to
// generate it. I'm hand coding this until I figure out the story there. In
// any event, then number of operations here should be quite small, so hand
// coding isn't the end of the world.
private[syntax] trait RenderableSyntax {
  import RenderableSyntax._

  implicit final def renderableSyntax[A](target: A)(
      implicit tc: Renderable[A]): RenderableOps[A] =
    new RenderableOps(target)
}

private object RenderableSyntax {
  final class RenderableOps[A](private val a: A)(implicit tc: Renderable[A]) {
    def renderAsString: String =
      tc.renderAsString(a)
  }
}
