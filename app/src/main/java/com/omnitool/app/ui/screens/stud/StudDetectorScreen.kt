package com.omnitool.app.ui.screens.stud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnitool.app.ui.screens.stud.components.MagneticGauge
import com.omnitool.app.ui.theme.*

@Composable
fun StudDetectorScreen(
    viewModel: StudDetectorViewModel,
    onNavigateToGuides: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startListening()
        onDispose { viewModel.stopListening() }
    }

    val statusBgColor by animateColorAsState(
        targetValue = when (state.status) {
            DetectionStatus.ALERT -> DangerCrimson.copy(alpha = 0.25f)
            DetectionStatus.CAUTION -> CautionAmber.copy(alpha = 0.25f)
            DetectionStatus.SAFE -> IndustrialSurface
        },
        label = "statusBgColor"
    )

    val statusBorderColor by animateColorAsState(
        targetValue = when (state.status) {
            DetectionStatus.ALERT -> DangerCrimson
            DetectionStatus.CAUTION -> CautionAmber
            DetectionStatus.SAFE -> IndustrialBorder
        },
        label = "statusBorderColor"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IndustrialDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Title Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "STUD & WIRE DETECTOR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "EMF Magnetic Proximity Scanner",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            IconButton(
                onClick = onNavigateToGuides,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(IndustrialCard)
            ) {
                Text(text = "📋", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(statusBgColor)
                .border(1.5.dp, statusBorderColor, RoundedCornerShape(12.dp))
                .padding(vertical = 12.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.status.label,
                color = when (state.status) {
                    DetectionStatus.ALERT -> DangerCrimson
                    DetectionStatus.CAUTION -> CautionAmber
                    DetectionStatus.SAFE -> NeonEmerald
                },
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Magnetic Needle Gauge
        MagneticGauge(
            currentMicroTesla = state.currentMagnitude,
            maxRange = 160f
        )

        // Telemetry Readings (Baseline, Delta, Estimated Depth)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TelemetryBox(
                label = "DELTA (+)",
                value = String.format(java.util.Locale.US, "+%.1f μT", state.delta),
                valueColor = if (state.delta > 20f) DangerCrimson else TextPrimary
            )
            TelemetryBox(
                label = "BASELINE",
                value = String.format(java.util.Locale.US, "%.1f μT", state.baseline),
                valueColor = TextSecondary
            )
            TelemetryBox(
                label = "EST. DEPTH",
                value = state.estimatedDepthCm?.let { String.format(java.util.Locale.US, "~%.1f cm", it) } ?: "—",
                valueColor = LaserCyan
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calibrate Zero Baseline Button
        Button(
            onClick = { viewModel.calibrateBaseline() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LaserCyan),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "🎯 CALIBRATE ZERO BASELINE",
                color = IndustrialDark,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sensitivity Mode Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IndustrialCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "DETECTION SENSITIVITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StudSensitivity.entries.forEach { sensitivity ->
                        val isSelected = state.sensitivity == sensitivity
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setSensitivity(sensitivity) },
                            label = {
                                Text(
                                    text = sensitivity.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LaserCyan,
                                selectedLabelColor = IndustrialDark,
                                containerColor = IndustrialSurface,
                                labelColor = TextSecondary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Sound & Vibration Toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.toggleSound() },
                modifier = Modifier.weight(1f).height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.soundEnabled) IndustrialCard else IndustrialSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (state.soundEnabled) LaserCyan else IndustrialBorder
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (state.soundEnabled) "🔊 Audio Beeper: ON" else "🔇 Audio: OFF",
                    fontSize = 12.sp,
                    color = if (state.soundEnabled) LaserCyan else TextMuted
                )
            }

            Button(
                onClick = { viewModel.toggleVibration() },
                modifier = Modifier.weight(1f).height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.vibrationEnabled) IndustrialCard else IndustrialSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (state.vibrationEnabled) NeonEmerald else IndustrialBorder
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (state.vibrationEnabled) "📳 Haptics: ON" else "📳 Haptics: OFF",
                    fontSize = 12.sp,
                    color = if (state.vibrationEnabled) NeonEmerald else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scanning Pro-Tip
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IndustrialSurface),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "💡 CONTRACTOR PRO-TIP",
                    style = MaterialTheme.typography.labelSmall,
                    color = CautionAmber
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hold phone flat against the wall away from visible outlets, press 'Calibrate Zero', then slide smoothly horizontally across the wall. Nails & screws in studs produce distinct vertical spike columns.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun TelemetryBox(
    label: String,
    value: String,
    valueColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = IndustrialCard),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = valueColor
            )
        }
    }
}
