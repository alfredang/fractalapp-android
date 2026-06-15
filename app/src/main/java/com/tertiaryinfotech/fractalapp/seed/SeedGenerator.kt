package com.tertiaryinfotech.fractalapp.seed

import com.tertiaryinfotech.fractalapp.model.ColorPalette
import com.tertiaryinfotech.fractalapp.model.FractalParameters
import com.tertiaryinfotech.fractalapp.model.FractalType

/**
 * Turns a text/number *seed* into a fully-formed [FractalParameters], deterministically:
 * the same seed always yields the same artwork. Uses the same FNV-1a + splitmix64
 * approach as the iOS app so seeds behave consistently across the family.
 */
object SeedGenerator {

    /** Deterministic 64-bit FNV-1a hash of a string (stable across launches/processes). */
    fun hash(s: String): Long {
        var h = -0x340d631b7bdddcdbL // 0xcbf29ce484222325
        for (b in s.lowercase().toByteArray(Charsets.UTF_8)) {
            h = h xor (b.toLong() and 0xff)
            h *= 0x100000001b3L
        }
        return h
    }

    /** Splitmix64 PRNG seeded from the hash. */
    private class Splitmix(var state: Long) {
        fun next(): Long {
            state += -0x61c8864680b583ebL // 0x9E3779B97F4A7C15
            var z = state
            z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L // 0xBF58476D1CE4E5B9
            z = (z xor (z ushr 27)) * -0x6b2fb644ecceee15L // 0x94D049BB133111EB
            return z xor (z ushr 31)
        }
        fun unit(): Double = (next() ushr 11).toDouble() * (1.0 / 9007199254740992.0)
        fun range(lo: Double, hi: Double): Double = lo + unit() * (hi - lo)
    }

    /** Build parameters from a free-text seed. */
    fun parameters(seed: String): FractalParameters {
        val trimmed = seed.trim()
        val rng = Splitmix(hash(if (trimmed.isEmpty()) "fractal" else trimmed))

        fun pick(count: Int): Int = ((rng.next() ushr 1) % count).toInt()
        val type = FractalType.entries[pick(FractalType.entries.size)]
        val palette = ColorPalette.entries[pick(ColorPalette.entries.size)]

        return if (type.usesSeedPoint) {
            FractalParameters(
                type = type, palette = palette,
                seedX = rng.range(-0.9, 0.4), seedY = rng.range(-0.9, 0.9),
                centerX = 0.0, centerY = 0.0, scale = 1.5,
                iterations = rng.range(160.0, 420.0).toInt(),
                colorShift = rng.unit(), colorDensity = rng.range(0.5, 2.4),
                interior = if (rng.unit() < 0.3) rng.range(0.2, 0.7) else 0.0,
            )
        } else {
            FractalParameters(
                type = type, palette = palette,
                centerX = rng.range(-1.4, 0.4), centerY = rng.range(-1.1, 1.1),
                scale = rng.range(0.04, 1.3),
                iterations = rng.range(160.0, 420.0).toInt(),
                colorShift = rng.unit(), colorDensity = rng.range(0.5, 2.4),
                interior = if (rng.unit() < 0.3) rng.range(0.2, 0.7) else 0.0,
            )
        }
    }
}
