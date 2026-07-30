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

import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL45.glActiveTexture
import org.lwjgl.opengl.GL45.glBindBuffer
import org.lwjgl.opengl.GL45.glBindSampler
import org.lwjgl.opengl.GL45.glBindTexture
import org.lwjgl.opengl.GL45.glBindVertexArray
import org.lwjgl.opengl.GL45.glBlendEquationSeparate
import org.lwjgl.opengl.GL45.glBlendFuncSeparate
import org.lwjgl.opengl.GL45.glDepthMask
import org.lwjgl.opengl.GL45.glDisable
import org.lwjgl.opengl.GL45.glEnable
import org.lwjgl.opengl.GL45.glGetBoolean
import org.lwjgl.opengl.GL45.glGetIntegerv
import org.lwjgl.opengl.GL45.glIsEnabled
import org.lwjgl.opengl.GL45.glPixelStorei
import org.lwjgl.opengl.GL45.glPolygonMode
import org.lwjgl.opengl.GL45.glScissor
import org.lwjgl.opengl.GL45.glUseProgram
import org.lwjgl.opengl.GL45.glViewport
import org.lwjgl.opengl.GL45.GL_ACTIVE_TEXTURE
import org.lwjgl.opengl.GL45.GL_ARRAY_BUFFER_BINDING
import org.lwjgl.opengl.GL45.GL_BLEND
import org.lwjgl.opengl.GL45.GL_BLEND_DST_ALPHA
import org.lwjgl.opengl.GL45.GL_BLEND_DST_RGB
import org.lwjgl.opengl.GL45.GL_BLEND_EQUATION_ALPHA
import org.lwjgl.opengl.GL45.GL_BLEND_EQUATION_RGB
import org.lwjgl.opengl.GL45.GL_BLEND_SRC_ALPHA
import org.lwjgl.opengl.GL45.GL_BLEND_SRC_RGB
import org.lwjgl.opengl.GL45.GL_CULL_FACE
import org.lwjgl.opengl.GL45.GL_CURRENT_PROGRAM
import org.lwjgl.opengl.GL45.GL_DEPTH_TEST
import org.lwjgl.opengl.GL45.GL_DEPTH_WRITEMASK
import org.lwjgl.opengl.GL45.GL_FRONT_AND_BACK
import org.lwjgl.opengl.GL45.GL_PACK_ALIGNMENT
import org.lwjgl.opengl.GL45.GL_PACK_IMAGE_HEIGHT
import org.lwjgl.opengl.GL45.GL_PACK_LSB_FIRST
import org.lwjgl.opengl.GL45.GL_PACK_ROW_LENGTH
import org.lwjgl.opengl.GL45.GL_PACK_SKIP_IMAGES
import org.lwjgl.opengl.GL45.GL_PACK_SKIP_PIXELS
import org.lwjgl.opengl.GL45.GL_PACK_SKIP_ROWS
import org.lwjgl.opengl.GL45.GL_PACK_SWAP_BYTES
import org.lwjgl.opengl.GL45.GL_PIXEL_UNPACK_BUFFER
import org.lwjgl.opengl.GL45.GL_PIXEL_UNPACK_BUFFER_BINDING
import org.lwjgl.opengl.GL45.GL_POLYGON_MODE
import org.lwjgl.opengl.GL45.GL_PRIMITIVE_RESTART
import org.lwjgl.opengl.GL45.GL_SAMPLER_BINDING
import org.lwjgl.opengl.GL45.GL_SCISSOR_BOX
import org.lwjgl.opengl.GL45.GL_SCISSOR_TEST
import org.lwjgl.opengl.GL45.GL_STENCIL_TEST
import org.lwjgl.opengl.GL45.GL_TEXTURE0
import org.lwjgl.opengl.GL45.GL_TEXTURE_2D
import org.lwjgl.opengl.GL45.GL_TEXTURE_BINDING_2D
import org.lwjgl.opengl.GL45.GL_UNPACK_ALIGNMENT
import org.lwjgl.opengl.GL45.GL_UNPACK_IMAGE_HEIGHT
import org.lwjgl.opengl.GL45.GL_UNPACK_LSB_FIRST
import org.lwjgl.opengl.GL45.GL_UNPACK_ROW_LENGTH
import org.lwjgl.opengl.GL45.GL_UNPACK_SKIP_IMAGES
import org.lwjgl.opengl.GL45.GL_UNPACK_SKIP_PIXELS
import org.lwjgl.opengl.GL45.GL_UNPACK_SKIP_ROWS
import org.lwjgl.opengl.GL45.GL_UNPACK_SWAP_BYTES
import org.lwjgl.opengl.GL45.GL_VERTEX_ARRAY_BINDING
import org.lwjgl.opengl.GL45.GL_VIEWPORT

