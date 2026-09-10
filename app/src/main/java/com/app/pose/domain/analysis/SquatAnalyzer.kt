package com.app.pose.domain.analysis

import com.app.pose.domain.model.PoseLandmarkIndices
import com.app.pose.domain.model.PosePoint

class SquatAnalyzer : ExerciseAnalyzer<SquatAnalysisResult> {

    private var currentPhase: SquatPhase = SquatPhase.STARTING_POSITION
    private var totalReps: Int = 0
    private var validReps: Int = 0
    private var lowestKneeAngleInRep: Float = 180f
    private var smoothedKneeAngle: Float = 180f
    private var smoothedHipAngle: Float = 180f
    private var hasReachedBottom: Boolean = false
    private var consecutiveFramesInPhase: Int = 0
    private var lostFramesCount: Int = 0

    companion object {
        const val STANDING_KNEE_THRESHOLD = 155f
        const val STANDING_HIP_THRESHOLD = 145f

        const val DESCENDING_KNEE_THRESHOLD = 140f

        const val BOTTOM_KNEE_THRESHOLD = 105f
        const val BOTTOM_HIP_THRESHOLD = 115f

        const val ASCENDING_MIN_KNEE_THRESHOLD = 118f

        const val MIN_LANDMARK_VISIBILITY = 0.40f
        const val REQUIRED_CONSECUTIVE_FRAMES = 2
        const val MAX_LOST_FRAMES_TOLERANCE = 4
    }

