package com.app.pose.domain.engine

enum class RepMovementState(val label: String) {
    WAITING_FOR_UP("Standing / Waiting for UP"),
    DOWN_CANDIDATE("Descending"),
    VALID_DOWN("Valid Depth"),
    RETURNING_UP("Ascending"),
    REP_COMPLETED("Rep Counted"),
    COOLDOWN("Cooldown")
}

data class RepValidationResult(
    val state: RepMovementState = RepMovementState.WAITING_FOR_UP,
    val repCount: Int = 0,
    val isRepCompleted: Boolean = false,
    val leftKneeAngle: Float = 180f,
    val rightKneeAngle: Float = 180f,
    val effectiveKneeAngle: Float = 180f,
    val standingBaseline: Float = 175f,
    val movementAmplitude: Float = 0f,
    val feedbackCue: String? = null,
    val hipDescent: Float = 0f,
    val normalizedHipDescent: Float = 0f,
    val legLength: Float = 0.5f,
    val isBilateralValid: Boolean = true,
    val isFeetGrounded: Boolean = true
)
