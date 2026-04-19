package com.voicememory.ui.navigation

sealed class Screen(val route: String) {
    object Record : Screen("record")
    object Timeline : Screen("timeline")
    object Calendar : Screen("calendar")
    object Player : Screen("player/{entryId}") {
        fun createRoute(entryId: Long) = "player/$entryId"
    }
    object Capsule : Screen("capsule")
    object Settings : Screen("settings")
    object AIChat : Screen("ai_chat")
    object AIAnalysis : Screen("ai_analysis/{entryId}") {
        fun createRoute(entryId: Long) = "ai_analysis/$entryId"
    }
    object Trend : Screen("trend")
}
