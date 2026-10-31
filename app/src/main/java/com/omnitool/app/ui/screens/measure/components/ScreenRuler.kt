package com.omnitool.app.ui.screens.measure.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnitool.app.ui.theme.*

@Composable
fun ScreenRuler(
    isMetric: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    val ydpi = displayMetrics.ydpi.takeIf { it > 100 } ?: 400f // Fallback to 400 dpi

    // Pixels per millimeter: 1 inch = 25.4 mm => pixelsPerMm = ydpi / 25.4
    val pixelsPerMm = ydpi / 25.4f
    // Pixels per 1/16th inch
    val pixelsPerSixteenthInch = ydpi / 16.0f

    var caliperTopY by remember { mutableFloatStateOf(100f) }
    var caliperBottomY by remember { mutableFloatStateOf(500f) }

    val measuredDistancePixels = kotlin.math.abs(caliperBottomY - caliperTopY)
    val distanceMm = measuredDistancePixels / pixelsPerMm
    val distanceInches = measuredDistancePixels / ydpi

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Measurement Digital Readout Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IndustrialCard),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TOUCH CALIPER READING",
                        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = if (isMetric) {
                            String.format(java.util.Locale.US, "%.1f mm (%.2f cm)", distanceMm, distanceMm / 10f)
                        } else {
                            String.format(java.util.Locale.US, "%.3f inches", distanceInches)
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = LaserCyan
                    )
                }

                Text(
                    text = "DPI: ${ydpi.toInt()}",
                    fontSize = 11.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Visual Interactive Ruler Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(IndustrialSurface, RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val touchY = change.position.y
                        // Move whichever caliper is closer to the touch position
                        val distToTop = kotlin.math.abs(touchY - caliperTopY)
                        val distToBottom = kotlin.math.abs(touchY - caliperBottomY)
                        if (distToTop < distToBottom) {
                            caliperTopY = (caliperTopY + dragAmount.y).coerceIn(20f, caliperBottomY - 10f)
                        } else {
                            caliperBottomY = (caliperBottomY + dragAmount.y).coerceIn(caliperTopY + 10f, size.height.toFloat() - 20f)
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val rulerWidth = size.width
                val rulerHeight = size.height

                // Draw mm or inch tick marks along the right and left edges
                if (isMetric) {
                    val totalMm = (rulerHeight / pixelsPerMm).toInt()
                    for (mm in 0..totalMm) {
                        val y = mm * pixelsPerMm
                        val isCm = mm % 10 == 0
                        val isHalfCm = mm % 5 == 0 && !isCm

                        val tickLength = when {
                            isCm -> 40.dp.toPx()
                            isHalfCm -> 24.dp.toPx()
                            else -> 12.dp.toPx()
                        }
                        val strokeWidth = if (isCm) 2.dp.toPx() else 1.dp.toPx()
                        val color = if (isCm) TextPrimary else TextMuted

                        // Left edge ticks
                        drawLine(
                            color = color,
                            start = Offset(0f, y),
                            end = Offset(tickLength, y),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                        // Right edge ticks
                        drawLine(
                            color = color,
                            start = Offset(rulerWidth, y),
                            end = Offset(rulerWidth - tickLength, y),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                } else {
                    val totalSixteenths = (rulerHeight / pixelsPerSixteenthInch).toInt()
                    for (s in 0..totalSixteenths) {
                        val y = s * pixelsPerSixteenthInch
                        val isInch = s % 16 == 0
                        val isHalfInch = s % 8 == 0 && !isInch
                        val isQuarterInch = s % 4 == 0 && !isHalfInch && !isInch

                        val tickLength = when {
                            isInch -> 44.dp.toPx()
                            isHalfInch -> 28.dp.toPx()
                            isQuarterInch -> 20.dp.toPx()
                            else -> 10.dp.toPx()
                        }
                        val strokeWidth = if (isInch) 2.dp.toPx() else 1.dp.toPx()
                        val color = if (isInch) TextPrimary else TextMuted

                        drawLine(
                            color = color,
                            start = Offset(0f, y),
                            end = Offset(tickLength, y),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = color,
                            start = Offset(rulerWidth, y),
                            end = Offset(rulerWidth - tickLength, y),
                            strokeWidth = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Measured Span Highlight
                drawRect(
                    color = LaserCyan.copy(alpha = 0.12f),
                    topLeft = Offset(45.dp.toPx(), caliperTopY),
                    size = androidx.compose.ui.geometry.Size(
                        rulerWidth - 90.dp.toPx(),
                        caliperBottomY - caliperTopY
                    )
                )

                // Top Movable Caliper Line
                drawLine(
                    color = LaserCyan,
                    start = Offset(0f, caliperTopY),
                    end = Offset(rulerWidth, caliperTopY),
                    strokeWidth = 3.dp.toPx()
                )
                drawCircle(
                    color = LaserCyan,
                    radius = 8.dp.toPx(),
                    center = Offset(rulerWidth / 2f, caliperTopY)
                )

                // Bottom Movable Caliper Line
                drawLine(
                    color = LaserCyan,
                    start = Offset(0f, caliperBottomY),
                    end = Offset(rulerWidth, caliperBottomY),
                    strokeWidth = 3.dp.toPx()
                )
                drawCircle(
                    color = LaserCyan,
                    radius = 8.dp.toPx(),
                    center = Offset(rulerWidth / 2f, caliperBottomY)
                )
            }
        }
    }
}
