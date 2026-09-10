package com.app.pose.domain.analysis

import com.app.pose.domain.model.PoseLandmarkIndices
import com.app.pose.domain.model.PosePoint

class SquatAnalyzer : ExerciseAnalyzer<SquatAnalysisResult> {

    private var currentPhase: SquatPhase = SquatPhase.STARTING_POSITION
    private var totalReps: Int = 0
    private var validReps: Int = 0
    private var lowestKneeAngleInRep: Float = 180f
    private var smoothedKneeAngle: Float = 180f
    private var repDepthReached: Boolean = false

    // Thresholds
    companion object {
        const val STANDING_THRESHOLD = 152f
        const val DESCENDING_THRESHOLD = 142f
        const val BOTTOM_THRESHOLD = 105f
        const val SHALLOW_MAX_THRESHOLD = 130f
        const val MIN_LANDMARK_VISIBILITY = 0.40f
    }

    override fun analyze(landmarks: List<PosePoint>, timestampMs: Long): SquatAnalysisResult {
        if (landmarks.size < 33) {
            return SquatAnalysisResult(
                phase = SquatPhase.STARTING_POSITION,
                isStartingPositionValid = false,
                feedbackCue = "Position yourself in frame",
                repCount = totalReps,
                validRepCount = validReps,
                formScore = calculateFormScore()
            )
        }

        val leftShoulder = landmarks[PoseLandmarkIndices.LEFT_SHOULDER]
        val rightShoulder = landmarks[PoseLandmarkIndices.RIGHT_SHOULDER]
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
            return SquatAnalysisResult(
                phase = SquatPhase.STARTING_POSITION,
                isStartingPositionValid = false,
                feedbackCue = "Step back so your full body is visible",
                repCount = totalReps,
                validRepCount = validReps,
                formScore = calculateFormScore()
            )
        }

        // Calculate knee angles
        val leftKneeAngle = if (isLeftLegVisible) {
            JointAngleCalculator.calculateAngle(leftHip, leftKnee, leftAnkle)
        } else 180f

        val rightKneeAngle = if (isRightLegVisible) {
            JointAngleCalculator.calculateAngle(rightHip, rightKnee, rightAnkle)
        } else 180f

        val rawAvgKneeAngle = when {
            isLeftLegVisible && isRightLegVisible -> (leftKneeAngle + rightKneeAngle) / 2f
            isLeftLegVisible -> leftKneeAngle
            else -> rightKneeAngle
        }

        // Exponential smoothing to eliminate jitter
        smoothedKneeAngle = (smoothedKneeAngle * 0.65f) + (rawAvgKneeAngle * 0.35f)

        // Calculate hip angles
        val leftHipAngle = if (isLeftLegVisible && leftShoulder.visibility > MIN_LANDMARK_VISIBILITY) {
            JointAngleCalculator.calculateAngle(leftShoulder, leftHip, leftKnee)
        } else 180f

        val rightHipAngle = if (isRightLegVisible && rightShoulder.visibility > MIN_LANDMARK_VISIBILITY) {
            JointAngleCalculator.calculateAngle(rightShoulder, rightHip, rightKnee)
        } else 180f

        val avgHipAngle = (leftHipAngle + rightHipAngle) / 2f

        // Calculate torso alignment
        val torsoAngle = if (leftShoulder.visibility > MIN_LANDMARK_VISIBILITY && leftHip.visibility > MIN_LANDMARK_VISIBILITY) {
            JointAngleCalculator.calculateVerticalAngle(leftShoulder, leftHip)
        } else 0f

        // Depth determination
        val depth = when {
            smoothedKneeAngle <= 85f -> SquatDepth.DEEP
            smoothedKneeAngle <= BOTTOM_THRESHOLD -> SquatDepth.PARALLEL
            smoothedKneeAngle <= SHALLOW_MAX_THRESHOLD -> SquatDepth.SHALLOW
            else -> SquatDepth.NONE
        }

        var isRepCompleted = false
        var isRepValid = true
        var feedbackCue: String? = null

        // State Machine
        when (currentPhase) {
            SquatPhase.STARTING_POSITION, SquatPhase.STANDING -> {
                if (smoothedKneeAngle >= STANDING_THRESHOLD) {
                    currentPhase = SquatPhase.STANDING
                    feedbackCue = "Ready — start your squat"
                } else if (smoothedKneeAngle < DESCENDING_THRESHOLD) {
                    currentPhase = SquatPhase.DESCENDING
                    lowestKneeAngleInRep = smoothedKneeAngle
                    repDepthReached = false
                    feedbackCue = "Squatting down"
                }
            }

            SquatPhase.DESCENDING -> {
                lowestKneeAngleInRep = minOf(lowestKneeAngleInRep, smoothedKneeAngle)

                if (smoothedKneeAngle <= BOTTOM_THRESHOLD) {
                    currentPhase = SquatPhase.BOTTOM
                    repDepthReached = true
                    feedbackCue = "Good depth — drive back up"
                } else if (smoothedKneeAngle > lowestKneeAngleInRep + 12f && lowestKneeAngleInRep <= SHALLOW_MAX_THRESHOLD) {
                    // Ascending prematurely without reaching parallel depth
                    currentPhase = SquatPhase.ASCENDING
                    feedbackCue = "Driving up"
                }
            }

            SquatPhase.BOTTOM -> {
                lowestKneeAngleInRep = minOf(lowestKneeAngleInRep, smoothedKneeAngle)

                if (smoothedKneeAngle > BOTTOM_THRESHOLD + 8f) {
                    currentPhase = SquatPhase.ASCENDING
                    feedbackCue = "Drive through your heels"
                }
            }

            SquatPhase.ASCENDING -> {
                if (smoothedKneeAngle >= STANDING_THRESHOLD) {
                    isRepCompleted = true
                    totalReps++

                    if (repDepthReached && lowestKneeAngleInRep <= BOTTOM_THRESHOLD + 5f) {
                        validReps++
                        isRepValid = true
                        feedbackCue = "Rep $totalReps counted"
                    } else {
                        isRepValid = false
                        feedbackCue = "Go slightly lower on next rep"
                    }

                    currentPhase = SquatPhase.STANDING
                }
            }
        }

        return SquatAnalysisResult(
            phase = currentPhase,
            leftKneeAngle = leftKneeAngle,
            rightKneeAngle = rightKneeAngle,
            avgKneeAngle = smoothedKneeAngle,
            leftHipAngle = leftHipAngle,
            rightHipAngle = rightHipAngle,
            avgHipAngle = avgHipAngle,
            torsoAngle = torsoAngle,
            depth = depth,
            isStartingPositionValid = true,
            isRepCompleted = isRepCompleted,
            isRepValid = isRepValid,
            feedbackCue = feedbackCue,
            repCount = totalReps,
            validRepCount = validReps,
            formScore = calculateFormScore()
        )
    }

    private fun calculateFormScore(): Int {
        return if (totalReps > 0) {
            (validReps * 100) / totalReps
        } else {
            100
        }
    }

    override fun reset() {
        currentPhase = SquatPhase.STARTING_POSITION
        totalReps = 0
        validReps = 0
        lowestKneeAngleInRep = 180f
        smoothedKneeAngle = 180f
        repDepthReached = false
    }
}
