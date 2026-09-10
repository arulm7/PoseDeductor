package com.app.pose.domain.model

data class UserProfile(
    val firstName: String,
    val fullName: String,
    val goal: String,
    val level: String,
    val streak: Int,
    val levelNumber: Int = 4
)

data class TodaySummary(
    val completed: Int,
    val planned: Int,
    val minutes: Int,
    val calories: Int,
    val formScore: Int
)

data class WeeklyActivity(
    val day: String,
    val minutes: Int,
    val active: Boolean
)

data class FormTrendItem(
    val label: String,
    val score: Int
)

data class ActivityHistoryItem(
    val id: String,
    val exerciseId: String,
    val name: String,
    val whenTime: String,
    val reps: Int,
    val formScore: Int
)

data class ExercisePerformance(
    val name: String,
    val score: Int,
    val reps: Int
)

data class ImprovementArea(
    val title: String,
    val detail: String,
    val metric: String
)
