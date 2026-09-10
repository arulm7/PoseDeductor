package com.app.pose.ui.feature.camera

import android.app.Application
import androidx.camera.core.ImageProxy
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.pose.camera.PoseLandmarkerHelper
import com.app.pose.data.ExerciseRepository
import com.app.pose.data.ProgressRepository
import com.app.pose.domain.classifier.ExerciseClassifier
import com.app.pose.domain.classifier.PoseClassificationResult
import com.app.pose.domain.engine.ExerciseRepValidator
import com.app.pose.domain.engine.RepMovementState
import com.app.pose.domain.engine.RepValidationResult
import com.app.pose.domain.engine.RepValidatorFactory
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

class CameraWorkoutViewModel @JvmOverloads constructor(
    application: Application,
    private val exerciseRepository: ExerciseRepository = ExerciseRepository(),
    private val progressRepository: ProgressRepository = ProgressRepository()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CameraWorkoutUiState())
    val uiState: StateFlow<CameraWorkoutUiState> = _uiState.asStateFlow()

    private val exerciseClassifier = ExerciseClassifier(application)
    private var repValidator: ExerciseRepValidator = RepValidatorFactory.createValidator("squat")
    private var timerJob: Job? = null

    fun initializeSession(exerciseId: String) {
        val exercise = exerciseRepository.getExerciseById(exerciseId)
        val target = exercise?.targetReps ?: 12
        repValidator = RepValidatorFactory.createValidator(exerciseId)
        repValidator.reset()

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
                isComplete = false,
                predictedClass = null,
                predictedConfidence = 0f,
                secondBestClass = null,
                secondBestConfidence = 0f,
                kneeAngle = 180f,
                movementFromBaseline = 0f,
                repState = "WAITING_UP"
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
            if (landmarks.isNotEmpty() && landmarks.size >= 33) {
                // 1. TFLite classification (runs in background coroutine)
                val classification: PoseClassificationResult? = exerciseClassifier.classify(landmarks)

                // 2. Movement Rep Engine validation
                val repResult: RepValidationResult = repValidator.processFrame(landmarks, classification, timestampMs)

                _uiState.update { current ->
                    val topLabel = classification?.topClass?.label
                    val topConf = classification?.topConfidence ?: 0f
                    val secondLabel = classification?.secondClass?.label
                    val secondConf = classification?.secondConfidence ?: 0f

                    val isFinished = repResult.repCount >= current.targetReps

                    val derivedState = when {
                        isFinished -> CameraState.COMPLETE
                        repResult.state == RepMovementState.DOWN_CONFIRMED -> CameraState.CORRECT
                        repResult.state == RepMovementState.COOLDOWN -> CameraState.CORRECT
                        else -> CameraState.TRACKING
                    }

                    current.copy(
                        landmarks = landmarks,
                        state = derivedState,
                        reps = repResult.repCount,
                        correctReps = repResult.repCount,
                        cue = repResult.feedbackCue ?: current.cue,
                        isComplete = isFinished,
                        predictedClass = topLabel,
                        predictedConfidence = topConf,
                        secondBestClass = secondLabel,
                        secondBestConfidence = secondConf,
                        kneeAngle = repResult.primaryMetricValue,
                        movementFromBaseline = repResult.movementFromBaseline,
                        repState = repResult.state.name
                    )
                }
            } else {
                _uiState.update { current ->
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
            focus = ex?.cues?.firstOrNull() ?: "Maintain good posture"
        )
        progressRepository.recordWorkoutResult(result)
        return result
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        exerciseClassifier.close()
    }
}
