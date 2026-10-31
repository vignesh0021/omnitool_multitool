package com.omnitool.app.ui.screens.level.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.omnitool.app.ui.theme.*

@Composable
fun TubularVial(
    angleDegrees: Float,
    maxAngle: Float = 45f,
    isLevel: Boolean = false,
    isHorizontal: Boolean = true,
    modifier: Modifier = Modifier
) {
    val clampedAngle = angleDegrees.coerceIn(-maxAngle, maxAngle)
    val ratio = clampedAngle / maxAngle // -1.0 to 1.0

    val animatedRatio by animateFloatAsState(
        targetValue = ratio,
        animationSpec = tween(durationMillis = 60),
        label = "bubblePosition"
    )

    val vialWidth = if (isHorizontal) 280.dp else 64.dp
    val vialHeight = if (isHorizontal) 64.dp else 280.dp

    Box(
        modifier = modifier
            .size(vialWidth, vialHeight)
            .clip(RoundedCornerShape(32.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cornerRadius = CornerRadius(size.minDimension / 2f, size.minDimension / 2f)

            // 1. Fluid Background with Gradient
            val fluidBrush = Brush.linearGradient(
                colors = listOf(
                    if (isLevel) NeonEmerald.copy(alpha = 0.85f) else SpiritFluidGreen.copy(alpha = 0.75f),
                    if (isLevel) NeonEmerald.copy(alpha = 0.60f) else SpiritFluidBg
                ),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )
            drawRoundRect(
                brush = fluidBrush,
                size = size,
                cornerRadius = cornerRadius
            )

            // 2. Glass Tube Border & Internal Bevel
            drawRoundRect(
                color = IndustrialBorder,
                size = size,
                cornerRadius = cornerRadius,
                style = Stroke(width = 3.dp.toPx())
            )

            // 3. Center Target Indicator Lines (tolerance zone)
            val centerTargetOffset = if (isHorizontal) size.width / 2f else size.height / 2f
            val toleranceZonePx = 28.dp.toPx()

            if (isHorizontal) {
                // Left marker line
                drawLine(
                    color = if (isLevel) TextPrimary else DangerCrimson.copy(alpha = 0.8f),
                    start = Offset(centerTargetOffset - toleranceZonePx / 2f, 6.dp.toPx()),
                    end = Offset(centerTargetOffset - toleranceZonePx / 2f, size.height - 6.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
                // Right marker line
                drawLine(
                    color = if (isLevel) TextPrimary else DangerCrimson.copy(alpha = 0.8f),
                    start = Offset(centerTargetOffset + toleranceZonePx / 2f, 6.dp.toPx()),
                    end = Offset(centerTargetOffset + toleranceZonePx / 2f, size.height - 6.dp.toPx()),
                    strokeWidth = 2.dp.toPx()
                )
            } else {
                // Top marker line
                drawLine(
                    color = if (isLevel) TextPrimary else DangerCrimson.copy(alpha = 0.8f),
                    start = Offset(6.dp.toPx(), centerTargetOffset - toleranceZonePx / 2f),
                    end = Offset(size.width - 6.dp.toPx(), centerTargetOffset - toleranceZonePx / 2f),
                    strokeWidth = 2.dp.toPx()
                )
                // Bottom marker line
                drawLine(
                    color = if (isLevel) TextPrimary else DangerCrimson.copy(alpha = 0.8f),
                    start = Offset(6.dp.toPx(), centerTargetOffset + toleranceZonePx / 2f),
                    end = Offset(size.width - 6.dp.toPx(), centerTargetOffset + toleranceZonePx / 2f),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 4. Floating Air Bubble
            val bubbleRadius = (size.minDimension - 14.dp.toPx()) / 2f
            val maxTravelPx = if (isHorizontal) {
                (size.width / 2f) - bubbleRadius - 8.dp.toPx()
            } else {
                (size.height / 2f) - bubbleRadius - 8.dp.toPx()
            }

            val bubbleCenter = if (isHorizontal) {
                Offset(
                    x = (size.width / 2f) + (animatedRatio * maxTravelPx),
                    y = size.height / 2f
                )
            } else {
                Offset(
                    x = size.width / 2f,
                    y = (size.height / 2f) + (animatedRatio * maxTravelPx)
                )
            }

            // Bubble outer glow
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = bubbleRadius,
                center = bubbleCenter
            )
            // Bubble inner liquid core
            drawCircle(
                color = if (isLevel) NeonEmerald else SpiritFluidGreen,
                radius = bubbleRadius - 3.dp.toPx(),
                center = bubbleCenter
            )
            // Bubble specular reflection highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.75f),
                radius = bubbleRadius * 0.35f,
                center = Offset(bubbleCenter.x - bubbleRadius * 0.3f, bubbleCenter.y - bubbleRadius * 0.3f)
            )

            // 5. Glass Reflection Sheen across the top edge
            drawRoundRect(
                color = SpiritGlassHighlight,
                topLeft = Offset(4.dp.toPx(), 4.dp.toPx()),
                size = Size(size.width - 8.dp.toPx(), size.height * 0.28f),
                cornerRadius = CornerRadius(cornerRadius.x * 0.8f, cornerRadius.y * 0.8f)
            )
        }
    }
}
