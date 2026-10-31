package com.omnitool.app.ui.screens.measure

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnitool.app.ui.screens.measure.components.CameraViewfinder
import com.omnitool.app.ui.screens.measure.components.ScreenRuler
import com.omnitool.app.ui.theme.*

@Composable
fun TapeMeasureScreen(
    viewModel: TapeMeasureViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startListening()
        onDispose { viewModel.stopListening() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IndustrialDark)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header & Unit Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "SMART TAPE MEASURE & AR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Screen Caliper & Optical Rangefinder",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Unit Switcher (Metric / Imperial)
            Button(
                onClick = { viewModel.toggleUnits() },
                colors = ButtonDefaults.buttonColors(containerColor = IndustrialCard),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, LaserCyan),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (state.isMetric) "METRIC (m/cm)" else "IMPERIAL (ft/in)",
                    fontSize = 11.sp,
                    color = LaserCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tool Mode Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MeasureMode.entries.forEach { mode ->
                val isSelected = state.currentMode == mode
                val label = when (mode) {
                    MeasureMode.SCREEN_RULER -> "Screen Caliper"
                    MeasureMode.CAMERA_LASER -> "Camera Laser"
                    MeasureMode.AREA_POLYGON -> "Area & Perimeter"
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

        Spacer(modifier = Modifier.height(14.dp))

        // Active Tool Content Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (state.currentMode) {
                MeasureMode.SCREEN_RULER -> {
                    ScreenRuler(isMetric = state.isMetric)
                }
                MeasureMode.CAMERA_LASER -> {
                    CameraViewfinder(
                        currentPitch = state.currentPitch,
                        estimatedDistanceMeters = state.liveDistanceMeters,
                        isMetric = state.isMetric,
                        onAddPoint = { viewModel.pinCurrentPoint() }
                    )
                }
                MeasureMode.AREA_POLYGON -> {
                    AreaPolygonView(
                        state = state,
                        onAddPoint = { viewModel.pinCurrentPoint() },
                        onClearPoints = { viewModel.clearPoints() },
                        onSave = { viewModel.saveMeasurement("Floor Plan Area") }
                    )
                }
            }
        }
    }
}

@Composable
private fun AreaPolygonView(
    state: TapeMeasureState,
    onAddPoint: () -> Unit,
    onClearPoints: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(IndustrialSurface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Summary Cards: Area and Perimeter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = IndustrialCard),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "TOTAL AREA", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = if (state.isMetric) {
                            String.format(java.util.Locale.US, "%.2f m²", state.calculatedAreaSqMeters)
                        } else {
                            String.format(java.util.Locale.US, "%.1f sq ft", state.calculatedAreaSqMeters * 10.7639f)
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = LaserCyan
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = IndustrialCard),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "PERIMETER", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = if (state.isMetric) {
                            String.format(java.util.Locale.US, "%.2f m", state.calculatedPerimeterMeters)
                        } else {
                            String.format(java.util.Locale.US, "%.1f ft", state.calculatedPerimeterMeters * 3.28084f)
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = NeonEmerald
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pinned Points List
        Text(
            text = "PINNED CORNER POINTS (${state.pinnedPoints.size})",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (state.pinnedPoints.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No points added yet.\nPoint your camera at room corners and tap '+ Pin Point' to calculate perimeter and area.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(state.pinnedPoints) { pt ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(IndustrialCard, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = pt.label, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format(java.util.Locale.US, "%.2f m @ %+.1f°", pt.distanceMeters, pt.pitchAngle),
                            color = LaserCyan,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions: Add Point, Clear, Save
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAddPoint,
                modifier = Modifier.weight(1f).height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LaserCyan),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "+ PIN POINT", color = IndustrialDark, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onClearPoints,
                modifier = Modifier.height(46.dp),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DangerCrimson)
            ) {
                Text(text = "CLEAR", color = DangerCrimson)
            }

            Button(
                onClick = onSave,
                modifier = Modifier.height(46.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "💾 SAVE", color = IndustrialDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}
