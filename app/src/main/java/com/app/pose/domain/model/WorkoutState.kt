package com.app.pose.domain.model

enum class CameraState {
    READY,
    TRACKING,
    CORRECT,
    INCORRECT,
    LOST,
    COMPLETE
}

enum class FormTone {
    GOOD,
    WARN,
    BAD,
    NEUTRAL
}

data class WorkoutResult(
    val exerciseId: String,
    val exerciseName: String,
    val reps: Int,
    val correctReps: Int,
    val formScore: Int,
    val durationSec: Int,
    val focus: String
)
