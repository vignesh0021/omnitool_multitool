package com.omnitool.app

import com.omnitool.app.utils.TrigonometryUtils
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.sqrt

class TrigonometryUtilsTest {

    @Test
    fun testBullseyeDeviationZeroWhenFlat() {
        val deviation = TrigonometryUtils.calculateBullseyeDeviation(0f, 0f)
        assertEquals(0f, deviation, 0.001f)
    }

    @Test
    fun testBullseyeDeviationPythagorean() {
        // 3-4-5 triangle: pitch = 3, roll = 4 => deviation = 5
        val deviation = TrigonometryUtils.calculateBullseyeDeviation(3f, 4f)
        assertEquals(5f, deviation, 0.001f)
    }

    @Test
    fun testGroundDistanceCalculation() {
        // Height = 1.0 m, pitch = 45 deg => tan(45) = 1.0 => distance = 1.0 m
        val distance = TrigonometryUtils.calculateGroundDistance(1.0f, 45.0f)
        assertEquals(1.0f, distance, 0.01f)
    }

    @Test
    fun testRectanglePerimeterAndArea() {
        // 3m x 4m rectangle: Points (0,0), (3,0), (3,4), (0,4)
        val rectangle = listOf(
            Pair(0f, 0f),
            Pair(3f, 0f),
            Pair(3f, 4f),
            Pair(0f, 4f)
        )
        val perimeter = TrigonometryUtils.calculatePerimeter(rectangle)
        val area = TrigonometryUtils.calculatePolygonArea(rectangle)

        assertEquals(14f, perimeter, 0.01f) // 3 + 4 + 3 + 4 = 14
        assertEquals(12f, area, 0.01f)      // 3 * 4 = 12
    }

    @Test
    fun testUnitConversions() {
        assertEquals(10f, TrigonometryUtils.inchesToCm(TrigonometryUtils.cmToInches(10f)), 0.001f)
        assertEquals(5f, TrigonometryUtils.feetToMeters(TrigonometryUtils.metersToFeet(5f)), 0.001f)
    }
}
