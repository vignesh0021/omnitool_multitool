package com.omnitool.app.utils

import kotlin.math.*

object TrigonometryUtils {

    /**
     * Calculate pitch angle (rotation around X axis) in degrees [-180, 180].
     */
    fun calculatePitch(ax: Float, ay: Float, az: Float): Float {
        return (atan2(ay.toDouble(), sqrt((ax * ax + az * az).toDouble())) * (180.0 / Math.PI)).toFloat()
    }

    /**
     * Calculate roll angle (rotation around Y axis) in degrees [-180, 180].
     */
    fun calculateRoll(ax: Float, ay: Float, az: Float): Float {
        return (atan2(-ax.toDouble(), az.toDouble()) * (180.0 / Math.PI)).toFloat()
    }

    /**
     * Calculate combined bullseye angular deviation in degrees [0, 90].
     */
    fun calculateBullseyeDeviation(pitch: Float, roll: Float): Float {
        return sqrt((pitch * pitch + roll * roll).toDouble()).toFloat()
    }

    /**
     * Calculate horizontal ground distance from camera tilt angle and camera height from ground.
     * @param cameraHeightMeters Height of device above floor (e.g. 1.4m)
     * @param pitchDegrees Downward pitch angle from horizontal in degrees (0 to 89)
     */
    fun calculateGroundDistance(cameraHeightMeters: Float, pitchDegrees: Float): Float {
        val clampedPitch = pitchDegrees.coerceIn(1f, 88f)
        val radians = Math.toRadians(clampedPitch.toDouble())
        // When pointing down: distance = height / tan(pitch)
        return (cameraHeightMeters / tan(radians)).toFloat()
    }

    /**
     * Calculate vertical height of an object given ground distance and pitch to top of object.
     * @param groundDistanceMeters Horizontal distance to the object
     * @param cameraHeightMeters Height of device above floor
     * @param pitchDegreesTop Pitch angle pointing to top of object (+ is up, - is down)
     */
    fun calculateObjectHeight(
        groundDistanceMeters: Float,
        cameraHeightMeters: Float,
        pitchDegreesTop: Float
    ): Float {
        val radians = Math.toRadians(pitchDegreesTop.toDouble())
        val deltaHeight = groundDistanceMeters * tan(radians).toFloat()
        return (cameraHeightMeters + deltaHeight).coerceAtLeast(0f)
    }

    /**
     * Calculate perimeter of a 2D polygon defined by ordered points.
     */
    fun calculatePerimeter(points: List<Pair<Float, Float>>): Float {
        if (points.size < 2) return 0f
        var perimeter = 0f
        for (i in points.indices) {
            val nextIndex = (i + 1) % points.size
            val p1 = points[i]
            val p2 = points[nextIndex]
            val dx = p2.first - p1.first
            val dy = p2.second - p1.second
            perimeter += sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        }
        return perimeter
    }

    /**
     * Calculate surface area of a 2D polygon using the Shoelace formula.
     */
    fun calculatePolygonArea(points: List<Pair<Float, Float>>): Float {
        if (points.size < 3) return 0f
        var area = 0.0
        val n = points.size
        for (i in 0 until n) {
            val j = (i + 1) % n
            area += (points[i].first * points[j].second)
            area -= (points[j].first * points[i].second)
        }
        return (abs(area) / 2.0).toFloat()
    }

    // Units conversion
    fun cmToInches(cm: Float): Float = cm / 2.54f
    fun inchesToCm(inches: Float): Float = inches * 2.54f
    fun metersToFeet(m: Float): Float = m * 3.28084f
    fun feetToMeters(ft: Float): Float = ft / 3.28084f
}
