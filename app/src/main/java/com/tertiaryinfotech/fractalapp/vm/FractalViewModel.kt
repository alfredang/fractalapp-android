package com.tertiaryinfotech.fractalapp.vm

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.tertiaryinfotech.fractalapp.model.FractalParameters
import com.tertiaryinfotech.fractalapp.model.FractalPreset
import com.tertiaryinfotech.fractalapp.seed.SeedGenerator

/**
 * Holds the live fractal description and UI state. Rendering/capture run in the
 * GL view; the screen wires [captureProvider] so the VM can request export bitmaps.
 * Android counterpart of the iOS `FractalViewModel`.
 */
class FractalViewModel : ViewModel() {

    var parameters by mutableStateOf(
        FractalPreset.all.first { it.name == "Electric Julia" }.parameters
    )
        private set

    var seedText by mutableStateOf("")
    var toast by mutableStateOf<String?>(null)
        private set

    /** Set by the screen: render an N×N bitmap and deliver it on the main thread. */
    var captureProvider: ((Int, (Bitmap?) -> Unit) -> Unit)? = null

    private val randomWords = listOf(
        "nebula", "ember", "tide", "lotus", "quartz", "zephyr", "coral",
        "mirage", "cobalt", "saffron", "glacier", "nova", "thorn", "halcyon"
    )

    fun onParametersChanged(p: FractalParameters) { parameters = p }

    fun applySeed() {
        val seed = seedText.trim()
        if (seed.isEmpty()) return
        parameters = SeedGenerator.parameters(seed)
        showToast("Generated from “$seed”")
    }

    fun randomize() {
        val pick = randomWords.random() + (0..9999).random()
        seedText = pick
        parameters = SeedGenerator.parameters(pick)
    }

    fun apply(preset: FractalPreset) {
        parameters = preset.parameters
        seedText = preset.name
    }

    /** Screenshot/demo hook: open directly on a named preset (launch with `--es demo "<name>"`). */
    fun applyByName(name: String) {
        FractalPreset.all.firstOrNull { it.name == name }?.let { apply(it) }
    }

    fun resetView() {
        parameters = parameters.copy(
            centerX = if (parameters.type.usesSeedPoint) 0.0 else -0.5,
            centerY = 0.0,
            scale = if (parameters.type.usesSeedPoint) 1.5 else 1.3,
        )
    }

    // Parameter editors used by the controls sheet.
    fun update(transform: (FractalParameters) -> FractalParameters) { parameters = transform(parameters) }

    fun saveToPhotos(onResult: (Bitmap?) -> Unit) {
        val provider = captureProvider ?: run { showToast("Renderer not ready"); return }
        provider(EXPORT_SIZE, onResult)
    }

    fun prepareShare(onResult: (Bitmap?) -> Unit) {
        val provider = captureProvider ?: run { showToast("Renderer not ready"); return }
        provider(EXPORT_SIZE, onResult)
    }

    fun showToast(message: String) { toast = message }
    fun clearToast() { toast = null }

    companion object {
        const val EXPORT_SIZE = 2048
    }
}
