package com.tertiaryinfotech.fractalapp.gl

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import com.tertiaryinfotech.fractalapp.model.FractalParameters
import kotlin.math.max

/**
 * A [GLSurfaceView] that renders fractals and turns pinch / drag / double-tap into
 * zoom & pan, feeding the updated parameters back via [onParametersChanged].
 * Android counterpart of the iOS `MetalFractalView`.
 */
class FractalGLSurfaceView(context: Context) : GLSurfaceView(context) {

    val renderer = FractalRenderer()
    var onParametersChanged: ((FractalParameters) -> Unit)? = null

    private var params = FractalParameters()

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val proposed = params.scale / detector.scaleFactor
            update(params.copy(scale = proposed.coerceIn(FractalParameters.ZOOM_MIN, FractalParameters.ZOOM_MAX)))
            return true
        }
    })

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            if (scaleDetector.isInProgress) return false
            val h = height.takeIf { it > 0 } ?: return false
            val k = 2.0 * params.scale / h            // complex units per pixel (same for x & y)
            update(params.copy(
                centerX = params.centerX + distanceX * k,
                centerY = params.centerY - distanceY * k,
            ))
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            val w = width.takeIf { it > 0 } ?: return false
            val h = height.takeIf { it > 0 } ?: return false
            val aspect = w.toDouble() / h
            val uvx = e.x / w * 2 - 1
            val uvy = 1 - e.y / h * 2
            val cx = params.centerX + uvx * aspect * params.scale
            val cy = params.centerY + uvy * params.scale
            val newScale = max(params.scale / 2, FractalParameters.ZOOM_MIN)
            update(params.copy(centerX = cx, centerY = cy, scale = newScale))
            performClick()
            return true
        }
    })

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        preserveEGLContextOnPause = true
    }

    /** Push externally-changed parameters (seed, presets, sliders) into the view. */
    fun setParameters(p: FractalParameters) {
        params = p
        renderer.parameters = p
        requestRender()
    }

    /** Render the current fractal off-screen at [size]×[size] and deliver the Bitmap on the main thread. */
    fun capture(size: Int, onResult: (android.graphics.Bitmap?) -> Unit) {
        queueEvent {
            val bmp = renderer.renderToBitmap(size, size)
            post { onResult(bmp) }
        }
    }

    private fun update(p: FractalParameters) {
        params = p
        renderer.parameters = p
        requestRender()
        onParametersChanged?.invoke(p)
    }

    @Suppress("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
