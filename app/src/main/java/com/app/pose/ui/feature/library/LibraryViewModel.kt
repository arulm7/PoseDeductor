package com.app.pose.ui.feature.library

import androidx.lifecycle.ViewModel
import com.app.pose.data.ExerciseRepository
import com.app.pose.domain.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LibraryViewModel(
    private val exerciseRepository: ExerciseRepository = ExerciseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LibraryUiState(
            exercises = exerciseRepository.getAllExercises(),
            totalCount = exerciseRepository.getAllExercises().size
        )
    )
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
        filterExercises()
    }

    fun onCategorySelect(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
        filterExercises()
    }

    private fun filterExercises() {
        val current = _uiState.value
        val filtered = exerciseRepository.searchExercises(current.query, current.selectedCategory)
        _uiState.update { it.copy(exercises = filtered) }
    }
}
