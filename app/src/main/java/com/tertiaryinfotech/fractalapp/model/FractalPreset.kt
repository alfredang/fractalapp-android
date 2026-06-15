package com.tertiaryinfotech.fractalapp.model

/** A curated, hand-tuned starting point shown in the gallery. */
data class FractalPreset(val name: String, val parameters: FractalParameters) {
    companion object {
        val all: List<FractalPreset> = listOf(
            FractalPreset(
                "Seahorse Valley",
                FractalParameters(
                    type = FractalType.MANDELBROT, palette = ColorPalette.AURORA,
                    centerX = -0.745, centerY = 0.113, scale = 0.018,
                    iterations = 380, colorDensity = 1.6, colorShift = 0.1,
                )
            ),
            FractalPreset(
                "Electric Julia",
                FractalParameters(
                    type = FractalType.JULIA, palette = ColorPalette.NEON,
                    seedX = -0.8, seedY = 0.156, scale = 1.5,
                    iterations = 300, colorDensity = 1.3, colorShift = 0.4,
                )
            ),
            FractalPreset(
                "Burning Bow",
                FractalParameters(
                    type = FractalType.BURNING_SHIP, palette = ColorPalette.INFERNO,
                    centerX = -1.755, centerY = -0.03, scale = 0.12,
                    iterations = 300, colorDensity = 1.1,
                )
            ),
            FractalPreset(
                "Deep Ocean",
                FractalParameters(
                    type = FractalType.MANDELBROT, palette = ColorPalette.OCEAN,
                    centerX = -0.16, centerY = 1.0405, scale = 0.026,
                    iterations = 360, colorDensity = 1.8, colorShift = 0.6,
                )
            ),
            FractalPreset(
                "Candy Spiral",
                FractalParameters(
                    type = FractalType.JULIA, palette = ColorPalette.CANDY,
                    seedX = 0.285, seedY = 0.01, scale = 1.4,
                    iterations = 260, colorDensity = 1.5, colorShift = 0.2,
                )
            ),
            FractalPreset(
                "Phoenix Bloom",
                FractalParameters(
                    type = FractalType.PHOENIX, palette = ColorPalette.SUNSET,
                    seedX = 0.5667, seedY = -0.5, scale = 1.6,
                    iterations = 240, colorDensity = 1.2, colorShift = 0.5,
                )
            ),
            FractalPreset(
                "Tricorn Star",
                FractalParameters(
                    type = FractalType.TRICORN, palette = ColorPalette.VIRIDIS,
                    centerX = 0.0, centerY = 0.0, scale = 1.6,
                    iterations = 220, colorDensity = 1.4,
                )
            ),
            FractalPreset(
                "Celtic Heart",
                FractalParameters(
                    type = FractalType.CELTIC, palette = ColorPalette.CANDY,
                    centerX = -0.6, centerY = 0.0, scale = 1.3,
                    iterations = 260, colorDensity = 1.3, colorShift = 0.7,
                )
            ),
        )
    }
}
