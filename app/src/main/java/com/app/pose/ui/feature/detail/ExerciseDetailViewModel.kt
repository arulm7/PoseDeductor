package com.app.pose.ui.feature.detail

import androidx.lifecycle.ViewModel
import com.app.pose.data.ExerciseRepository
import com.app.pose.domain.model.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExerciseDetailViewModel(
    private val exerciseRepository: ExerciseRepository = ExerciseRepository()
) : ViewModel() {

    private val _exercise = MutableStateFlow<Exercise?>(null)
    val exercise: StateFlow<Exercise?> = _exercise.asStateFlow()

    fun loadExercise(exerciseId: String) {
        _exercise.value = exerciseRepository.getExerciseById(exerciseId)
    }
}
