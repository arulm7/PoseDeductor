package com.app.pose.ui.feature.home

import com.app.pose.domain.model.ActivityHistoryItem
import com.app.pose.domain.model.Exercise
import com.app.pose.domain.model.TodaySummary
import com.app.pose.domain.model.UserProfile
import com.app.pose.domain.model.WeeklyActivity

data class HomeUiState(
    val userProfile: UserProfile? = null,
    val todaySummary: TodaySummary? = null,
    val weeklyActivity: List<WeeklyActivity> = emptyList(),
    val recommendedExercises: List<Exercise> = emptyList(),
    val recentActivity: List<ActivityHistoryItem> = emptyList(),
    val totalReps: Int = 248
)
