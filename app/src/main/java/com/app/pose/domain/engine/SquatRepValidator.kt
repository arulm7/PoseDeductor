package com.app.pose.domain.engine

import com.app.pose.domain.analysis.JointAngleCalculator
import com.app.pose.domain.classifier.ExercisePoseClass
import com.app.pose.domain.classifier.PoseClassificationResult
import com.app.pose.domain.model.PoseLandmarkIndices
import com.app.pose.domain.model.PosePoint
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SquatRepValidator : ExerciseRepValidator {

    private var currentState: RepMovementState = RepMovementState.WAITING_UP
    private var totalReps: Int = 0

    // Bilateral smoothed knee angles
    private var smoothedLeftKnee: Float = 175f
    private var smoothedRightKnee: Float = 175f

    // Adaptive standing baseline
    private var standingKneeBaseline: Float = 175f
    private var minKneeAngleInCycle: Float = 180f
    private var maxMovementInCycle: Float = 0f

    // Debouncing & Temporal validation
    private var consecutiveFramesInState: Int = 0
    private var cooldownFramesRemaining: Int = 0
    private var lostFramesCount: Int = 0

    companion object {
        private const val MIN_VISIBILITY = 0.35f
        private const val REQUIRED_CONSECUTIVE_FRAMES = 2
        private const val COOLDOWN_FRAMES_AFTER_REP = 3

        // Movement thresholds
        private const val CANDIDATE_MOVEMENT_THRESHOLD = 15f      // 15° drop from baseline
        private const val CONFIRMED_DEPTH_MOVEMENT_THRESHOLD = 35f // 35° drop from baseline
        private const val MAX_ABSOLUTE_BOTTOM_ANGLE = 128f        // Knee must reach <= 128°
        private const val ASCENT_DELTA_FROM_MIN = 12f             // 12° rise from lowest point
        private const val RETURN_UP_DELTA_FROM_BASELINE = 12f     // Return to within 12° of standing
        private const val MAX_KNEE_ASYMMETRY = 45f                // Both legs must participate
    }

    override fun processFrame(
        landmarks: List<PosePoint>,
        tflitePrediction: PoseClassificationResult?,
        timestampMs: Long
    ): RepValidationResult {
        if (landmarks.size < 33) {
            handleLostLandmarks()
            return RepValidationResult(
                state = currentState,
                repCount = totalReps,
                isRepCompleted = false,
                primaryMetricValue = 180f,
                movementFromBaseline = 0f,
                standingBaseline = standingKneeBaseline,
                feedbackCue = "Position yourself in camera view"
            )
        }

        val leftHip = landmarks[PoseLandmarkIndices.LEFT_HIP]
        val rightHip = landmarks[PoseLandmarkIndices.RIGHT_HIP]
        val leftKnee = landmarks[PoseLandmarkIndices.LEFT_KNEE]
        val rightKnee = landmarks[PoseLandmarkIndices.RIGHT_KNEE]
        val leftAnkle = landmarks[PoseLandmarkIndices.LEFT_ANKLE]
        val rightAnkle = landmarks[PoseLandmarkIndices.RIGHT_ANKLE]

        val isLeftLegVisible = leftHip.visibility > MIN_VISIBILITY &&
                leftKnee.visibility > MIN_VISIBILITY &&
                leftAnkle.visibility > MIN_VISIBILITY

        val isRightLegVisible = rightHip.visibility > MIN_VISIBILITY &&
                rightKnee.visibility > MIN_VISIBILITY &&
                rightAnkle.visibility > MIN_VISIBILITY

        if (!isLeftLegVisible && !isRightLegVisible) {
            handleLostLandmarks()
            return RepValidationResult(
                state = currentState,
                repCount = totalReps,
                isRepCompleted = false,
                primaryMetricValue = 180f,
                movementFromBaseline = 0f,
                standingBaseline = standingKneeBaseline,
                feedbackCue = "Step back so your legs are visible"
            )
        }

        lostFramesCount = 0

        // Calculate raw bilateral knee angles
        val rawLeftKnee = if (isLeftLegVisible) {
            JointAngleCalculator.calculateAngle(leftHip, leftKnee, leftAnkle)
        } else 175f

        val rawRightKnee = if (isRightLegVisible) {
            JointAngleCalculator.calculateAngle(rightHip, rightKnee, rightAnkle)
        } else 175f

        // Exponential smoothing per leg
        smoothedLeftKnee = (smoothedLeftKnee * 0.65f) + (rawLeftKnee * 0.35f)
        smoothedRightKnee = (smoothedRightKnee * 0.65f) + (rawRightKnee * 0.35f)

        // Effective bilateral knee angle for depth evaluation
        val bothLegsVisible = isLeftLegVisible && isRightLegVisible
        val effectiveKneeAngle = if (bothLegsVisible) {
            max(smoothedLeftKnee, smoothedRightKnee) // Both knees must bend for depth
        } else if (isLeftLegVisible) smoothedLeftKnee else smoothedRightKnee

        val kneeAsymmetry = if (bothLegsVisible) abs(smoothedLeftKnee - smoothedRightKnee) else 0f

        // Movement distance (angle drop) from adaptive standing baseline
        val movementFromBaseline = max(0f, standingKneeBaseline - effectiveKneeAngle)

        var isRepCompleted = false
        var feedbackCue: String? = null

        // TFLite classification context (used as secondary supporting signal)
        val isTfliteDown = tflitePrediction?.let {
            it.topClass == ExercisePoseClass.SQUATS_DOWN && it.topConfidence >= 0.50f
        } ?: false

        val isTfliteUp = tflitePrediction?.let {
            it.topClass == ExercisePoseClass.SQUATS_UP && it.topConfidence >= 0.50f
        } ?: false

        val isDepthValid = movementFromBaseline >= CONFIRMED_DEPTH_MOVEMENT_THRESHOLD &&
                effectiveKneeAngle <= MAX_ABSOLUTE_BOTTOM_ANGLE &&
                kneeAsymmetry <= MAX_KNEE_ASYMMETRY

        // State Machine Execution
        when (currentState) {
            RepMovementState.WAITING_UP -> {
                minKneeAngleInCycle = 180f
                maxMovementInCycle = 0f

                // Calibrate standing baseline when user is upright
                if (effectiveKneeAngle >= 155f) {
                    standingKneeBaseline = (standingKneeBaseline * 0.90f) + (effectiveKneeAngle * 0.10f)
                }

                if (cooldownFramesRemaining > 0) {
                    cooldownFramesRemaining--
                }

                if (cooldownFramesRemaining == 0 && isDepthValid) {
                    consecutiveFramesInState++
                    if (consecutiveFramesInState >= REQUIRED_CONSECUTIVE_FRAMES) {
                        currentState = RepMovementState.DOWN_CONFIRMED
                        minKneeAngleInCycle = effectiveKneeAngle
                        maxMovementInCycle = movementFromBaseline
                        consecutiveFramesInState = 0
                        feedbackCue = "Good depth — drive up"
                    }
                } else if (cooldownFramesRemaining == 0 && movementFromBaseline >= CANDIDATE_MOVEMENT_THRESHOLD) {
                    consecutiveFramesInState++
                    if (consecutiveFramesInState >= REQUIRED_CONSECUTIVE_FRAMES) {
                        currentState = RepMovementState.DOWN_CANDIDATE
                        minKneeAngleInCycle = effectiveKneeAngle
                        maxMovementInCycle = movementFromBaseline
                        consecutiveFramesInState = 0
                        feedbackCue = "Squatting down"
                    }
                } else {
                    consecutiveFramesInState = 0
                    feedbackCue = if (cooldownFramesRemaining > 0) "Good rep" else "Ready — squat down"
                }
            }

            RepMovementState.DOWN_CANDIDATE -> {
                minKneeAngleInCycle = min(minKneeAngleInCycle, effectiveKneeAngle)
                maxMovementInCycle = max(maxMovementInCycle, movementFromBaseline)

                if (isDepthValid) {
                    consecutiveFramesInState++
                    if (consecutiveFramesInState >= REQUIRED_CONSECUTIVE_FRAMES) {
                        currentState = RepMovementState.DOWN_CONFIRMED
                        consecutiveFramesInState = 0
                        feedbackCue = "Good depth — drive up"
                    }
                } else if (movementFromBaseline <= 8f || effectiveKneeAngle >= standingKneeBaseline - 8f) {
                    // Aborted or partial movement returned without reaching depth -> reset with NO rep
                    consecutiveFramesInState++
                    if (consecutiveFramesInState >= REQUIRED_CONSECUTIVE_FRAMES) {
                        currentState = RepMovementState.WAITING_UP
                        consecutiveFramesInState = 0
                        feedbackCue = "Squat deeper to count a full rep"
                    }
                } else {
                    consecutiveFramesInState = 0
                    feedbackCue = if (kneeAsymmetry > MAX_KNEE_ASYMMETRY) "Bend both knees together" else "Squat down"
                }
            }

            RepMovementState.DOWN_CONFIRMED -> {
                minKneeAngleInCycle = min(minKneeAngleInCycle, effectiveKneeAngle)
                maxMovementInCycle = max(maxMovementInCycle, movementFromBaseline)

                // Check for upward ascent movement (at least 12° rise from lowest point reached)
                val isAscending = (effectiveKneeAngle > (minKneeAngleInCycle + ASCENT_DELTA_FROM_MIN) && effectiveKneeAngle > 115f) ||
                        (isTfliteUp && effectiveKneeAngle > 120f)

                if (isAscending) {
                    consecutiveFramesInState++
                    if (consecutiveFramesInState >= REQUIRED_CONSECUTIVE_FRAMES) {
                        currentState = RepMovementState.RETURNING_UP
                        consecutiveFramesInState = 0
                        feedbackCue = "Driving up"
                    }
                } else {
                    consecutiveFramesInState = 0
                    feedbackCue = "Hold and drive up"
                }
            }

            RepMovementState.RETURNING_UP -> {
                // Check if user returned completely to the standing position
                val isStandingUp = effectiveKneeAngle >= (standingKneeBaseline - RETURN_UP_DELTA_FROM_BASELINE)

                if (isStandingUp) {
                    consecutiveFramesInState++
                    if (consecutiveFramesInState >= REQUIRED_CONSECUTIVE_FRAMES) {
                        // Complete valid rep!
                        totalReps++
                        isRepCompleted = true
                        currentState = RepMovementState.COOLDOWN
                        cooldownFramesRemaining = COOLDOWN_FRAMES_AFTER_REP
                        consecutiveFramesInState = 0
                        feedbackCue = "Rep $totalReps counted!"
                    }
                } else if (isDepthValid) {
                    // Dropped back down before standing
                    currentState = RepMovementState.DOWN_CONFIRMED
                    consecutiveFramesInState = 0
                } else {
                    consecutiveFramesInState = 0
                    feedbackCue = "Stand all the way up"
                }
            }

            RepMovementState.COOLDOWN -> {
                if (cooldownFramesRemaining > 0) {
                    cooldownFramesRemaining--
                }
                val isStandingUp = effectiveKneeAngle >= (standingKneeBaseline - RETURN_UP_DELTA_FROM_BASELINE)
                if (cooldownFramesRemaining == 0 && isStandingUp) {
                    currentState = RepMovementState.WAITING_UP
                    consecutiveFramesInState = 0
                }
                feedbackCue = "Rep $totalReps complete"
            }
        }

        return RepValidationResult(
            state = currentState,
            repCount = totalReps,
            isRepCompleted = isRepCompleted,
            primaryMetricValue = effectiveKneeAngle,
            movementFromBaseline = movementFromBaseline,
            standingBaseline = standingKneeBaseline,
            feedbackCue = feedbackCue
        )
    }

    private fun handleLostLandmarks() {
        lostFramesCount++
        if (lostFramesCount >= 4) {
            currentState = RepMovementState.WAITING_UP
            consecutiveFramesInState = 0
            cooldownFramesRemaining = 0
        }
    }

    override fun reset() {
        currentState = RepMovementState.WAITING_UP
        totalReps = 0
        smoothedLeftKnee = 175f
        smoothedRightKnee = 175f
        standingKneeBaseline = 175f
        minKneeAngleInCycle = 180f
        maxMovementInCycle = 0f
        consecutiveFramesInState = 0
        cooldownFramesRemaining = 0
        lostFramesCount = 0
    }
}
