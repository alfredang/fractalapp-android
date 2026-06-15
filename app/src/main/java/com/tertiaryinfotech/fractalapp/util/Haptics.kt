package com.tertiaryinfotech.fractalapp.util

import android.view.HapticFeedbackConstants
import android.view.View

/** Tiny haptic helper mirroring the iOS light/success taps. */
object Haptics {
    fun tap(view: View?) {
        view?.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }
}