    override fun analyze(landmarks: List<PosePoint>, timestampMs: Long): SquatAnalysisResult {
        if (landmarks.size < 33) {
            handleLandmarksLost()
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
            handleLandmarksLost()
            return SquatAnalysisResult(
                phase = SquatPhase.STARTING_POSITION,
                isStartingPositionValid = false,
                feedbackCue = "Step back so your full body is visible",
                repCount = totalReps,
                validRepCount = validReps,
                formScore = calculateFormScore()
            )
        }

        lostFramesCount = 0

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

        // Calculate hip angles
        val leftHipAngle = if (isLeftLegVisible && leftShoulder.visibility > MIN_LANDMARK_VISIBILITY) {
            JointAngleCalculator.calculateAngle(leftShoulder, leftHip, leftKnee)
        } else 180f

        val rightHipAngle = if (isRightLegVisible && rightShoulder.visibility > MIN_LANDMARK_VISIBILITY) {
            JointAngleCalculator.calculateAngle(rightShoulder, rightHip, rightKnee)
        } else 180f

        val rawAvgHipAngle = (leftHipAngle + rightHipAngle) / 2f

        // Exponential smoothing
        smoothedKneeAngle = (smoothedKneeAngle * 0.60f) + (rawAvgKneeAngle * 0.40f)
        smoothedHipAngle = (smoothedHipAngle * 0.60f) + (rawAvgHipAngle * 0.40f)

        // Calculate torso alignment
        val torsoAngle = if (leftShoulder.visibility > MIN_LANDMARK_VISIBILITY && leftHip.visibility > MIN_LANDMARK_VISIBILITY) {
            JointAngleCalculator.calculateVerticalAngle(leftShoulder, leftHip)
        } else 0f

        // Depth determination
        val depth = when {
            smoothedKneeAngle <= 85f -> SquatDepth.DEEP
            smoothedKneeAngle <= BOTTOM_KNEE_THRESHOLD -> SquatDepth.PARALLEL
            smoothedKneeAngle <= 130f -> SquatDepth.SHALLOW
            else -> SquatDepth.NONE
        }

        var isRepCompleted = false
        var feedbackCue: String? = null

        // Robust Phase-based State Machine
        when (currentPhase) {
            SquatPhase.STARTING_POSITION -> {
                if (smoothedKneeAngle >= STANDING_KNEE_THRESHOLD && smoothedHipAngle >= STANDING_HIP_THRESHOLD) {
                    consecutiveFramesInPhase++
                    if (consecutiveFramesInPhase >= REQUIRED_CONSECUTIVE_FRAMES) {
                        currentPhase = SquatPhase.STANDING
                        consecutiveFramesInPhase = 0
                        feedbackCue = "Ready — start your squat"
                    }
                } else {
                    consecutiveFramesInPhase = 0
                    feedbackCue = "Stand tall in frame"
                }
            }

            SquatPhase.STANDING -> {
                lowestKneeAngleInRep = 180f
                hasReachedBottom = false

                if (smoothedKneeAngle < DESCENDING_KNEE_THRESHOLD) {
                    consecutiveFramesInPhase++
                    if (consecutiveFramesInPhase >= REQUIRED_CONSECUTIVE_FRAMES) {
                        currentPhase = SquatPhase.DESCENDING
                        lowestKneeAngleInRep = smoothedKneeAngle
                        consecutiveFramesInPhase = 0
                        feedbackCue = "Squatting down"
                    }
                } else {
                    consecutiveFramesInPhase = 0
                    feedbackCue = "Ready — squat when you are"
                }
            }

            SquatPhase.DESCENDING -> {
                lowestKneeAngleInRep = minOf(lowestKneeAngleInRep, smoothedKneeAngle)

                if (smoothedKneeAngle <= BOTTOM_KNEE_THRESHOLD) {
                    consecutiveFramesInPhase++
                    if (consecutiveFramesInPhase >= REQUIRED_CONSECUTIVE_FRAMES) {
                        currentPhase = SquatPhase.BOTTOM
                        hasReachedBottom = true
                        consecutiveFramesInPhase = 0
                        feedbackCue = "Good depth — drive up"
                    }
                } else if (smoothedKneeAngle >= STANDING_KNEE_THRESHOLD) {
                    // Aborted descent / small knee bend without reaching bottom -> reset to standing with NO rep
                    consecutiveFramesInPhase++
                    if (consecutiveFramesInPhase >= REQUIRED_CONSECUTIVE_FRAMES) {
                        currentPhase = SquatPhase.STANDING
                        consecutiveFramesInPhase = 0
                        hasReachedBottom = false
                        feedbackCue = "Go deeper to count the squat"
                    }
                } else {
                    consecutiveFramesInPhase = 0
                    feedbackCue = "Squatting down"
                }
            }

            SquatPhase.BOTTOM -> {
                lowestKneeAngleInRep = minOf(lowestKneeAngleInRep, smoothedKneeAngle)

                // Meaningful ascent from bottom
                if (smoothedKneeAngle > ASCENDING_MIN_KNEE_THRESHOLD && smoothedKneeAngle > lowestKneeAngleInRep + 10f) {
                    consecutiveFramesInPhase++
                    if (consecutiveFramesInPhase >= REQUIRED_CONSECUTIVE_FRAMES) {
                        currentPhase = SquatPhase.ASCENDING
                        consecutiveFramesInPhase = 0
                        feedbackCue = "Drive through your heels"
                    }
                } else {
                    consecutiveFramesInPhase = 0
                    feedbackCue = "Hold and drive up"
                }
            }

            SquatPhase.ASCENDING -> {
                if (smoothedKneeAngle >= STANDING_KNEE_THRESHOLD && smoothedHipAngle >= STANDING_HIP_THRESHOLD) {
                    consecutiveFramesInPhase++
                    if (consecutiveFramesInPhase >= REQUIRED_CONSECUTIVE_FRAMES) {
                        if (hasReachedBottom) {
                            totalReps++
                            validReps++
                            isRepCompleted = true
                            feedbackCue = "Rep $totalReps counted"
                        }
                        currentPhase = SquatPhase.STANDING
                        consecutiveFramesInPhase = 0
                        hasReachedBottom = false
                        lowestKneeAngleInRep = 180f
                    }
                } else if (smoothedKneeAngle <= BOTTOM_KNEE_THRESHOLD) {
                    // Dropped back to bottom
                    currentPhase = SquatPhase.BOTTOM
                    consecutiveFramesInPhase = 0
                } else {
                    consecutiveFramesInPhase = 0
                    feedbackCue = "Stand all the way up"
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
            avgHipAngle = smoothedHipAngle,
            torsoAngle = torsoAngle,
            depth = depth,
            isStartingPositionValid = true,
            isRepCompleted = isRepCompleted,
            isRepValid = true,
            feedbackCue = feedbackCue,
            repCount = totalReps,
            validRepCount = validReps,
            formScore = calculateFormScore()
        )
    }

    private fun handleLandmarksLost() {
        lostFramesCount++
        if (lostFramesCount >= MAX_LOST_FRAMES_TOLERANCE) {
            currentPhase = SquatPhase.STARTING_POSITION
            consecutiveFramesInPhase = 0
            hasReachedBottom = false
            lowestKneeAngleInRep = 180f
        }
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
        smoothedHipAngle = 180f
        hasReachedBottom = false
        consecutiveFramesInPhase = 0
        lostFramesCount = 0
    }
}
