package com.app.pose.ui.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pose.data.ProgressRepository
import com.app.pose.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val userProfile: UserProfile? = null,
    val isVoiceCoachingEnabled: Boolean = true,
    val isHapticsEnabled: Boolean = true,
    val isSaveVideoEnabled: Boolean = false,
    val unitSystem: String = "Metric"
)

class ProfileViewModel(
    private val progressRepository: ProgressRepository = ProgressRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            progressRepository.userProfile.collect { user ->
                _uiState.update { it.copy(userProfile = user) }
            }
        }
    }

    fun toggleVoiceCoaching() {
        _uiState.update { it.copy(isVoiceCoachingEnabled = !it.isVoiceCoachingEnabled) }
    }

    fun toggleHaptics() {
        _uiState.update { it.copy(isHapticsEnabled = !it.isHapticsEnabled) }
    }

    fun toggleSaveVideo() {
        _uiState.update { it.copy(isSaveVideoEnabled = !it.isSaveVideoEnabled) }
    }

    fun setUnitSystem(unit: String) {
        _uiState.update { it.copy(unitSystem = unit) }
    }
}
