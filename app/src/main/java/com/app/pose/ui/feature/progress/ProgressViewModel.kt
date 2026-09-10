package com.app.pose.ui.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pose.data.ProgressRepository
import com.app.pose.domain.model.ExercisePerformance
import com.app.pose.domain.model.FormTrendItem
import com.app.pose.domain.model.ImprovementArea
import com.app.pose.domain.model.WeeklyActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProgressUiState(
    val selectedRange: String = "Week",
    val formTrend: List<FormTrendItem> = emptyList(),
    val weeklyActivity: List<WeeklyActivity> = emptyList(),
    val exercisePerformance: List<ExercisePerformance> = emptyList(),
    val improvementAreas: List<ImprovementArea> = emptyList(),
    val sessionsCount: Int = 12,
    val avgFormScore: Int = 88,
    val totalReps: Int = 668
)

class ProgressViewModel(
    private val progressRepository: ProgressRepository = ProgressRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                progressRepository.formTrend,
                progressRepository.weeklyActivity,
                progressRepository.exercisePerformance,
                progressRepository.improvementAreas
            ) { trend, weekly, perf, areas ->
                ProgressUiState(
                    formTrend = trend,
                    weeklyActivity = weekly,
                    exercisePerformance = perf,
                    improvementAreas = areas
                )
            }.collect { state ->
                _uiState.update { prev ->
                    state.copy(selectedRange = prev.selectedRange)
                }
            }
        }
    }

    fun onRangeSelected(range: String) {
        _uiState.update { it.copy(selectedRange = range) }
    }
}
