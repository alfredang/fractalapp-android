package com.tertiaryinfotech.fractalapp.gl

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.tertiaryinfotech.fractalapp.model.FractalParameters
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.max

/**
 * Renders [FractalParameters] with an OpenGL ES 2.0 fragment shader onto a fullscreen
 * triangle, and also renders off-screen at an arbitrary resolution into a [Bitmap] for
 * export. Android counterpart of the iOS `FractalRenderer`.
 */
class FractalRenderer : GLSurfaceView.Renderer {

    /** Mutated from the UI thread, read on the GL thread each frame. */
    @Volatile
    var parameters: FractalParameters = FractalParameters()

    private var program = 0
    private var aPos = 0
    private val uniforms = HashMap<String, Int>()
    private var surfaceWidth = 1
    private var surfaceHeight = 1

    private val triangle: FloatBuffer = ByteBuffer
        .allocateDirect(6 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
            put(floatArrayOf(-1f, -1f, 3f, -1f, -1f, 3f))
            position(0)
        }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        program = buildProgram(FractalShaders.VERTEX, FractalShaders.FRAGMENT)
        aPos = GLES20.glGetAttribLocation(program, "aPos")
        for (name in UNIFORM_NAMES) {
            uniforms[name] = GLES20.glGetUniformLocation(program, name)
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        surfaceWidth = max(1, width)
        surfaceHeight = max(1, height)
        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        drawFractal(surfaceWidth.toFloat() / surfaceHeight.toFloat())
    }

    private fun drawFractal(aspect: Float) {
        GLES20.glUseProgram(program)
        GLES20.glVertexAttribPointer(aPos, 2, GLES20.GL_FLOAT, false, 0, triangle)
        GLES20.glEnableVertexAttribArray(aPos)

        val p = parameters
        GLES20.glUniform2f(loc("uCenter"), p.centerX.toFloat(), p.centerY.toFloat())
        GLES20.glUniform2f(loc("uSeed"), p.seedX.toFloat(), p.seedY.toFloat())
        GLES20.glUniform1f(loc("uScale"), p.scale.toFloat())
        GLES20.glUniform1f(loc("uAspect"), aspect)
        GLES20.glUniform1i(loc("uMaxIter"), p.iterations.coerceIn(20, FractalParameters.MAX_SHADER_ITERATIONS))
        GLES20.glUniform1i(loc("uType"), p.type.value)
        GLES20.glUniform1i(loc("uPalette"), p.palette.value)
        GLES20.glUniform1f(loc("uColorShift"), p.colorShift.toFloat())
        GLES20.glUniform1f(loc("uColorDensity"), max(0.05, p.colorDensity).toFloat())
        GLES20.glUniform1f(loc("uInterior"), p.interior.toFloat())

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
        GLES20.glDisableVertexAttribArray(aPos)
    }

    /**
     * Render the current parameters off-screen at [width]×[height] and return a Bitmap.
     * MUST be called on the GL thread (use [GLSurfaceView.queueEvent]).
     */
    fun renderToBitmap(width: Int, height: Int): Bitmap? {
        if (program == 0) return null
        val fbo = IntArray(1)
        val tex = IntArray(1)
        GLES20.glGenFramebuffers(1, fbo, 0)
        GLES20.glGenTextures(1, tex, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0])
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0])
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D, tex[0], 0)

        var bitmap: Bitmap? = null
        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) == GLES20.GL_FRAMEBUFFER_COMPLETE) {
            GLES20.glViewport(0, 0, width, height)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
            drawFractal(width.toFloat() / height.toFloat())

            val buf = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder())
            GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf)
            buf.rewind()
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bmp.copyPixelsFromBuffer(buf)
            // GL origin is bottom-left; flip vertically for a screen-oriented image.
            bitmap = flipVertical(bmp)
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glDeleteFramebuffers(1, fbo, 0)
        GLES20.glDeleteTextures(1, tex, 0)
        GLES20.glViewport(0, 0, surfaceWidth, surfaceHeight)
        return bitmap
    }

    private fun flipVertical(src: Bitmap): Bitmap {
        val m = android.graphics.Matrix().apply { preScale(1f, -1f) }
        val out = Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, false)
        if (out != src) src.recycle()
        return out
    }

    private fun loc(name: String) = uniforms[name] ?: -1

    private fun buildProgram(vertexSrc: String, fragmentSrc: String): Int {
        val vs = compile(GLES20.GL_VERTEX_SHADER, vertexSrc)
        val fs = compile(GLES20.GL_FRAGMENT_SHADER, fragmentSrc)
        val prog = GLES20.glCreateProgram()
        GLES20.glAttachShader(prog, vs)
        GLES20.glAttachShader(prog, fs)
        GLES20.glLinkProgram(prog)
        val status = IntArray(1)
        GLES20.glGetProgramiv(prog, GLES20.GL_LINK_STATUS, status, 0)
        check(status[0] != 0) { "Program link failed: " + GLES20.glGetProgramInfoLog(prog) }
        GLES20.glDeleteShader(vs)
        GLES20.glDeleteShader(fs)
        return prog
    }

    private fun compile(type: Int, src: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, src)
        GLES20.glCompileShader(shader)
        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        check(status[0] != 0) { "Shader compile failed: " + GLES20.glGetShaderInfoLog(shader) }
        return shader
    }

    private companion object {
        val UNIFORM_NAMES = listOf(
            "uCenter", "uSeed", "uScale", "uAspect", "uMaxIter",
            "uType", "uPalette", "uColorShift", "uColorDensity", "uInterior",
        )
    }
}
