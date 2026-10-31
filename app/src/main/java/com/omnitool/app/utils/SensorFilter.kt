package com.omnitool.app.utils

/**
 * Filter utility providing exponential moving average (EMA) smoothing
 * for sensor data, simulating physical fluid viscosity or noise reduction.
 */
class SensorFilter(private var alpha: Float = 0.2f) {

    private var hasValue = false
    private var currentValue = 0f

    fun setAlpha(newAlpha: Float) {
        this.alpha = newAlpha.coerceIn(0.01f, 1.0f)
    }

    fun getAlpha(): Float = alpha

    fun filter(newValue: Float): Float {
        if (!hasValue) {
            currentValue = newValue
            hasValue = true
            return newValue
        }
        currentValue = currentValue + alpha * (newValue - currentValue)
        return currentValue
    }

    fun reset() {
        hasValue = false
        currentValue = 0f
    }
}

/**
 * 3-Axis sensor filter for vector quantities (e.g. magnetic field or acceleration).
 */
class Vector3Filter(alpha: Float = 0.2f) {
    private val filterX = SensorFilter(alpha)
    private val filterY = SensorFilter(alpha)
    private val filterZ = SensorFilter(alpha)

    fun setAlpha(newAlpha: Float) {
        filterX.setAlpha(newAlpha)
        filterY.setAlpha(newAlpha)
        filterZ.setAlpha(newAlpha)
    }

    fun filter(x: Float, y: Float, z: Float): Triple<Float, Float, Float> {
        return Triple(
            filterX.filter(x),
            filterY.filter(y),
            filterZ.filter(z)
        )
    }

    fun reset() {
        filterX.reset()
        filterY.reset()
        filterZ.reset()
    }
}
