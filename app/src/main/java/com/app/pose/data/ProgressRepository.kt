package com.app.pose.data

import com.app.pose.data.mock.MockProgress
import com.app.pose.domain.model.ActivityHistoryItem
import com.app.pose.domain.model.ExercisePerformance
import com.app.pose.domain.model.FormTrendItem
import com.app.pose.domain.model.ImprovementArea
import com.app.pose.domain.model.TodaySummary
import com.app.pose.domain.model.UserProfile
import com.app.pose.domain.model.WeeklyActivity
import com.app.pose.domain.model.WorkoutResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProgressRepository {
    private val _userProfile = MutableStateFlow(MockProgress.userProfile)
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _todaySummary = MutableStateFlow(MockProgress.todaySummary)
    val todaySummary: StateFlow<TodaySummary> = _todaySummary.asStateFlow()

    private val _weeklyActivity = MutableStateFlow(MockProgress.weeklyActivity)
    val weeklyActivity: StateFlow<List<WeeklyActivity>> = _weeklyActivity.asStateFlow()

    private val _formTrend = MutableStateFlow(MockProgress.formTrend)
    val formTrend: StateFlow<List<FormTrendItem>> = _formTrend.asStateFlow()

    private val _recentActivity = MutableStateFlow(MockProgress.recentActivity)
    val recentActivity: StateFlow<List<ActivityHistoryItem>> = _recentActivity.asStateFlow()

    private val _exercisePerformance = MutableStateFlow(MockProgress.exercisePerformance)
    val exercisePerformance: StateFlow<List<ExercisePerformance>> = _exercisePerformance.asStateFlow()

    private val _improvementAreas = MutableStateFlow(MockProgress.improvementAreas)
    val improvementAreas: StateFlow<List<ImprovementArea>> = _improvementAreas.asStateFlow()

    fun recordWorkoutResult(result: WorkoutResult) {
        // Add to recent activity
        val newItem = ActivityHistoryItem(
            id = "w_${System.currentTimeMillis()}",
            exerciseId = result.exerciseId,
            name = result.exerciseName,
            whenTime = "Just now",
            reps = result.reps,
            formScore = result.formScore
        )
        _recentActivity.update { listOf(newItem) + it }

        // Update today summary
        _todaySummary.update { prev ->
            prev.copy(
                completed = prev.completed + 1,
                minutes = prev.minutes + (result.durationSec / 60).coerceAtLeast(1),
                calories = prev.calories + (result.reps * 8),
                formScore = ((prev.formScore + result.formScore) / 2)
            )
        }
    }
}
