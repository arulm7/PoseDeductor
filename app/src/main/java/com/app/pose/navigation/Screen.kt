package com.app.pose.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Library : Screen("library")
    object Detail : Screen("detail/{exerciseId}") {
        fun createRoute(exerciseId: String) = "detail/$exerciseId"
    }
    object Camera : Screen("camera/{exerciseId}") {
        fun createRoute(exerciseId: String) = "camera/$exerciseId"
    }
    object Summary : Screen("summary/{exerciseId}/{reps}/{correctReps}/{formScore}/{durationSec}") {
        fun createRoute(
            exerciseId: String,
            reps: Int,
            correctReps: Int,
            formScore: Int,
            durationSec: Int
        ) = "summary/$exerciseId/$reps/$correctReps/$formScore/$durationSec"
    }
    object Progress : Screen("progress")
    object Profile : Screen("profile")
}
