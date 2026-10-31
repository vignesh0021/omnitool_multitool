package com.omnitool.app.ui.screens.measure

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import com.omnitool.app.data.models.MeasurementItem
import com.omnitool.app.data.models.MeasurementType
import com.omnitool.app.utils.SensorFilter
import com.omnitool.app.utils.TrigonometryUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class MeasureMode {
    SCREEN_RULER,
    CAMERA_LASER,
    AREA_POLYGON
}

data class PinnedPoint(
    val id: Int,
    val distanceMeters: Float,
    val pitchAngle: Float,
    val label: String
)

data class TapeMeasureState(
    val currentMode: MeasureMode = MeasureMode.SCREEN_RULER,
    val isMetric: Boolean = true,
    val currentPitch: Float = 30f,
    val cameraHeightMeters: Float = 1.4f, // Standard eye-level holding height
    val liveDistanceMeters: Float = 2.42f,
    val pinnedPoints: List<PinnedPoint> = emptyList(),
    val calculatedPerimeterMeters: Float = 0f,
    val calculatedAreaSqMeters: Float = 0f,
    val savedHistory: List<MeasurementItem> = emptyList()
)

class TapeMeasureViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val filter = SensorFilter(0.18f)

    private val _uiState = MutableStateFlow(TapeMeasureState())
    val uiState: StateFlow<TapeMeasureState> = _uiState.asStateFlow()

    fun startListening() {
        accelerometer?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    fun setMode(mode: MeasureMode) {
        _uiState.update { it.copy(currentMode = mode) }
    }

    fun toggleUnits() {
        _uiState.update { it.copy(isMetric = !it.isMetric) }
    }

    fun setCameraHeight(heightMeters: Float) {
        _uiState.update { it.copy(cameraHeightMeters = heightMeters) }
    }

    fun pinCurrentPoint() {
        val currentDist = _uiState.value.liveDistanceMeters
        val currentPitch = _uiState.value.currentPitch
        val nextId = _uiState.value.pinnedPoints.size + 1
        val newPoint = PinnedPoint(nextId, currentDist, currentPitch, "Point $nextId")

        _uiState.update { state ->
            val updatedList = state.pinnedPoints + newPoint
            // Convert radial points (distance, pitch) to local coordinate plane for polygon
            val points2D = updatedList.map { p ->
                val rad = Math.toRadians(p.pitchAngle.toDouble())
                Pair((p.distanceMeters * kotlin.math.cos(rad)).toFloat(), (p.distanceMeters * kotlin.math.sin(rad)).toFloat())
            }
            val perimeter = TrigonometryUtils.calculatePerimeter(points2D)
            val area = TrigonometryUtils.calculatePolygonArea(points2D)

            state.copy(
                pinnedPoints = updatedList,
                calculatedPerimeterMeters = perimeter,
                calculatedAreaSqMeters = area
            )
        }
    }

    fun clearPoints() {
        _uiState.update {
            it.copy(
                pinnedPoints = emptyList(),
                calculatedPerimeterMeters = 0f,
                calculatedAreaSqMeters = 0f
            )
        }
    }

    fun saveMeasurement(title: String) {
        val state = _uiState.value
        val (primary, secondary, type) = when (state.currentMode) {
            MeasureMode.SCREEN_RULER -> Triple(15.2f, 0f, MeasurementType.LINEAR_RULER)
            MeasureMode.CAMERA_LASER -> Triple(state.liveDistanceMeters, 0f, MeasurementType.CAMERA_DISTANCE)
            MeasureMode.AREA_POLYGON -> Triple(state.calculatedAreaSqMeters, state.calculatedPerimeterMeters, MeasurementType.POLYGON_AREA)
        }

        val item = MeasurementItem(
            title = title.ifBlank { "Measurement #${state.savedHistory.size + 1}" },
            type = type,
            primaryValue = primary,
            secondaryValue = secondary,
            unit = if (state.isMetric) "m" else "ft"
        )

        _uiState.update { it.copy(savedHistory = listOf(item) + it.savedHistory) }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val ax = event.values[0]
        val ay = event.values[1]
        val az = event.values[2]

        val rawPitch = TrigonometryUtils.calculatePitch(ax, ay, az)
        val smoothedPitch = filter.filter(rawPitch)

        val height = _uiState.value.cameraHeightMeters
        val distance = TrigonometryUtils.calculateGroundDistance(height, smoothedPitch)

        _uiState.update {
            it.copy(
                currentPitch = smoothedPitch,
                liveDistanceMeters = distance
            )
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
