package com.omnitool.app.ui.screens.measure.components

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.omnitool.app.ui.theme.*

@Composable
fun CameraViewfinder(
    currentPitch: Float,
    estimatedDistanceMeters: Float,
    isMetric: Boolean = true,
    onAddPoint: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .background(IndustrialDark)
    ) {
        // 1. CameraX PreviewView
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview
                        )
                    } catch (_: Exception) {}
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. HUD Crosshair Overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val reticleRadius = 36.dp.toPx()

            // Outer Target Circle
            drawCircle(
                color = LaserCyan,
                radius = reticleRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // Inner Center Dot
            drawCircle(
                color = DangerCrimson,
                radius = 4.dp.toPx(),
                center = center
            )

            // Crosshair lines
            val lineLength = 24.dp.toPx()
            val gap = 12.dp.toPx()

            // Top
            drawLine(
                color = LaserCyan,
                start = Offset(center.x, center.y - reticleRadius - gap),
                end = Offset(center.x, center.y - reticleRadius - gap - lineLength),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Bottom
            drawLine(
                color = LaserCyan,
                start = Offset(center.x, center.y + reticleRadius + gap),
                end = Offset(center.x, center.y + reticleRadius + gap + lineLength),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Left
            drawLine(
                color = LaserCyan,
                start = Offset(center.x - reticleRadius - gap, center.y),
                end = Offset(center.x - reticleRadius - gap - lineLength, center.y),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Right
            drawLine(
                color = LaserCyan,
                start = Offset(center.x + reticleRadius + gap, center.y),
                end = Offset(center.x + reticleRadius + gap + lineLength, center.y),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // 3. Top Telemetry Banner
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(containerColor = IndustrialDark.copy(alpha = 0.85f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "LASER DISTANCE", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = if (isMetric) {
                            String.format(java.util.Locale.US, "%.2f m", estimatedDistanceMeters)
                        } else {
                            String.format(java.util.Locale.US, "%.1f ft", estimatedDistanceMeters * 3.28084f)
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = LaserCyan
                    )
                }

                Divider(
                    modifier = Modifier.height(28.dp).width(1.dp),
                    color = IndustrialBorder
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "TILT PITCH", fontSize = 10.sp, color = TextMuted)
                    Text(
                        text = String.format(java.util.Locale.US, "%+.1f°", currentPitch),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (currentPitch > 10f) NeonEmerald else CautionAmber
                    )
                }
            }
        }

        // 4. Bottom Action Bar: Capture Point
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onAddPoint,
                modifier = Modifier
                    .height(56.dp)
                    .padding(horizontal = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LaserCyan),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = "📍 PIN TARGET POINT",
                    color = IndustrialDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
