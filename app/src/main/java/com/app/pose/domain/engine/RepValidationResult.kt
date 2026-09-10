package com.app.pose.domain.engine

enum class RepMovementState(val label: String) {
    WAITING_UP("Standing / Ready"),
    DOWN_CANDIDATE("Initiating Movement"),
    DOWN_CONFIRMED("Depth Confirmed"),
    RETURNING_UP("Ascending"),
    COOLDOWN("Rep Completed")
}

data class RepValidationResult(
    val state: RepMovementState = RepMovementState.WAITING_UP,
    val repCount: Int = 0,
    val isRepCompleted: Boolean = false,
    val primaryMetricValue: Float = 180f,     // e.g. current smoothed knee angle
    val movementFromBaseline: Float = 0f,     // e.g. angle drop from standing baseline
    val standingBaseline: Float = 175f,       // calibrated standing baseline
    val feedbackCue: String? = null
)
