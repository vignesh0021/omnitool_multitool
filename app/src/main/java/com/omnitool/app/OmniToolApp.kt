package com.omnitool.app

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.omnitool.app.ui.navigation.NavigationItem
import com.omnitool.app.ui.screens.guides.SafetyGuideScreen
import com.omnitool.app.ui.screens.level.BubbleLevelScreen
import com.omnitool.app.ui.screens.level.BubbleLevelViewModel
import com.omnitool.app.ui.screens.measure.TapeMeasureScreen
import com.omnitool.app.ui.screens.measure.TapeMeasureViewModel
import com.omnitool.app.ui.screens.stud.StudDetectorScreen
import com.omnitool.app.ui.screens.stud.StudDetectorViewModel
import com.omnitool.app.ui.theme.*

@Composable
fun OmniToolApp() {
    val context = LocalContext.current
    var currentRoute by remember { mutableStateOf(NavigationItem.StudDetector.route) }

    // ViewModels for each tool module
    val studViewModel: StudDetectorViewModel = viewModel()
    val levelViewModel: BubbleLevelViewModel = viewModel()
    val measureViewModel: TapeMeasureViewModel = viewModel()

    // Camera permission check & launcher for AR Measure
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = IndustrialSurface,
                contentColor = LaserCyan,
                tonalElevation = 8.dp
            ) {
                NavigationItem.items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (item == NavigationItem.TapeMeasure && !hasCameraPermission) {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                            currentRoute = item.route
                        },
                        icon = {
                            Text(
                                text = item.iconEmoji,
                                fontSize = if (isSelected) 22.sp else 18.sp
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) LaserCyan else TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = LaserCyan,
                            selectedTextColor = LaserCyan,
                            indicatorColor = IndustrialCard,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        },
        containerColor = IndustrialDark
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(IndustrialDark)
        ) {
            Crossfade(targetState = currentRoute, label = "tabTransition") { route ->
                when (route) {
                    NavigationItem.StudDetector.route -> {
                        StudDetectorScreen(
                            viewModel = studViewModel,
                            onNavigateToGuides = { currentRoute = NavigationItem.SafetyGuides.route }
                        )
                    }
                    NavigationItem.BubbleLevel.route -> {
                        BubbleLevelScreen(viewModel = levelViewModel)
                    }
                    NavigationItem.TapeMeasure.route -> {
                        TapeMeasureScreen(viewModel = measureViewModel)
                    }
                    NavigationItem.SafetyGuides.route -> {
                        SafetyGuideScreen()
                    }
                }
            }
        }
    }
}
