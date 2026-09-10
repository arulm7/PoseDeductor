package com.app.pose.ui.feature.summary

import androidx.lifecycle.ViewModel
import com.app.pose.data.ExerciseRepository
import com.app.pose.domain.model.WorkoutResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WorkoutSummaryViewModel(
    private val exerciseRepository: ExerciseRepository = ExerciseRepository()
) : ViewModel() {

    private val _result = MutableStateFlow<WorkoutResult?>(null)
    val result: StateFlow<WorkoutResult?> = _result.asStateFlow()

    fun setResult(
        exerciseId: String,
        reps: Int,
        correctReps: Int,
        formScore: Int,
        durationSec: Int
    ) {
        val exercise = exerciseRepository.getExerciseById(exerciseId)
        _result.value = WorkoutResult(
            exerciseId = exerciseId,
            exerciseName = exercise?.name ?: "Workout",
            reps = reps,
            correctReps = correctReps,
            formScore = formScore,
            durationSec = durationSec,
            focus = exercise?.cues?.firstOrNull() ?: "Keep your knees aligned"
        )
    }
}
