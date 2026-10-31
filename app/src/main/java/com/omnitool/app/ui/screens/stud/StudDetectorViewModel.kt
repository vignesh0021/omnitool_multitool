package com.omnitool.app.ui.screens.stud

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.omnitool.app.utils.AudioHapticFeedback
import com.omnitool.app.utils.SensorFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.sqrt

enum class StudSensitivity(val thresholdDelta: Float, val label: String) {
    LOW(40f, "Low (Deep Metal / Pipes)"),
    MEDIUM(25f, "Medium (Standard Wall Screws)"),
    HIGH(12f, "High (Sensitive Wiring & Nails)")
}

enum class DetectionStatus(val label: String) {
    SAFE("CLEAR — NO METAL DETECTED"),
    CAUTION("APPROACHING METAL / EMF FIELD"),
    ALERT("⚠️ ALERT: METAL / LIVE WIRE LOCATED!")
}

data class StudDetectorState(
    val currentMagnitude: Float = 45f,
    val baseline: Float = 45f,
    val delta: Float = 0f,
    val status: DetectionStatus = DetectionStatus.SAFE,
    val sensitivity: StudSensitivity = StudSensitivity.MEDIUM,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val estimatedDepthCm: Float? = null,
    val hasMagneticSensor: Boolean = true
)

class StudDetectorViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val magneticSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val audioHaptics = AudioHapticFeedback(application)
    private val filter = SensorFilter(0.25f)

    private val _uiState = MutableStateFlow(
        StudDetectorState(hasMagneticSensor = magneticSensor != null)
    )
    val uiState: StateFlow<StudDetectorState> = _uiState.asStateFlow()

    private var initialBaselineCalibrated = false

    fun startListening() {
        magneticSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    fun setSensitivity(sensitivity: StudSensitivity) {
        _uiState.update { it.copy(sensitivity = sensitivity) }
    }

    fun toggleSound() {
        _uiState.update { it.copy(soundEnabled = !it.soundEnabled) }
    }

    fun toggleVibration() {
        _uiState.update { it.copy(vibrationEnabled = !it.vibrationEnabled) }
    }

    fun calibrateBaseline() {
        val current = _uiState.value.currentMagnitude
        _uiState.update {
            it.copy(
                baseline = current,
                delta = 0f,
                status = DetectionStatus.SAFE,
                estimatedDepthCm = null
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_MAGNETIC_FIELD) return

        val rawMag = sqrt(
            (event.values[0] * event.values[0] +
             event.values[1] * event.values[1] +
             event.values[2] * event.values[2]).toDouble()
        ).toFloat()

        val smoothedMag = filter.filter(rawMag)

        if (!initialBaselineCalibrated && smoothedMag > 5f) {
            initialBaselineCalibrated = true
            _uiState.update { it.copy(baseline = smoothedMag) }
        }

        val baseline = _uiState.value.baseline
        val delta = (smoothedMag - baseline).coerceAtLeast(0f)
        val sensitivity = _uiState.value.sensitivity

        val status = when {
            delta >= sensitivity.thresholdDelta -> DetectionStatus.ALERT
            delta >= sensitivity.thresholdDelta * 0.45f -> DetectionStatus.CAUTION
            else -> DetectionStatus.SAFE
        }

        // Estimated depth inverse mapping: large delta = closer to surface
        val estimatedDepth = if (delta > 4f) {
            (12f - (delta / 8f)).coerceIn(1f, 15f)
        } else null

        val intensityRatio = (delta / (sensitivity.thresholdDelta * 1.5f)).coerceIn(0f, 1f)

        if (_uiState.value.soundEnabled && intensityRatio > 0.25f) {
            audioHaptics.playProximityBeep(intensityRatio)
        }

        if (_uiState.value.vibrationEnabled && intensityRatio > 0.45f) {
            audioHaptics.vibrateProximity(intensityRatio)
        }

        _uiState.update {
            it.copy(
                currentMagnitude = smoothedMag,
                delta = delta,
                status = status,
                estimatedDepthCm = estimatedDepth
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        stopListening()
        audioHaptics.release()
    }
}
