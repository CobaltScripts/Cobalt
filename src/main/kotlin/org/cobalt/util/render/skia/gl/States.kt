/*
 * This file is part of https://github.com/Lyzev/Skija.
 *
 * Copyright (c) 2025. Lyzev
 *
 * Skija is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License, or
 * (at your option) any later version.
 *
 * Skija is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Skija. If not, see <https://www.gnu.org/licenses/>.
 */

package org.cobalt.util.render.skia.gl

import java.util.concurrent.CopyOnWriteArrayList
import org.lwjgl.opengl.GL30.GL_MAJOR_VERSION
import org.lwjgl.opengl.GL30.GL_MINOR_VERSION
import org.lwjgl.opengl.GL30.glGetIntegerv

object States {

  private val glVersion: Int
  private val stack = CopyOnWriteArrayList<State>()

  fun push() {
    stack += State(glVersion).push()
  }

  fun pop() {
    require(stack.isNotEmpty()) { "No state to restore." }
    stack.removeAt(stack.lastIndex).pop()
  }

  init {
    val major = IntArray(1)
    val minor = IntArray(1)
    glGetIntegerv(GL_MAJOR_VERSION, major)
    glGetIntegerv(GL_MINOR_VERSION, minor)
    glVersion = major[0] * 100 + minor[0] * 10
  }

}
