package com.tertiaryinfotech.fractalapp.model

import androidx.compose.ui.graphics.Color

/**
 * Named colour schemes. The [value] selects the palette branch in the shader
 * (`paletteColor()`), so keep the ordering in sync. Android port of `ColorPalette`.
 */
enum class ColorPalette(val value: Int, val title: String, val swatch: List<Color>) {
    AURORA(0, "Aurora", listOf(Color(0xFF06103A), Color(0xFF1ACCB3), Color(0xFF99F280))),
    INFERNO(1, "Inferno", listOf(Color(0xFF00000D), Color(0xFFBF1F4D), Color(0xFFFFE666))),
    OCEAN(2, "Ocean", listOf(Color(0xFF030D26), Color(0xFF0073BF), Color(0xFF8CF2F2))),
    NEON(3, "Neon", listOf(Color(0xFF0D0026), Color(0xFFF21AD9), Color(0xFF33F2F2))),
    SUNSET(4, "Sunset", listOf(Color(0xFF1A0533), Color(0xFFF25940), Color(0xFFFFD973))),
    VIRIDIS(5, "Viridis", listOf(Color(0xFF450054), Color(0xFF21918C), Color(0xFFFCE824))),
    CANDY(6, "Candy", listOf(Color(0xFF260D40), Color(0xFFF273BF), Color(0xFF8CD9FF))),
    MONO(7, "Mono", listOf(Color(0xFF000000), Color(0xFF808080), Color(0xFFFFFFFF)));

    companion object {
        fun from(value: Int): ColorPalette = entries.firstOrNull { it.value == value } ?: AURORA
    }
}
