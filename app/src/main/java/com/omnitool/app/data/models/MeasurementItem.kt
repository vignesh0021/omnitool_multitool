package com.omnitool.app.data.models

data class MeasurementItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val type: MeasurementType,
    val primaryValue: Float,
    val secondaryValue: Float = 0f,
    val unit: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MeasurementType {
    LINEAR_RULER,
    CAMERA_DISTANCE,
    OBJECT_HEIGHT,
    POLYGON_AREA
}
