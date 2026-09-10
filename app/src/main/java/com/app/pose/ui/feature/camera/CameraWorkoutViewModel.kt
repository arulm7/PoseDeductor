package com.app.pose.ui.feature.camera

import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.pose.camera.PoseLandmarkerHelper
import com.app.pose.data.ExerciseRepository
import com.app.pose.data.ProgressRepository
import com.app.pose.domain.analysis.SquatAnalysisResult
import com.app.pose.domain.analysis.SquatAnalyzer
import com.app.pose.domain.analysis.SquatPhase
import com.app.pose.domain.model.CameraState
import com.app.pose.domain.model.PosePoint
import com.app.pose.domain.model.WorkoutResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CameraWorkoutViewModel(
    private val exerciseRepository: ExerciseRepository = ExerciseRepository(),
    private val progressRepository: ProgressRepository = ProgressRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraWorkoutUiState())
    val uiState: StateFlow<CameraWorkoutUiState> = _uiState.asStateFlow()

    private val squatAnalyzer = SquatAnalyzer()
    private var timerJob: Job? = null

    fun initializeSession(exerciseId: String) {
        val exercise = exerciseRepository.getExerciseById(exerciseId)
        val target = exercise?.targetReps ?: 12
        squatAnalyzer.reset()

        _uiState.update {
            it.copy(
                exercise = exercise,
                targetReps = target,
                reps = 0,
                correctReps = 0,
                formScore = 100,
                state = CameraState.READY,
                landmarks = emptyList(),
                elapsedSeconds = 0,
                cue = "Position yourself in frame",
                isPaused = false,
                isComplete = false
            )
        }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_uiState.value.isPaused && !_uiState.value.isComplete) {
                    _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                }
            }
        }
    }

    fun onCameraPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(isCameraPermissionGranted = granted) }
    }

    fun processCameraFrame(
        imageProxy: ImageProxy,
        isFrontCamera: Boolean,
        poseLandmarkerHelper: PoseLandmarkerHelper?
    ) {
        if (_uiState.value.isPaused || _uiState.value.isComplete) {
            imageProxy.close()
            return
        }
        poseLandmarkerHelper?.detectLiveStream(imageProxy, isFrontCamera)
    }

    fun onPoseLandmarksDetected(landmarks: List<PosePoint>, timestampMs: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            _uiState.update { current ->
                if (landmarks.isNotEmpty()) {
                    val analysis: SquatAnalysisResult = squatAnalyzer.analyze(landmarks, timestampMs)
                    val target = current.targetReps

                    val isFinished = analysis.repCount >= target

                    val derivedState = when {
                        isFinished -> CameraState.COMPLETE
                        !analysis.isStartingPositionValid -> CameraState.READY
                        analysis.isRepCompleted && !analysis.isRepValid -> CameraState.INCORRECT
                        analysis.isRepCompleted && analysis.isRepValid -> CameraState.CORRECT
                        analysis.phase == SquatPhase.BOTTOM -> CameraState.CORRECT
                        else -> CameraState.TRACKING
                    }

                    current.copy(
                        landmarks = landmarks,
                        state = derivedState,
                        reps = analysis.repCount,
                        correctReps = analysis.validRepCount,
                        formScore = analysis.formScore,
                        cue = analysis.feedbackCue ?: current.cue,
                        isComplete = isFinished
                    )
                } else {
                    val derivedState = if (current.state == CameraState.TRACKING || current.state == CameraState.CORRECT) {
                        CameraState.LOST
                    } else {
                        current.state
                    }
                    current.copy(
                        landmarks = emptyList(),
                        state = derivedState,
                        cue = if (derivedState == CameraState.LOST) "Step back so your full body is visible" else current.cue
                    )
                }
            }
        }
    }

    fun togglePause() {
        _uiState.update { it.copy(isPaused = !it.isPaused) }
    }

    fun toggleSound() {
        _uiState.update { it.copy(isSoundOn = !it.isSoundOn) }
    }

    fun flipCamera() {
        _uiState.update { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun getWorkoutResult(): WorkoutResult {
        val state = _uiState.value
        val ex = state.exercise
        val reps = state.reps.coerceAtLeast(1)
        val correct = state.correctReps
        val score = if (reps > 0) (correct * 100) / reps else 100
        val result = WorkoutResult(
            exerciseId = ex?.id ?: "squat",
            exerciseName = ex?.name ?: "Squat",
            reps = reps,
            correctReps = correct,
            formScore = score,
            durationSec = state.elapsedSeconds.coerceAtLeast(10),
            focus = ex?.cues?.firstOrNull() ?: "Keep your knees aligned"
        )
        progressRepository.recordWorkoutResult(result)
        return result
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
