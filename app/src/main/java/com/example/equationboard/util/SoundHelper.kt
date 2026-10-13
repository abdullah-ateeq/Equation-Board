package com.example.equationboard.util

import android.media.AudioManager
import android.media.ToneGenerator

object SoundHelper {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    fun playTileSelect(enabled: Boolean) {
        if (!enabled) return
        playTone(ToneGenerator.TONE_PROP_BEEP, 30)
    }

    fun playOperationSelect(enabled: Boolean) {
        if (!enabled) return
        playTone(ToneGenerator.TONE_PROP_ACK, 40)
    }

    fun playCorrect(enabled: Boolean) {
        if (!enabled) return
        playTone(ToneGenerator.TONE_PROP_PROMPT, 120)
    }

    fun playIncorrect(enabled: Boolean) {
        if (!enabled) return
        playTone(ToneGenerator.TONE_PROP_NACK, 100)
    }

    fun playTileRemoval(enabled: Boolean) {
        if (!enabled) return
        playTone(ToneGenerator.TONE_PROP_BEEP2, 50)
    }

    fun playNewTarget(enabled: Boolean) {
        if (!enabled) return
        playTone(ToneGenerator.TONE_PROP_PROMPT, 80)
    }

    fun playSkip(enabled: Boolean) {
        if (!enabled) return
        playTone(ToneGenerator.TONE_PROP_BEEP, 60)
    }

    fun playHint(enabled: Boolean) {
        if (!enabled) return
        playTone(ToneGenerator.TONE_PROP_ACK, 70)
    }

    fun playGameComplete(enabled: Boolean) {
        if (!enabled) return
        playTone(ToneGenerator.TONE_PROP_PROMPT, 250)
    }

    private fun playTone(toneType: Int, durationMs: Int) {
        try {
            toneGenerator?.startTone(toneType, durationMs)
        } catch (_: Exception) {
            // Ignore sound errors
        }
    }
}
