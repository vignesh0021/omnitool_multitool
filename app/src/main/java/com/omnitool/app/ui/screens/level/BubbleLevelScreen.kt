package com.omnitool.app.ui.screens.level

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.omnitool.app.data.models.*
import com.omnitool.app.ui.screens.level.components.BullseyeTarget
import com.omnitool.app.ui.screens.level.components.TubularVial
import com.omnitool.app.ui.theme.*

@Composable
fun BubbleLevelScreen(
    viewModel: BubbleLevelViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startListening()
        onDispose { viewModel.stopListening() }
    }

    val levelStatusColor by animateColorAsState(
        targetValue = if (state.isLevel) NeonEmerald else IndustrialSurface,
        label = "levelStatusColor"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IndustrialDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Title & Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PRECISION SPIRIT LEVEL",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Dual-Axis Digital Inclinometer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Freeze / Hold Button
            FilledIconToggleButton(
                checked = state.isFrozen,
                onCheckedChange = { viewModel.toggleFreeze() },
                colors = IconButtonDefaults.filledIconToggleButtonColors(
                    checkedContainerColor = CautionAmber,
                    checkedContentColor = IndustrialDark,
                    containerColor = IndustrialCard,
                    contentColor = TextPrimary
                )
            ) {
                Text(text = if (state.isFrozen) "🔒" else "🔓", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Level Orientation Mode Tabs (X, Y, Bullseye)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LevelOrientationMode.entries.forEach { mode ->
                val isSelected = state.settings.mode == mode
                val label = when (mode) {
                    LevelOrientationMode.HORIZONTAL_X -> "Horizontal (X)"
                    LevelOrientationMode.VERTICAL_Y -> "Vertical (Y)"
                    LevelOrientationMode.BULLSEYE_XY -> "Bullseye (2D)"
                }
                Button(
                    onClick = { viewModel.setMode(mode) },
                    modifier = Modifier.weight(1f).height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) LaserCyan else IndustrialCard
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) IndustrialDark else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Alignment Status Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (state.isLevel) NeonEmerald.copy(alpha = 0.2f) else IndustrialSurface)
                .border(
                    1.5.dp,
                    if (state.isLevel) NeonEmerald else IndustrialBorder,
                    RoundedCornerShape(10.dp)
                )
                .padding(vertical = 10.dp, horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (state.isLevel) "🎯 PERFECTLY LEVEL (ALIGNED)" else String.format(java.util.Locale.US, "DEVIATION: %.2f°", state.totalDeviation),
                color = if (state.isLevel) NeonEmerald else CautionAmber,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Vial Target Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            val maxAngle = state.settings.scale.maxDegreeDeviation

            when (state.settings.mode) {
                LevelOrientationMode.HORIZONTAL_X -> {
                    TubularVial(
                        angleDegrees = state.pitch,
                        maxAngle = maxAngle,
                        isLevel = state.isLevel,
                        isHorizontal = true
                    )
                }
                LevelOrientationMode.VERTICAL_Y -> {
                    TubularVial(
                        angleDegrees = state.roll,
                        maxAngle = maxAngle,
                        isLevel = state.isLevel,
                        isHorizontal = false
                    )
                }
                LevelOrientationMode.BULLSEYE_XY -> {
                    BullseyeTarget(
                        pitchDegrees = state.pitch,
                        rollDegrees = state.roll,
                        maxAngle = maxAngle,
                        isLevel = state.isLevel
                    )
                }
            }
        }

        // Digital Angle Readout Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AngleTelemetryBox(
                label = "PITCH (X)",
                angle = state.pitch,
                isZero = kotlin.math.abs(state.pitch) <= state.settings.acceptableDeviation
            )
            AngleTelemetryBox(
                label = "ROLL (Y)",
                angle = state.roll,
                isZero = kotlin.math.abs(state.roll) <= state.settings.acceptableDeviation
            )
            AngleTelemetryBox(
                label = "DEVIATION",
                angle = state.totalDeviation,
                isZero = state.isLevel
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calibration Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.calibrateZero() },
                modifier = Modifier.weight(1f).height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LaserCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "📐 CALIBRATE ZERO",
                    color = IndustrialDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            OutlinedButton(
                onClick = { viewModel.resetCalibration() },
                modifier = Modifier.weight(0.7f).height(44.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, IndustrialBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "RESET CAL", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings (Viscosity, Precision Scale)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IndustrialCard),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "FLUID VISCOSITY & DAMPENING",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ViscositySetting.entries.forEach { visc ->
                        val isSelected = state.settings.viscosity == visc
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setViscosity(visc) },
                            label = { Text(text = visc.name, fontSize = 11.sp) },
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

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SCALE & ACCURACY MODE",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PrecisionScale.entries.forEach { scale ->
                        val isSelected = state.settings.scale == scale
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setScale(scale) },
                            label = { Text(text = scale.name, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonEmerald,
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

        // Audio & Haptic Feedback Toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.toggleSound() },
                modifier = Modifier.weight(1f).height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.settings.soundEnabled) IndustrialCard else IndustrialSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (state.settings.soundEnabled) LaserCyan else IndustrialBorder
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (state.settings.soundEnabled) "🔔 Level Chime: ON" else "🔕 Chime: OFF",
                    fontSize = 11.sp,
                    color = if (state.settings.soundEnabled) LaserCyan else TextMuted
                )
            }

            Button(
                onClick = { viewModel.toggleVibration() },
                modifier = Modifier.weight(1f).height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.settings.vibrationEnabled) IndustrialCard else IndustrialSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (state.settings.vibrationEnabled) NeonEmerald else IndustrialBorder
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (state.settings.vibrationEnabled) "📳 Haptics: ON" else "📳 Haptics: OFF",
                    fontSize = 11.sp,
                    color = if (state.settings.vibrationEnabled) NeonEmerald else TextMuted
                )
            }
        }
    }
}

@Composable
private fun AngleTelemetryBox(
    label: String,
    angle: Float,
    isZero: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = IndustrialCard),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = String.format(java.util.Locale.US, "%+.1f°", angle),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isZero) NeonEmerald else TextPrimary
            )
        }
    }
}
