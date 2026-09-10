package com.app.pose.domain.analysis

enum class SquatPhase(val label: String) {
    STARTING_POSITION("Position in frame"),
    STANDING("Standing tall"),
    DESCENDING("Squatting down"),
    BOTTOM("Bottom reached"),
    ASCENDING("Driving up")
}

enum class SquatDepth(val label: String) {
    NONE("None"),
    SHALLOW("Shallow"),
    PARALLEL("Parallel"),
    DEEP("Deep")
}

data class SquatAnalysisResult(
    val phase: SquatPhase = SquatPhase.STARTING_POSITION,
    val leftKneeAngle: Float = 180f,
    val rightKneeAngle: Float = 180f,
    val avgKneeAngle: Float = 180f,
    val leftHipAngle: Float = 180f,
    val rightHipAngle: Float = 180f,
    val avgHipAngle: Float = 180f,
    val torsoAngle: Float = 0f,
    val depth: SquatDepth = SquatDepth.NONE,
    val isStartingPositionValid: Boolean = false,
    val isRepCompleted: Boolean = false,
    val isRepValid: Boolean = true,
    val feedbackCue: String? = null,
    val repCount: Int = 0,
    val validRepCount: Int = 0,
    val formScore: Int = 100
)
