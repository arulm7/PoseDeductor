package com.app.pose.data.mock

import com.app.pose.domain.model.ActivityHistoryItem
import com.app.pose.domain.model.ExercisePerformance
import com.app.pose.domain.model.FormTrendItem
import com.app.pose.domain.model.ImprovementArea
import com.app.pose.domain.model.TodaySummary
import com.app.pose.domain.model.UserProfile
import com.app.pose.domain.model.WeeklyActivity

object MockProgress {
    val userProfile = UserProfile(
        firstName = "Maya",
        fullName = "Maya Bennett",
        goal = "Build strength",
        level = "Intermediate",
        streak = 6,
        levelNumber = 4
    )

    val todaySummary = TodaySummary(
        completed = 3,
        planned = 5,
        minutes = 18,
        calories = 214,
        formScore = 88
    )

    val weeklyActivity = listOf(
        WeeklyActivity("M", 22, true),
        WeeklyActivity("T", 14, true),
        WeeklyActivity("W", 0, false),
        WeeklyActivity("T", 28, true),
        WeeklyActivity("F", 18, true),
        WeeklyActivity("S", 34, true),
        WeeklyActivity("S", 12, true)
    )

    val formTrend = listOf(
        FormTrendItem("W1", 68),
        FormTrendItem("W2", 72),
        FormTrendItem("W3", 71),
        FormTrendItem("W4", 79),
        FormTrendItem("W5", 84),
        FormTrendItem("W6", 88)
    )

    val recentActivity = listOf(
        ActivityHistoryItem("a1", "squat", "Squat", "Today · 8:12 AM", 12, 83),
        ActivityHistoryItem("a2", "pushup", "Push-up", "Today · 8:04 AM", 10, 91),
        ActivityHistoryItem("a3", "plank", "Plank", "Yesterday · 7:40 PM", 3, 76)
    )

    val exercisePerformance = listOf(
        ExercisePerformance("Push-up", 91, 148),
        ExercisePerformance("Shoulder Press", 87, 96),
        ExercisePerformance("Squat", 83, 212),
        ExercisePerformance("Lunge", 74, 88),
        ExercisePerformance("Plank", 69, 24)
    )

    val improvementAreas = listOf(
        ImprovementArea(
            title = "Knee alignment on squats",
            detail = "Knees drifted inward on 4 of 12 reps this week.",
            metric = "-9%"
        ),
        ImprovementArea(
            title = "Depth consistency",
            detail = "Lunges finished above parallel on the last set.",
            metric = "-6%"
        )
    )
}
