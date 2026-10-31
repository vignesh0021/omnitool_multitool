package com.omnitool.app.ui.screens.stud.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnitool.app.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MagneticGauge(
    currentMicroTesla: Float,
    maxRange: Float = 150f,
    modifier: Modifier = Modifier
) {
    val clampedVal = currentMicroTesla.coerceIn(0f, maxRange)
    val ratio = clampedVal / maxRange

    val animatedRatio by animateFloatAsState(
        targetValue = ratio,
        animationSpec = tween(durationMillis = 100),
        label = "gaugeNeedle"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val center = Offset(size.width / 2f, size.height * 0.78f)
            val radius = size.width * 0.40f

            // Start angle = 150 deg (bottom left), Sweep = 240 deg (up and over to bottom right: 390 = 30 deg)
            val startAngle = 150f
            val sweepAngle = 240f

            // 1. Background Arc Track
            drawArc(
                color = IndustrialBorder,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
            )

            // 2. Active Colored Indicator Arc
            val activeSweep = sweepAngle * animatedRatio
            if (activeSweep > 0.5f) {
                val arcBrush = Brush.sweepGradient(
                    0.0f to NeonEmerald,
                    0.4f to CautionAmber,
                    0.8f to DangerCrimson,
                    1.0f to DangerCrimson,
                    center = center
                )
                drawArc(
                    brush = arcBrush,
                    startAngle = startAngle,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 3. Scale Tick Marks (Every 15 degrees)
            val totalTicks = 16
            for (i in 0..totalTicks) {
                val tickRatio = i.toFloat() / totalTicks
                val tickAngle = Math.toRadians((startAngle + tickRatio * sweepAngle).toDouble())
                val isMajor = i % 4 == 0
                val tickLength = if (isMajor) 18.dp.toPx() else 10.dp.toPx()
                val tickStroke = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx()
                val tickColor = if (isMajor) TextPrimary else TextMuted

                val innerR = radius - 20.dp.toPx()
                val outerR = innerR - tickLength

                val startX = (center.x + innerR * cos(tickAngle)).toFloat()
                val startY = (center.y + innerR * sin(tickAngle)).toFloat()
                val endX = (center.x + outerR * cos(tickAngle)).toFloat()
                val endY = (center.y + outerR * sin(tickAngle)).toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickStroke,
                    cap = StrokeCap.Round
                )
            }

            // 4. Animated Needle
            val needleAngle = startAngle + animatedRatio * sweepAngle
            rotate(degrees = needleAngle + 90f, pivot = center) {
                // Needle Body
                drawLine(
                    color = if (ratio > 0.65f) DangerCrimson else LaserCyan,
                    start = Offset(center.x, center.y + 16.dp.toPx()),
                    end = Offset(center.x, center.y - radius + 8.dp.toPx()),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 5. Center Hub / Pivot Cap
            drawCircle(
                color = IndustrialDark,
                radius = 18.dp.toPx(),
                center = center
            )
            drawCircle(
                color = if (ratio > 0.65f) DangerCrimson else LaserCyan,
                radius = 12.dp.toPx(),
                center = center
            )
        }

        // Digital Value Display inside the gauge arc
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = String.format(java.util.Locale.US, "%.1f", currentMicroTesla),
                color = when {
                    ratio > 0.65f -> DangerCrimson
                    ratio > 0.40f -> CautionAmber
                    else -> NeonEmerald
                },
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "μT (MICROTESLA)",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }
    }
}
