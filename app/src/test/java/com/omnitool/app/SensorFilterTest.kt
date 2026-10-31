package com.omnitool.app

import com.omnitool.app.utils.SensorFilter
import com.omnitool.app.utils.Vector3Filter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SensorFilterTest {

    @Test
    fun testInitialValueTakesImmediately() {
        val filter = SensorFilter(0.2f)
        val result = filter.filter(50f)
        assertEquals(50f, result, 0.001f)
    }

    @Test
    fun testExponentialSmoothingDampensSpikes() {
        val filter = SensorFilter(0.1f) // Heavy smoothing
        filter.filter(10f)
        val step1 = filter.filter(100f) // Sudden spike from 10 to 100
        // Expected: 10 + 0.1 * (100 - 10) = 19
        assertEquals(19f, step1, 0.01f)
    }

    @Test
    fun testVector3FilterSmoothesAllAxes() {
        val vectorFilter = Vector3Filter(0.5f)
        vectorFilter.filter(0f, 0f, 0f)
        val (x, y, z) = vectorFilter.filter(10f, 20f, 30f)
        assertEquals(5f, x, 0.01f)
        assertEquals(10f, y, 0.01f)
        assertEquals(15f, z, 0.01f)
    }
}
