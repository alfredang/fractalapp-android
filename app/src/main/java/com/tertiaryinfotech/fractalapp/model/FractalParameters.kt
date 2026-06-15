package com.tertiaryinfotech.fractalapp.model

/**
 * The complete description of a single fractal image — everything the renderer
 * needs. A value can be saved, restored, shared, or derived from a text seed.
 * Android port of the iOS `FractalParameters`.
 */
data class FractalParameters(
    val type: FractalType = FractalType.MANDELBROT,
    val palette: ColorPalette = ColorPalette.AURORA,
    // View centre in the complex plane.
    val centerX: Double = -0.5,
    val centerY: Double = 0.0,
    // Half-height of the view in complex-plane units. Smaller = deeper zoom.
    val scale: Double = 1.3,
    // Seed point `c` for Julia / Phoenix families.
    val seedX: Double = -0.8,
    val seedY: Double = 0.156,
    // Detail / escape iterations.
    val iterations: Int = 220,
    // Colouring controls.
    val colorShift: Double = 0.0,     // 0…1 hue offset around the palette
    val colorDensity: Double = 1.0,   // 0.2…4 banding frequency
    val interior: Double = 0.0,       // 0…1 brightness of the trapped interior
) {
    companion object {
        val ZOOM_MIN = 0.0000005
        val ZOOM_MAX = 3.0
        const val MAX_SHADER_ITERATIONS = 700
    }
}
