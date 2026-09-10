package com.app.pose.ui.feature.camera

import com.app.pose.domain.model.CameraState
import com.app.pose.domain.model.Exercise
import com.app.pose.domain.model.PosePoint

data class CameraWorkoutUiState(
    val exercise: Exercise? = null,
    val state: CameraState = CameraState.READY,
    val landmarks: List<PosePoint> = emptyList(),
    val isCameraPermissionGranted: Boolean = false,
    val isFrontCamera: Boolean = true,
    val reps: Int = 0,
    val correctReps: Int = 0,
    val targetReps: Int = 12,
    val formScore: Int = 100,
    val elapsedSeconds: Int = 0,
    val cue: String? = null,
    val isPaused: Boolean = false,
    val isSoundOn: Boolean = true,
    val isComplete: Boolean = false,
    // Live TFLite Classifier Diagnostics
    val predictedClass: String? = null,
    val predictedConfidence: Float = 0f,
    val secondBestClass: String? = null,
    val secondBestConfidence: Float = 0f,
    // Live Movement Engine Diagnostics
    val kneeAngle: Float = 180f,
    val movementFromBaseline: Float = 0f,
    val repState: String = "WAITING_UP"
)
