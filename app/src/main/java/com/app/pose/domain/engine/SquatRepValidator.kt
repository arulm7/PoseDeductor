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

    private var currentState: RepMovementState = RepMovementState.WAITING_FOR_UP
    private var totalReps: Int = 0

    // Bilateral smoothed knee angles
    private var smoothedLeftKnee: Float = 175f
    private var smoothedRightKnee: Float = 175f

    // Adaptive standing baseline & ROM tracking
    private var standingKneeBaseline: Float = 175f
    private var repBaseline: Float = 175f
    private var minKneeInRep: Float = 180f
    private var maxMovementAmplitudeInRep: Float = 0f

    // Vertical hip displacement tracking
    private var standingHipYBaseline: Float? = null
    private var standingLegLengthBaseline: Float = 0.5f
    private var repHipYBaseline: Float = 0.5f
    private var repLegLengthBaseline: Float = 0.5f
    private var maxDescentRatioInRep: Float = 0f

    // Temporal frame debouncing & cooldown
    private var consecutiveFramesInState: Int = 0
    private var cooldownFramesRemaining: Int = 0
    private var lostFramesCount: Int = 0

    companion object {
        private const val MIN_LANDMARK_VISIBILITY = 0.35f
        private const val REQUIRED_CONSECUTIVE_FRAMES = 2
        private const val MIN_CONSECUTIVE_DOWN_FRAMES = 3
        private const val COOLDOWN_FRAMES_AFTER_REP = 4

        // Movement Thresholds & Hysteresis
        private const val STANDING_KNEE_THRESHOLD = 155f
        private const val CANDIDATE_DESCENT_DELTA = 16f         // 16° drop from baseline to initiate descent
        private const val CANDIDATE_HIP_DESCENT_RATIO = 0.04f   // 4% hip drop to initiate candidate descent

        // Depth criteria
        private const val MIN_SQUAT_AMPLITUDE_DELTA = 40f       // At least 40° ROM drop required for strong depth
        private const val HYSTERESIS_ENTRY_MAX_KNEE = 115f      // Knee angle <= 115° for strong depth
        private const val MIN_HIP_DESCENT_RATIO = 0.08f         // At least 8% of leg length hip descent for depth

        // Borderline depth with TFLite corroboration
        private const val CORROBORATED_MAX_KNEE = 122f          // Moderate depth allowed if TFLite agrees
        private const val CORROBORATED_MIN_AMPLITUDE = 32f
        private const val CORROBORATED_MIN_HIP_DESCENT = 0.06f

        // Ascent & Return to standing
        private const val HYSTERESIS_EXIT_MIN_ASCENT_DELTA = 15f // Must rise >= 15° from minimum to exit depth
        private const val RETURN_UP_DELTA_FROM_BASELINE = 12f   // Must return to within 12° of standing baseline
        private const val RETURN_UP_MAX_HIP_DESCENT = 0.045f    // Hip must return close to baseline

        // Symmetry & Stance
        private const val MAX_KNEE_ASYMMETRY = 35f              // Both legs must participate symmetrically
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
                leftKneeAngle = smoothedLeftKnee,
                rightKneeAngle = smoothedRightKnee,
                effectiveKneeAngle = max(smoothedLeftKnee, smoothedRightKnee),
                standingBaseline = standingKneeBaseline,
                movementAmplitude = 0f,
                feedbackCue = "Position yourself in camera view",
                hipDescent = 0f,
                normalizedHipDescent = 0f,
                legLength = standingLegLengthBaseline,
                isBilateralValid = false,
                isFeetGrounded = false
            )
        }

        val leftHip = landmarks[PoseLandmarkIndices.LEFT_HIP]
        val rightHip = landmarks[PoseLandmarkIndices.RIGHT_HIP]
        val leftKnee = landmarks[PoseLandmarkIndices.LEFT_KNEE]
        val rightKnee = landmarks[PoseLandmarkIndices.RIGHT_KNEE]
        val leftAnkle = landmarks[PoseLandmarkIndices.LEFT_ANKLE]
        val rightAnkle = landmarks[PoseLandmarkIndices.RIGHT_ANKLE]

        val isLeftLegVisible = leftHip.visibility > MIN_LANDMARK_VISIBILITY &&
                leftKnee.visibility > MIN_LANDMARK_VISIBILITY &&
                leftAnkle.visibility > MIN_LANDMARK_VISIBILITY

        val isRightLegVisible = rightHip.visibility > MIN_LANDMARK_VISIBILITY &&
                rightKnee.visibility > MIN_LANDMARK_VISIBILITY &&
                rightAnkle.visibility > MIN_LANDMARK_VISIBILITY

        if (!isLeftLegVisible && !isRightLegVisible) {
            handleLostLandmarks()
            return RepValidationResult(
                state = currentState,
                repCount = totalReps,
                isRepCompleted = false,
                leftKneeAngle = smoothedLeftKnee,
                rightKneeAngle = smoothedRightKnee,
                effectiveKneeAngle = max(smoothedLeftKnee, smoothedRightKnee),
                standingBaseline = standingKneeBaseline,
                movementAmplitude = 0f,
                feedbackCue = "Step back so your legs are visible",
                hipDescent = 0f,
                normalizedHipDescent = 0f,
                legLength = standingLegLengthBaseline,
                isBilateralValid = false,
                isFeetGrounded = false
            )
        }

        lostFramesCount = 0

        // Calculate individual geometric knee angles
        val rawLeftKnee = if (isLeftLegVisible) {
            JointAngleCalculator.calculateAngle(leftHip, leftKnee, leftAnkle)
        } else 175f

        val rawRightKnee = if (isRightLegVisible) {
            JointAngleCalculator.calculateAngle(rightHip, rightKnee, rightAnkle)
        } else 175f

        // Exponential smoothing per leg to filter landmark jitter
        smoothedLeftKnee = (smoothedLeftKnee * 0.65f) + (rawLeftKnee * 0.35f)
        smoothedRightKnee = (smoothedRightKnee * 0.65f) + (rawRightKnee * 0.35f)

        // Effective bilateral evaluation
        val bothLegsVisible = isLeftLegVisible && isRightLegVisible
        val effectiveKneeAngle = if (bothLegsVisible) {
            max(smoothedLeftKnee, smoothedRightKnee) // Both knees must bend for depth
        } else if (isLeftLegVisible) smoothedLeftKnee else smoothedRightKnee

        val kneeAsymmetry = if (bothLegsVisible) abs(smoothedLeftKnee - smoothedRightKnee) else 0f

        // Vertical hip & ankle positions in MediaPipe normalized coordinates (Y: 0 is top, 1 is bottom)
        val currentHipY = when {
            bothLegsVisible -> (leftHip.y + rightHip.y) / 2f
            isLeftLegVisible -> leftHip.y
            else -> rightHip.y
        }

        val currentAnkleY = when {
            bothLegsVisible -> (leftAnkle.y + rightAnkle.y) / 2f
            isLeftLegVisible -> leftAnkle.y
            else -> rightAnkle.y
        }

        val currentLegLength = max(0.15f, currentAnkleY - currentHipY)

        // Stance & Grounding Checks:
        // 1. Both feet grounded (ankles on similar horizontal plane)
        val ankleVerticalDiff = if (bothLegsVisible) abs(leftAnkle.y - rightAnkle.y) else 0f
        val isFeetGrounded = if (bothLegsVisible) {
            ankleVerticalDiff <= (0.16f * standingLegLengthBaseline).coerceAtLeast(0.06f)
        } else true

        // 2. Neither knee touches the floor
        val isLeftKneeAboveFloor = leftKnee.y < (leftAnkle.y - 0.035f)
        val isRightKneeAboveFloor = rightKnee.y < (rightAnkle.y - 0.035f)
        val isKneeOffFloor = if (bothLegsVisible) {
            isLeftKneeAboveFloor && isRightKneeAboveFloor
        } else if (isLeftLegVisible) isLeftKneeAboveFloor else isRightKneeAboveFloor

        // 3. Bilateral knee height symmetry
        val kneeVerticalDiff = if (bothLegsVisible) abs(leftKnee.y - rightKnee.y) else 0f
        val isKneeHeightSymmetric = if (bothLegsVisible) {
            kneeVerticalDiff <= (0.28f * standingLegLengthBaseline).coerceAtLeast(0.12f)
        } else true

        // 4. Bilateral hip level symmetry
        val hipVerticalDiff = if (bothLegsVisible) abs(leftHip.y - rightHip.y) else 0f
        val isHipLevelSymmetric = hipVerticalDiff <= 0.12f

        // 5. Torso alignment
        val leftShoulder = landmarks[PoseLandmarkIndices.LEFT_SHOULDER]
        val rightShoulder = landmarks[PoseLandmarkIndices.RIGHT_SHOULDER]
        val isTorsoUpright = if (leftShoulder.visibility > MIN_LANDMARK_VISIBILITY && leftHip.visibility > MIN_LANDMARK_VISIBILITY) {
            val torsoAngle = JointAngleCalculator.calculateVerticalAngle(leftShoulder, leftHip)
            torsoAngle <= 60f && (leftShoulder.y < leftHip.y - 0.06f)
        } else if (rightShoulder.visibility > MIN_LANDMARK_VISIBILITY && rightHip.visibility > MIN_LANDMARK_VISIBILITY) {
            val torsoAngle = JointAngleCalculator.calculateVerticalAngle(rightShoulder, rightHip)
            torsoAngle <= 60f && (rightShoulder.y < rightHip.y - 0.06f)
        } else true

        val isSquatStanceValid = isKneeOffFloor &&
                isKneeHeightSymmetric &&
                isFeetGrounded &&
                isHipLevelSymmetric &&
                isTorsoUpright

        // Upright standing calibration
        val isStandingPosture = if (bothLegsVisible) {
            smoothedLeftKnee >= STANDING_KNEE_THRESHOLD &&
                    smoothedRightKnee >= STANDING_KNEE_THRESHOLD &&
                    isFeetGrounded &&
                    isTorsoUpright
        } else {
            effectiveKneeAngle >= STANDING_KNEE_THRESHOLD && isTorsoUpright
        }

        if (currentState == RepMovementState.WAITING_FOR_UP && isStandingPosture) {
            standingKneeBaseline = (standingKneeBaseline * 0.90f) + (effectiveKneeAngle * 0.10f)
            standingHipYBaseline = if (standingHipYBaseline == null) {
                currentHipY
            } else {
                (standingHipYBaseline!! * 0.90f) + (currentHipY * 0.10f)
            }
            standingLegLengthBaseline = (standingLegLengthBaseline * 0.90f) + (currentLegLength * 0.10f)
        }

        val activeBaseline = if (currentState == RepMovementState.WAITING_FOR_UP) standingKneeBaseline else repBaseline
        val activeHipBaseline = if (currentState == RepMovementState.WAITING_FOR_UP) (standingHipYBaseline ?: currentHipY) else repHipYBaseline
        val activeLegLength = if (currentState == RepMovementState.WAITING_FOR_UP) standingLegLengthBaseline else repLegLengthBaseline

        // ROM and hip displacement metrics (MediaPipe coordinates: moving down increases Y)
        val currentMovementAmplitude = max(0f, activeBaseline - effectiveKneeAngle)
        val hipDescent = max(0f, currentHipY - activeHipBaseline)
        val normalizedHipDescent = if (activeLegLength > 0.05f) (hipDescent / activeLegLength) else 0f

        // Bilateral flexion requirement (both knees must participate)
        val isBilateralValid = !bothLegsVisible || (
                smoothedLeftKnee <= 135f &&
                smoothedRightKnee <= 135f &&
                kneeAsymmetry <= MAX_KNEE_ASYMMETRY
        )

        var isRepCompleted = false
        var feedbackCue: String? = null

        // TFLite classification signals (corroborating evidence)
        val isTfliteDownConfident = tflitePrediction?.let {
            (it.topClass == ExercisePoseClass.SQUATS_DOWN && it.topConfidence >= 0.35f) ||
                    it.probabilities[8] >= 0.25f
        } ?: false

        // Squat Depth Validation (Hybrid: Geometric Evidence + TFLite Corroboration)
        // 1. Strong Geometric Depth: Definitive depth requiring NO TFLite agreement (e.g. back-facing squats)
        val isStrongGeometricDepth = isSquatStanceValid &&
                isBilateralValid &&
                effectiveKneeAngle <= HYSTERESIS_ENTRY_MAX_KNEE &&
                currentMovementAmplitude >= MIN_SQUAT_AMPLITUDE_DELTA &&
                normalizedHipDescent >= MIN_HIP_DESCENT_RATIO

        // 2. Corroborated Depth: Borderline depth supported by confident TFLite squats_down
        val isCorroboratedDepth = isTfliteDownConfident &&
                isSquatStanceValid &&
                isBilateralValid &&
                effectiveKneeAngle <= CORROBORATED_MAX_KNEE &&
                currentMovementAmplitude >= CORROBORATED_MIN_AMPLITUDE &&
                normalizedHipDescent >= CORROBORATED_MIN_HIP_DESCENT

        val isValidSquatDepth = isStrongGeometricDepth || isCorroboratedDepth

        // State Machine Execution
        when (currentState) {
            RepMovementState.WAITING_FOR_UP -> {
                minKneeInRep = 180f
                maxMovementAmplitudeInRep = 0f
                maxDescentRatioInRep = 0f

                if (cooldownFramesRemaining > 0) {
                    cooldownFramesRemaining--
                }

                // Descent candidate check: requires knee drop AND hip descent AND valid stance
                val isInitiatingDescent = currentMovementAmplitude >= CANDIDATE_DESCENT_DELTA &&
                        normalizedHipDescent >= CANDIDATE_HIP_DESCENT_RATIO &&
                        isSquatStanceValid &&
                        (!bothLegsVisible || kneeAsymmetry <= MAX_KNEE_ASYMMETRY + 5f)

                if (cooldownFramesRemaining == 0 && isInitiatingDescent) {
                    consecutiveFramesInState++
                    if (consecutiveFramesInState >= REQUIRED_CONSECUTIVE_FRAMES) {
                        currentState = RepMovementState.DOWN_CANDIDATE
                        repBaseline = standingKneeBaseline // Freeze baselines for this rep cycle
                        repHipYBaseline = standingHipYBaseline ?: currentHipY
                        repLegLengthBaseline = standingLegLengthBaseline
                        minKneeInRep = effectiveKneeAngle
                        maxMovementAmplitudeInRep = currentMovementAmplitude
                        maxDescentRatioInRep = normalizedHipDescent
                        consecutiveFramesInState = 0
                        feedbackCue = "Squatting down"
                    }
                } else {
                    consecutiveFramesInState = 0
                    feedbackCue = if (cooldownFramesRemaining > 0) "Good rep!" else "Ready — squat down"
                }
            }

            RepMovementState.DOWN_CANDIDATE -> {
                minKneeInRep = min(minKneeInRep, effectiveKneeAngle)
                maxMovementAmplitudeInRep = max(maxMovementAmplitudeInRep, currentMovementAmplitude)
                maxDescentRatioInRep = max(maxDescentRatioInRep, normalizedHipDescent)

                if (isValidSquatDepth) {
                    consecutiveFramesInState++
                    if (consecutiveFramesInState >= MIN_CONSECUTIVE_DOWN_FRAMES) {
                        currentState = RepMovementState.VALID_DOWN
                        consecutiveFramesInState = 0
                        feedbackCue = "Good depth — drive up"
                    }
                } else if (currentMovementAmplitude <= 12f || effectiveKneeAngle >= repBaseline - 12f || normalizedHipDescent <= 0.025f) {
                    // Aborted descent / shallow twitch -> reset safely to WAITING_FOR_UP with 0 reps!
                    consecutiveFramesInState++
                    if (consecutiveFramesInState >= REQUIRED_CONSECUTIVE_FRAMES) {
                        currentState = RepMovementState.WAITING_FOR_UP
                        consecutiveFramesInState = 0
                        feedbackCue = "Squat deeper to count a full rep"
                    }
                } else {
                    consecutiveFramesInState = 0
                    feedbackCue = when {
                        !isFeetGrounded -> "Keep both feet grounded"
                        !isKneeOffFloor -> "Keep knees off the floor"
                        !isKneeHeightSymmetric -> "Squat with both legs symmetrically"
                        !isTorsoUpright -> "Keep chest up"
                        kneeAsymmetry > MAX_KNEE_ASYMMETRY -> "Bend both knees together"
                        normalizedHipDescent < MIN_HIP_DESCENT_RATIO -> "Lower your hips down"
                        else -> "Squatting down"
                    }
                }
            }

            RepMovementState.VALID_DOWN -> {
                minKneeInRep = min(minKneeInRep, effectiveKneeAngle)
                maxMovementAmplitudeInRep = max(maxMovementAmplitudeInRep, currentMovementAmplitude)
                maxDescentRatioInRep = max(maxDescentRatioInRep, normalizedHipDescent)

                // Ascent hysteresis: Knee must rise by at least 15° from bottom AND exceed 118°
                val isAscentStarted = effectiveKneeAngle >= (minKneeInRep + HYSTERESIS_EXIT_MIN_ASCENT_DELTA) &&
                        effectiveKneeAngle > 118f

                if (isAscentStarted) {
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
                // Must return close to the original standing baseline (knees + hips)
                val isReturnedToStanding = effectiveKneeAngle >= (repBaseline - RETURN_UP_DELTA_FROM_BASELINE) &&
                        (!bothLegsVisible || (smoothedLeftKnee >= 150f && smoothedRightKnee >= 150f)) &&
                        normalizedHipDescent <= RETURN_UP_MAX_HIP_DESCENT &&
                        isFeetGrounded

                if (isReturnedToStanding) {
                    consecutiveFramesInState++
                    if (consecutiveFramesInState >= REQUIRED_CONSECUTIVE_FRAMES) {
                        // Complete valid rep cycle!
                        totalReps++
                        isRepCompleted = true
                        currentState = RepMovementState.REP_COMPLETED
                        consecutiveFramesInState = 0
                        feedbackCue = "Rep $totalReps counted!"
                    }
                } else if (isValidSquatDepth) {
                    // Dropped back to bottom before standing
                    currentState = RepMovementState.VALID_DOWN
                    consecutiveFramesInState = 0
                } else {
                    consecutiveFramesInState = 0
                    feedbackCue = "Stand all the way up"
                }
            }

            RepMovementState.REP_COMPLETED -> {
                currentState = RepMovementState.COOLDOWN
                cooldownFramesRemaining = COOLDOWN_FRAMES_AFTER_REP
                consecutiveFramesInState = 0
                feedbackCue = "Rep $totalReps complete!"
            }

            RepMovementState.COOLDOWN -> {
                if (cooldownFramesRemaining > 0) {
                    cooldownFramesRemaining--
                }
                val isStandingUp = effectiveKneeAngle >= (standingKneeBaseline - RETURN_UP_DELTA_FROM_BASELINE) &&
                        normalizedHipDescent <= RETURN_UP_MAX_HIP_DESCENT
                if (cooldownFramesRemaining == 0 && isStandingUp) {
                    currentState = RepMovementState.WAITING_FOR_UP
                    consecutiveFramesInState = 0
                }
                feedbackCue = "Rep $totalReps complete"
            }
        }

        return RepValidationResult(
            state = currentState,
            repCount = totalReps,
            isRepCompleted = isRepCompleted,
            leftKneeAngle = smoothedLeftKnee,
            rightKneeAngle = smoothedRightKnee,
            effectiveKneeAngle = effectiveKneeAngle,
            standingBaseline = standingKneeBaseline,
            movementAmplitude = currentMovementAmplitude,
            feedbackCue = feedbackCue,
            hipDescent = hipDescent,
            normalizedHipDescent = normalizedHipDescent,
            legLength = activeLegLength,
            isBilateralValid = isBilateralValid,
            isFeetGrounded = isFeetGrounded
        )
    }

    private fun handleLostLandmarks() {
        lostFramesCount++
        if (lostFramesCount >= 4) {
            currentState = RepMovementState.WAITING_FOR_UP
            consecutiveFramesInState = 0
            cooldownFramesRemaining = 0
            minKneeInRep = 180f
            maxMovementAmplitudeInRep = 0f
            maxDescentRatioInRep = 0f
        }
    }

    override fun reset() {
        currentState = RepMovementState.WAITING_FOR_UP
        totalReps = 0
        smoothedLeftKnee = 175f
        smoothedRightKnee = 175f
        standingKneeBaseline = 175f
        repBaseline = 175f
        minKneeInRep = 180f
        maxMovementAmplitudeInRep = 0f
        standingHipYBaseline = null
        standingLegLengthBaseline = 0.5f
        repHipYBaseline = 0.5f
        repLegLengthBaseline = 0.5f
        maxDescentRatioInRep = 0f
        consecutiveFramesInState = 0
        cooldownFramesRemaining = 0
        lostFramesCount = 0
    }
}
