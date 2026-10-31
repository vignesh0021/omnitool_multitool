package com.omnitool.app.ui.screens.level

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import com.omnitool.app.data.models.*
import com.omnitool.app.utils.AudioHapticFeedback
import com.omnitool.app.utils.SensorFilter
import com.omnitool.app.utils.TrigonometryUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs

data class BubbleLevelState(
    val pitch: Float = 0f,
    val roll: Float = 0f,
    val totalDeviation: Float = 0f,
    val isLevel: Boolean = false,
    val isFrozen: Boolean = false,
    val settings: LevelSettings = LevelSettings(),
    val hasAccelerometer: Boolean = true
)

class BubbleLevelViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val audioHaptics = AudioHapticFeedback(application)

    private val pitchFilter = SensorFilter(0.15f)
    private val rollFilter = SensorFilter(0.15f)

    private val _uiState = MutableStateFlow(
        BubbleLevelState(hasAccelerometer = accelerometer != null)
    )
    val uiState: StateFlow<BubbleLevelState> = _uiState.asStateFlow()

    private var wasLevelLastFrame = false

    fun startListening() {
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    fun setMode(mode: LevelOrientationMode) {
        _uiState.update { it.copy(settings = it.settings.copy(mode = mode)) }
    }

    fun setViscosity(viscosity: ViscositySetting) {
        pitchFilter.setAlpha(viscosity.alpha)
        rollFilter.setAlpha(viscosity.alpha)
        _uiState.update { it.copy(settings = it.settings.copy(viscosity = viscosity)) }
    }

    fun setScale(scale: PrecisionScale) {
        _uiState.update { it.copy(settings = it.settings.copy(scale = scale)) }
    }

    fun setAcceptableDeviation(deviation: Float) {
        _uiState.update { it.copy(settings = it.settings.copy(acceptableDeviation = deviation)) }
    }

    fun toggleFreeze() {
        _uiState.update { it.copy(isFrozen = !it.isFrozen) }
    }

    fun toggleSound() {
        _uiState.update { it.copy(settings = it.settings.copy(soundEnabled = !it.settings.soundEnabled)) }
    }

    fun toggleVibration() {
        _uiState.update { it.copy(settings = it.settings.copy(vibrationEnabled = !it.settings.vibrationEnabled)) }
    }

    fun toggleKeepScreenOn() {
        _uiState.update { it.copy(settings = it.settings.copy(keepScreenOn = !it.settings.keepScreenOn)) }
    }

    fun calibrateZero() {
        val currentPitch = _uiState.value.pitch + _uiState.value.settings.calibrationOffsetX
        val currentRoll = _uiState.value.roll + _uiState.value.settings.calibrationOffsetY
        _uiState.update {
            it.copy(
                settings = it.settings.copy(
                    calibrationOffsetX = currentPitch,
                    calibrationOffsetY = currentRoll
                )
            )
        }
    }

    fun resetCalibration() {
        _uiState.update {
            it.copy(
                settings = it.settings.copy(
                    calibrationOffsetX = 0f,
                    calibrationOffsetY = 0f
                )
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        if (_uiState.value.isFrozen) return

        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]

        val rawPitch = TrigonometryUtils.calculatePitch(ax, ay, az)
        val rawRoll = TrigonometryUtils.calculateRoll(ax, ay, az)

        val smoothedPitch = pitchFilter.filter(rawPitch)
        val smoothedRoll = rollFilter.filter(rawRoll)

        val settings = _uiState.value.settings
        val calibratedPitch = smoothedPitch - settings.calibrationOffsetX
        val calibratedRoll = smoothedRoll - settings.calibrationOffsetY

        val activeDeviation = when (settings.mode) {
            LevelOrientationMode.HORIZONTAL_X -> abs(calibratedPitch)
            LevelOrientationMode.VERTICAL_Y -> abs(calibratedRoll)
            LevelOrientationMode.BULLSEYE_XY -> TrigonometryUtils.calculateBullseyeDeviation(calibratedPitch, calibratedRoll)
        }

        val isLevel = activeDeviation <= settings.acceptableDeviation

        if (isLevel && !wasLevelLastFrame) {
            if (settings.soundEnabled) audioHaptics.playLevelLockedTone()
            if (settings.vibrationEnabled) audioHaptics.vibrateLevelAligned()
        }
        wasLevelLastFrame = isLevel

        _uiState.update {
            it.copy(
                pitch = calibratedPitch,
                roll = calibratedRoll,
                totalDeviation = activeDeviation,
                isLevel = isLevel
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
