package com.omnitool.app.data.models

enum class LevelOrientationMode {
    HORIZONTAL_X,   // 1D Pitch vial
    VERTICAL_Y,     // 1D Roll vial
    BULLSEYE_XY     // 2D Bullseye 2-axis circular vial
}

enum class ViscositySetting(val alpha: Float, val label: String) {
    LOW(0.35f, "Low (Fast Response)"),
    MEDIUM(0.15f, "Medium (Balanced)"),
    HIGH(0.04f, "High (Oil Damped)")
}

enum class PrecisionScale(val maxDegreeDeviation: Float, val label: String) {
    CLASSIC(45f, "Classic Mode (±45°)"),
    ENGINEER(10f, "Engineer's Mode (±10° High Precision)")
}

data class LevelSettings(
    val mode: LevelOrientationMode = LevelOrientationMode.BULLSEYE_XY,
    val viscosity: ViscositySetting = ViscositySetting.MEDIUM,
    val scale: PrecisionScale = PrecisionScale.CLASSIC,
    val acceptableDeviation: Float = 0.3f, // degrees
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val keepScreenOn: Boolean = true,
    val calibrationOffsetX: Float = 0f,
    val calibrationOffsetY: Float = 0f
)
