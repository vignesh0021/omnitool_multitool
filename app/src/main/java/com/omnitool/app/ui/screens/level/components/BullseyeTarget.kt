package com.omnitool.app.ui.screens.level.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.omnitool.app.ui.theme.*
import kotlin.math.min
import kotlin.math.sqrt

@Composable
fun BullseyeTarget(
    pitchDegrees: Float,
    rollDegrees: Float,
    maxAngle: Float = 15f,
    isLevel: Boolean = false,
    modifier: Modifier = Modifier
) {
    val normPitch = (pitchDegrees / maxAngle).coerceIn(-1.0f, 1.0f)
    val normRoll = (rollDegrees / maxAngle).coerceIn(-1.0f, 1.0f)

    val animatedPitch by animateFloatAsState(
        targetValue = normPitch,
        animationSpec = tween(durationMillis = 60),
        label = "bullseyePitch"
    )
    val animatedRoll by animateFloatAsState(
        targetValue = normRoll,
        animationSpec = tween(durationMillis = 60),
        label = "bullseyeRoll"
    )

    Box(
        modifier = modifier
            .size(280.dp)
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = min(size.width, size.height) / 2f

            // 1. Fluid Radial Gradient
            val fluidBrush = Brush.radialGradient(
                colors = listOf(
                    if (isLevel) NeonEmerald.copy(alpha = 0.85f) else SpiritFluidGreen.copy(alpha = 0.80f),
                    if (isLevel) NeonEmerald.copy(alpha = 0.45f) else SpiritFluidBg,
                    IndustrialCard
                ),
                center = center,
                radius = outerRadius
            )
            drawCircle(
                brush = fluidBrush,
                radius = outerRadius,
                center = center
            )

            // 2. Beveled Metallic Outer Ring
            drawCircle(
                color = IndustrialBorder,
                radius = outerRadius,
                center = center,
                style = Stroke(width = 6.dp.toPx())
            )

            // 3. Concentric Degree Calibration Rings
            val ringFractions = listOf(0.25f, 0.50f, 0.75f)
            ringFractions.forEach { fraction ->
                drawCircle(
                    color = TextMuted.copy(alpha = 0.45f),
                    radius = outerRadius * fraction,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Center Perfect Level Circle
            val centerTargetRadius = outerRadius * 0.18f
            drawCircle(
                color = if (isLevel) NeonEmerald else DangerCrimson.copy(alpha = 0.8f),
                radius = centerTargetRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 4. Crosshairs
            drawLine(
                color = TextMuted.copy(alpha = 0.6f),
                start = Offset(center.x, center.y - outerRadius + 8.dp.toPx()),
                end = Offset(center.x, center.y + outerRadius - 8.dp.toPx()),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = TextMuted.copy(alpha = 0.6f),
                start = Offset(center.x - outerRadius + 8.dp.toPx(), center.y),
                end = Offset(center.x + outerRadius - 8.dp.toPx(), center.y),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // 5. Floating Air Bubble
            val bubbleRadius = centerTargetRadius * 0.9f
            val maxBubbleTravel = outerRadius - bubbleRadius - 6.dp.toPx()

            // In screen coordinates:
            // Roll (left/right tilt) moves bubble along X (bubble floats opposite to tilt or follows level vial logic)
            // Pitch (forward/back tilt) moves bubble along Y
            var dx = -animatedRoll * maxBubbleTravel
            var dy = animatedPitch * maxBubbleTravel
            val currentDist = sqrt((dx * dx + dy * dy).toDouble()).toFloat()
            if (currentDist > maxBubbleTravel) {
                val scale = maxBubbleTravel / currentDist
                dx *= scale
                dy *= scale
            }

            val bubbleCenter = Offset(center.x + dx, center.y + dy)

            // Bubble glow
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = bubbleRadius,
                center = bubbleCenter
            )
            // Bubble inner core
            drawCircle(
                color = if (isLevel) NeonEmerald else SpiritFluidGreen,
                radius = bubbleRadius - 2.dp.toPx(),
                center = bubbleCenter
            )
            // Specular lens highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.85f),
                radius = bubbleRadius * 0.35f,
                center = Offset(bubbleCenter.x - bubbleRadius * 0.28f, bubbleCenter.y - bubbleRadius * 0.28f)
            )

            // 6. Glass Top Highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(center.x - outerRadius * 0.3f, center.y - outerRadius * 0.3f),
                    radius = outerRadius * 0.6f
                ),
                radius = outerRadius - 6.dp.toPx(),
                center = center
            )
        }
    }
}
