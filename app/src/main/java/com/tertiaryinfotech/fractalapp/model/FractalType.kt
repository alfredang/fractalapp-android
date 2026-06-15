package com.tertiaryinfotech.fractalapp.model

/**
 * The family of escape-time fractal to render. The [value] is passed straight to
 * the GLSL fragment shader, so the ordering must match `iterate()` in the shader.
 * Android port of the iOS `FractalType`.
 */
enum class FractalType(val value: Int, val title: String, val blurb: String) {
    MANDELBROT(0, "Mandelbrot", "The classic set — endless detail at every zoom."),
    JULIA(1, "Julia", "Filaments and spirals shaped by a seed point."),
    BURNING_SHIP(2, "Burning Ship", "Jagged, ship-like structures from absolute values."),
    TRICORN(3, "Tricorn", "The mirrored ‘Mandelbar’ with three-fold symmetry."),
    PHOENIX(4, "Phoenix", "Swirling tendrils with a feedback term."),
    CELTIC(5, "Celtic", "Heart-shaped lobes from a folded Mandelbrot.");

    /** Fractals that read the seed point `c` as a free parameter. */
    val usesSeedPoint: Boolean get() = this == JULIA || this == PHOENIX

    companion object {
        fun from(value: Int): FractalType = entries.firstOrNull { it.value == value } ?: MANDELBROT
    }
}
