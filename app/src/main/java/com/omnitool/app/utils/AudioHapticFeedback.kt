package com.omnitool.app.utils

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Audio and Haptic feedback helper for stud detection beeps and level alignment cues.
 */
class AudioHapticFeedback(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private var lastToneTime = 0L
    private var lastVibrateTime = 0L

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    /**
     * Play a beep corresponding to metal/EMF signal proximity.
     * Higher signal = higher frequency beep interval.
     */
    fun playProximityBeep(signalIntensityRatio: Float) {
        val now = System.currentTimeMillis()
        val intervalMs = (400 - (signalIntensityRatio * 320)).toLong().coerceIn(60L, 500L)

        if (now - lastToneTime >= intervalMs) {
            lastToneTime = now
            val toneType = if (signalIntensityRatio > 0.75f) {
                ToneGenerator.TONE_CDMA_HIGH_L
            } else {
                ToneGenerator.TONE_PROP_BEEP
            }
            try {
                toneGenerator?.startTone(toneType, 40)
            } catch (_: Exception) {}
        }
    }

    /**
     * Play a pleasant chime tone when bubble level is locked.
     */
    fun playLevelLockedTone() {
        val now = System.currentTimeMillis()
        if (now - lastToneTime > 500) {
            lastToneTime = now
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 120)
            } catch (_: Exception) {}
        }
    }

    /**
     * Vibrate for target alignment (short crisp click or warning buzz).
     */
    fun vibrateLevelAligned() {
        val now = System.currentTimeMillis()
        if (now - lastVibrateTime > 600) {
            lastVibrateTime = now
            vibrate(50, 200)
        }
    }

    /**
     * Vibrate according to metal proximity.
     */
    fun vibrateProximity(signalIntensityRatio: Float) {
        val now = System.currentTimeMillis()
        val intervalMs = (500 - (signalIntensityRatio * 420)).toLong().coerceIn(80L, 600L)
        if (now - lastVibrateTime >= intervalMs) {
            lastVibrateTime = now
            val duration = (signalIntensityRatio * 40).toLong().coerceIn(15L, 50L)
            val amplitude = (signalIntensityRatio * 255).toInt().coerceIn(50, 255)
            vibrate(duration, amplitude)
        }
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
