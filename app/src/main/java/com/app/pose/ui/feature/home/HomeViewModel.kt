package com.app.pose.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pose.data.ExerciseRepository
import com.app.pose.data.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(
    private val exerciseRepository: ExerciseRepository = ExerciseRepository(),
    private val progressRepository: ProgressRepository = ProgressRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            combine(
                progressRepository.userProfile,
                progressRepository.todaySummary,
                progressRepository.weeklyActivity,
                progressRepository.recentActivity,
                exerciseRepository.exercises
            ) { user, summary, weekly, recent, exercises ->
                HomeUiState(
                    userProfile = user,
                    todaySummary = summary,
                    weeklyActivity = weekly,
                    recommendedExercises = exercises.take(4),
                    recentActivity = recent
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