class State(private val glVersion: Int) {

  private val props = Properties()

  fun push(): State {
    with(props) {
      glGetIntegerv(GL_ACTIVE_TEXTURE, lastActiveTexture)
      glActiveTexture(GL_TEXTURE0)
      glGetIntegerv(GL_CURRENT_PROGRAM, lastProgram)
      glGetIntegerv(GL_TEXTURE_BINDING_2D, lastTexture)

      if (glVersion >= 330 || GL.getCapabilities().GL_ARB_sampler_objects) {
        glGetIntegerv(GL_SAMPLER_BINDING, lastSampler)
      }

      glGetIntegerv(GL_ARRAY_BUFFER_BINDING, lastArrayBuffer)
      glGetIntegerv(GL_VERTEX_ARRAY_BINDING, lastVertexArrayObject)

      if (glVersion >= 200) {
        glGetIntegerv(GL_POLYGON_MODE, lastPolygonMode)
      }

      glGetIntegerv(GL_VIEWPORT, lastViewport)
      glGetIntegerv(GL_SCISSOR_BOX, lastScissorBox)
      glGetIntegerv(GL_BLEND_SRC_RGB, lastBlendSrcRgb)
      glGetIntegerv(GL_BLEND_DST_RGB, lastBlendDstRgb)
      glGetIntegerv(GL_BLEND_SRC_ALPHA, lastBlendSrcAlpha)
      glGetIntegerv(GL_BLEND_DST_ALPHA, lastBlendDstAlpha)
      glGetIntegerv(GL_BLEND_EQUATION_RGB, lastBlendEquationRgb)
      glGetIntegerv(GL_BLEND_EQUATION_ALPHA, lastBlendEquationAlpha)

      lastEnableBlend = glIsEnabled(GL_BLEND)
      lastEnableCullFace = glIsEnabled(GL_CULL_FACE)
      lastEnableDepthTest = glIsEnabled(GL_DEPTH_TEST)
      lastEnableStencilTest = glIsEnabled(GL_STENCIL_TEST)
      lastEnableScissorTest = glIsEnabled(GL_SCISSOR_TEST)

      if (glVersion >= 310) {
        lastEnablePrimitiveRestart = glIsEnabled(GL_PRIMITIVE_RESTART)
      }

      lastDepthMask = glGetBoolean(GL_DEPTH_WRITEMASK)

      glGetIntegerv(GL_PIXEL_UNPACK_BUFFER_BINDING, lastPixelUnpackBufferBinding)
      glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0)

      glGetIntegerv(GL_PACK_SWAP_BYTES, lastPackSwapBytes)
      glGetIntegerv(GL_PACK_LSB_FIRST, lastPackLsbFirst)
      glGetIntegerv(GL_PACK_ROW_LENGTH, lastPackRowLength)
      glGetIntegerv(GL_PACK_SKIP_PIXELS, lastPackSkipPixels)
      glGetIntegerv(GL_PACK_SKIP_ROWS, lastPackSkipRows)
      glGetIntegerv(GL_PACK_ALIGNMENT, lastPackAlignment)

      glGetIntegerv(GL_UNPACK_SWAP_BYTES, lastUnpackSwapBytes)
      glGetIntegerv(GL_UNPACK_LSB_FIRST, lastUnpackLsbFirst)
      glGetIntegerv(GL_UNPACK_ALIGNMENT, lastUnpackAlignment)
      glGetIntegerv(GL_UNPACK_ROW_LENGTH, lastUnpackRowLength)
      glGetIntegerv(GL_UNPACK_SKIP_PIXELS, lastUnpackSkipPixels)
      glGetIntegerv(GL_UNPACK_SKIP_ROWS, lastUnpackSkipRows)

      if (glVersion >= 120) {
        glGetIntegerv(GL_PACK_IMAGE_HEIGHT, lastPackImageHeight)
        glGetIntegerv(GL_PACK_SKIP_IMAGES, lastPackSkipImages)
        glGetIntegerv(GL_UNPACK_IMAGE_HEIGHT, lastUnpackImageHeight)
        glGetIntegerv(GL_UNPACK_SKIP_IMAGES, lastUnpackSkipImages)
      }

      glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
      glPixelStorei(GL_UNPACK_ROW_LENGTH, 0)
      glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0)
      glPixelStorei(GL_UNPACK_SKIP_ROWS, 0)
    }
    return this
  }

  fun pop(): State {
    with(props) {
      glUseProgram(lastProgram[0])
      glBindTexture(GL_TEXTURE_2D, lastTexture[0])

      if (glVersion >= 330 || GL.getCapabilities().GL_ARB_sampler_objects) {
        glBindSampler(0, lastSampler[0])
      }

      glActiveTexture(lastActiveTexture[0])
      glBindVertexArray(lastVertexArrayObject[0])
      glBindBuffer(GL_ARRAY_BUFFER, lastArrayBuffer[0])
      glBlendEquationSeparate(lastBlendEquationRgb[0], lastBlendEquationAlpha[0])
      glBlendFuncSeparate(lastBlendSrcRgb[0], lastBlendDstRgb[0], lastBlendSrcAlpha[0], lastBlendDstAlpha[0])

      setEnabled(GL_BLEND, lastEnableBlend)
      setEnabled(GL_CULL_FACE, lastEnableCullFace)
      setEnabled(GL_DEPTH_TEST, lastEnableDepthTest)
      setEnabled(GL_STENCIL_TEST, lastEnableStencilTest)
      setEnabled(GL_SCISSOR_TEST, lastEnableScissorTest)

      if (glVersion >= 310) {
        setEnabled(GL_PRIMITIVE_RESTART, lastEnablePrimitiveRestart)
      }

      if (glVersion >= 200) {
        glPolygonMode(GL_FRONT_AND_BACK, lastPolygonMode[0])
      }

      glViewport(lastViewport[0], lastViewport[1], lastViewport[2], lastViewport[3])
      glScissor(lastScissorBox[0], lastScissorBox[1], lastScissorBox[2], lastScissorBox[3])

      glPixelStorei(GL_PACK_SWAP_BYTES, lastPackSwapBytes[0])
      glPixelStorei(GL_PACK_LSB_FIRST, lastPackLsbFirst[0])
      glPixelStorei(GL_PACK_ROW_LENGTH, lastPackRowLength[0])
      glPixelStorei(GL_PACK_SKIP_PIXELS, lastPackSkipPixels[0])
      glPixelStorei(GL_PACK_SKIP_ROWS, lastPackSkipRows[0])
      glPixelStorei(GL_PACK_ALIGNMENT, lastPackAlignment[0])

      glBindBuffer(GL_PIXEL_UNPACK_BUFFER, lastPixelUnpackBufferBinding[0])
      glPixelStorei(GL_UNPACK_SWAP_BYTES, lastUnpackSwapBytes[0])
      glPixelStorei(GL_UNPACK_LSB_FIRST, lastUnpackLsbFirst[0])
      glPixelStorei(GL_UNPACK_ALIGNMENT, lastUnpackAlignment[0])
      glPixelStorei(GL_UNPACK_ROW_LENGTH, lastUnpackRowLength[0])
      glPixelStorei(GL_UNPACK_SKIP_PIXELS, lastUnpackSkipPixels[0])
      glPixelStorei(GL_UNPACK_SKIP_ROWS, lastUnpackSkipRows[0])

      if (glVersion >= 120) {
        glPixelStorei(GL_PACK_IMAGE_HEIGHT, lastPackImageHeight[0])
        glPixelStorei(GL_PACK_SKIP_IMAGES, lastPackSkipImages[0])
        glPixelStorei(GL_UNPACK_IMAGE_HEIGHT, lastUnpackImageHeight[0])
        glPixelStorei(GL_UNPACK_SKIP_IMAGES, lastUnpackSkipImages[0])
      }

      glDepthMask(lastDepthMask)
    }

    return this
  }

  private fun setEnabled(cap: Int, enabled: Boolean) {
    if (enabled) glEnable(cap) else glDisable(cap)
  }

}
