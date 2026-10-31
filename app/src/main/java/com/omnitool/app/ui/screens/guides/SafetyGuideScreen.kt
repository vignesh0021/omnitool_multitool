package com.omnitool.app.ui.screens.guides

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnitool.app.data.models.ChecklistItem
import com.omnitool.app.data.models.SafetyGuideRepository
import com.omnitool.app.ui.theme.*

@Composable
fun SafetyGuideScreen(
    modifier: Modifier = Modifier
) {
    var checklist by remember { mutableStateOf(SafetyGuideRepository.getDefaultChecklist()) }
    val wiringStandards = remember { SafetyGuideRepository.getWiringStandards() }
    val materialTips = remember { SafetyGuideRepository.getWallMaterialTips() }

    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IndustrialDark)
            .padding(16.dp)
    ) {
        // Title Bar
        Text(
            text = "SAFETY & DRILLING GUIDES",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Essential checklists, wiring heights & wall physics",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Tabs: Checklist, Wiring Standards, Materials
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = IndustrialCard,
            contentColor = LaserCyan
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Pre-Drill Checklist", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Wiring Heights", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Wall Materials", fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> {
                // Checklist Tab
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(checklist) { item ->
                        ChecklistItemCard(
                            item = item,
                            onToggle = {
                                checklist = checklist.map {
                                    if (it.id == item.id) it.copy(isChecked = !it.isChecked) else it
                                }
                            }
                        )
                    }
                }
            }
            1 -> {
                // Wiring Standards Tab
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(wiringStandards) { std ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = IndustrialCard),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${std.flagEmoji} ${std.country}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "⚡ Light Switches: ${std.switchHeight}",
                                    fontSize = 13.sp,
                                    color = LaserCyan,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "🔌 Power Sockets: ${std.socketHeight}",
                                    fontSize = 13.sp,
                                    color = NeonEmerald,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = std.notes,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
            2 -> {
                // Materials Tab
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(materialTips) { tip ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = IndustrialCard),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "🧱 ${tip.material}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Max Depth: ${tip.detectionDepth}",
                                        fontSize = 12.sp,
                                        color = LaserCyan
                                    )
                                    Text(
                                        text = "Interference: ${tip.interferenceLevel}",
                                        fontSize = 12.sp,
                                        color = if (tip.interferenceLevel.contains("High")) CautionAmber else NeonEmerald
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = tip.advice,
                                    fontSize = 12.sp,
                                    color = TextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistItemCard(
    item: ChecklistItem,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (item.isChecked) IndustrialSurface else IndustrialCard
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = NeonEmerald,
                    uncheckedColor = IndustrialBorder
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (item.isChecked) TextSecondary else TextPrimary
                    )
                    if (item.isCrucial) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CRITICAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DangerCrimson
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = TextMuted,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
