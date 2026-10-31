package com.omnitool.app.ui.navigation

sealed class NavigationItem(
    val route: String,
    val title: String,
    val iconEmoji: String
) {
    object StudDetector : NavigationItem("stud", "Stud & Wire", "🧲")
    object BubbleLevel : NavigationItem("level", "Spirit Level", "⚖️")
    object TapeMeasure : NavigationItem("measure", "AR Measure", "📐")
    object SafetyGuides : NavigationItem("guides", "Drill Safety", "📋")

    companion object {
        val items = listOf(StudDetector, BubbleLevel, TapeMeasure, SafetyGuides)
    }
}
