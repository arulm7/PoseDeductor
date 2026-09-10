package com.app.pose.domain.analysis

import com.app.pose.domain.model.PoseLandmarkIndices
import com.app.pose.domain.model.PosePoint
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class SquatAnalyzer : ExerciseAnalyzer<SquatAnalysisResult> {

    private var currentPhase: SquatPhase = SquatPhase.STARTING_POSITION
    private var totalReps: Int = 0
    private var validReps: Int = 0
    private var lowestKneeAngleInRep: Float = 180f
    private var hasReachedBottom: Boolean = false
    private var consecutiveFramesInPhase: Int = 0
    private var lostFramesCount: Int = 0

    // Smoothed bilateral joint angles
    private var smoothedLeftKnee: Float = 180f
    private var smoothedRightKnee: Float = 180f
    private var smoothedLeftHip: Float = 180f
    private var smoothedRightHip: Float = 180f

    // Vertical displacement tracking
    private var standingHipYBaseline: Float? = null
    private var standingLegLengthBaseline: Float = 0.5f
    private var maxDescentRatioInRep: Float = 0f

    companion object {
        const val STANDING_KNEE_THRESHOLD = 155f
        const val STANDING_HIP_THRESHOLD = 145f

        const val DESCENDING_KNEE_THRESHOLD = 140f

        const val BOTTOM_KNEE_THRESHOLD = 110f
        const val BOTTOM_HIP_THRESHOLD = 150f

        const val ASCENDING_MIN_KNEE_THRESHOLD = 118f

        const val MIN_HIP_DESCENT_RATIO = 0.06f
        const val MAX_KNEE_ASYMMETRY_BOTTOM = 40f

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

        // Calculate individual knee angles
        val rawLeftKneeAngle = if (isLeftLegVisible) {
            JointAngleCalculator.calculateAngle(leftHip, leftKnee, leftAnkle)
        } else 180f

        val rawRightKneeAngle = if (isRightLegVisible) {
            JointAngleCalculator.calculateAngle(rightHip, rightKnee, rightAnkle)
        } else 180f

        // Calculate individual hip angles
        val rawLeftHipAngle = if (isLeftLegVisible && leftShoulder.visibility > MIN_LANDMARK_VISIBILITY) {
            JointAngleCalculator.calculateAngle(leftShoulder, leftHip, leftKnee)
        } else 180f

        val rawRightHipAngle = if (isRightLegVisible && rightShoulder.visibility > MIN_LANDMARK_VISIBILITY) {
            JointAngleCalculator.calculateAngle(rightShoulder, rightHip, rightKnee)
        } else 180f

        // Exponential smoothing per joint to preserve bilateral asymmetry
        smoothedLeftKnee = (smoothedLeftKnee * 0.60f) + (rawLeftKneeAngle * 0.40f)
        smoothedRightKnee = (smoothedRightKnee * 0.60f) + (rawRightKneeAngle * 0.40f)
        smoothedLeftHip = (smoothedLeftHip * 0.60f) + (rawLeftHipAngle * 0.40f)
        smoothedRightHip = (smoothedRightHip * 0.60f) + (rawRightHipAngle * 0.40f)

        // Effective bilateral evaluation
        val bothLegsVisible = isLeftLegVisible && isRightLegVisible
        val activeKneeAngle = if (bothLegsVisible) {
            max(smoothedLeftKnee, smoothedRightKnee) // Both knees must flex for depth
        } else if (isLeftLegVisible) smoothedLeftKnee else smoothedRightKnee

        val minKneeAngle = if (bothLegsVisible) {
            min(smoothedLeftKnee, smoothedRightKnee)
        } else if (isLeftLegVisible) smoothedLeftKnee else smoothedRightKnee

        val activeHipAngle = if (bothLegsVisible) {
            max(smoothedLeftHip, smoothedRightHip) // Both hips must flex
        } else if (isLeftLegVisible) smoothedLeftHip else smoothedRightHip

        val avgKneeAngle = (smoothedLeftKnee + smoothedRightKnee) / 2f
        val avgHipAngle = (smoothedLeftHip + smoothedRightHip) / 2f

        // Vertical hip displacement relative to body scale
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

        // Bilateral check: are both knees and hips standing tall?
        val isBothLegsStanding = smoothedLeftKnee >= STANDING_KNEE_THRESHOLD &&
                smoothedRightKnee >= STANDING_KNEE_THRESHOLD &&
                smoothedLeftHip >= STANDING_HIP_THRESHOLD &&
                smoothedRightHip >= STANDING_HIP_THRESHOLD

        // Update standing baseline only when truly standing
        if (isBothLegsStanding) {
            standingLegLengthBaseline = currentLegLength
            standingHipYBaseline = if (standingHipYBaseline == null) {
                currentHipY
            } else {
                (standingHipYBaseline!! * 0.85f) + (currentHipY * 0.15f)
            }
        }

        val baselineHipY = standingHipYBaseline ?: currentHipY
        // In normalized coordinates, descent moves downward (increasing Y)
        val normalizedHipDescent = if (standingLegLengthBaseline > 0.05f) {
            (currentHipY - baselineHipY) / standingLegLengthBaseline
        } else 0f

        // Calculate torso alignment
        val torsoAngle = if (leftShoulder.visibility > MIN_LANDMARK_VISIBILITY && leftHip.visibility > MIN_LANDMARK_VISIBILITY) {
            JointAngleCalculator.calculateVerticalAngle(leftShoulder, leftHip)
        } else 0f

        // Depth determination based on bilateral knee flexion
        val depth = when {
            activeKneeAngle <= 85f -> SquatDepth.DEEP
            activeKneeAngle <= BOTTOM_KNEE_THRESHOLD -> SquatDepth.PARALLEL
            activeKneeAngle <= 130f -> SquatDepth.SHALLOW
            else -> SquatDepth.NONE
        }

        var isRepCompleted = false
        var feedbackCue: String? = null

        // Bilateral check: did both knees descend?
        val isBothLegsDescending = smoothedLeftKnee < DESCENDING_KNEE_THRESHOLD &&
                smoothedRightKnee < DESCENDING_KNEE_THRESHOLD

        // Bilateral check: did both knees and hips reach valid bottom depth?
        val kneeAsymmetry = abs(smoothedLeftKnee - smoothedRightKnee)
        val isBothLegsAtBottom = smoothedLeftKnee <= BOTTOM_KNEE_THRESHOLD &&
                smoothedRightKnee <= BOTTOM_KNEE_THRESHOLD &&
                smoothedLeftHip <= BOTTOM_HIP_THRESHOLD &&
                smoothedRightHip <= BOTTOM_HIP_THRESHOLD &&
                (!bothLegsVisible || kneeAsymmetry <= MAX_KNEE_ASYMMETRY_BOTTOM) &&
                normalizedHipDescent >= MIN_HIP_DESCENT_RATIO

        // Bilateral check: are both knees ascending from bottom?
        val isBothLegsAscending = smoothedLeftKnee > ASCENDING_MIN_KNEE_THRESHOLD &&
                smoothedRightKnee > ASCENDING_MIN_KNEE_THRESHOLD &&
                activeKneeAngle > lowestKneeAngleInRep + 8f

        // Robust Phase-based State Machine
        when (currentPhase) {
            SquatPhase.STARTING_POSITION -> {
                if (isBothLegsStanding) {
                    consecutiveFramesInPhase++
                    if (consecutiveFramesInPhase >= REQUIRED_CONSECUTIVE_FRAMES) {
                        currentPhase = SquatPhase.STANDING
                        consecutiveFramesInPhase = 0
                        feedbackCue = "Ready — start your squat"
                    }
                } else {
                    consecutiveFramesInPhase = 0
                    feedbackCue = "Stand tall with both legs straight"
                }
            }

            SquatPhase.STANDING -> {
                lowestKneeAngleInRep = 180f
                hasReachedBottom = false
                maxDescentRatioInRep = 0f

                if (isBothLegsDescending) {
                    consecutiveFramesInPhase++
                    if (consecutiveFramesInPhase >= REQUIRED_CONSECUTIVE_FRAMES) {
                        currentPhase = SquatPhase.DESCENDING
                        lowestKneeAngleInRep = activeKneeAngle
                        consecutiveFramesInPhase = 0
                        feedbackCue = "Squatting down"
                    }
                } else {
                    consecutiveFramesInPhase = 0
                    if (bothLegsVisible && kneeAsymmetry > 35f && minKneeAngle < DESCENDING_KNEE_THRESHOLD) {
                        feedbackCue = "Bend both knees together"
                    } else {
                        feedbackCue = "Ready — squat when you are"
                    }
                }
            }

            SquatPhase.DESCENDING -> {
                lowestKneeAngleInRep = min(lowestKneeAngleInRep, activeKneeAngle)
                maxDescentRatioInRep = max(maxDescentRatioInRep, normalizedHipDescent)

                if (isBothLegsAtBottom) {
                    consecutiveFramesInPhase++
                    if (consecutiveFramesInPhase >= REQUIRED_CONSECUTIVE_FRAMES) {
                        currentPhase = SquatPhase.BOTTOM
                        hasReachedBottom = true
                        consecutiveFramesInPhase = 0
                        feedbackCue = "Good depth — drive up"
                    }
                } else if (isBothLegsStanding) {
                    // Aborted descent or shallow partial bend -> return to standing with NO rep
                    consecutiveFramesInPhase++
                    if (consecutiveFramesInPhase >= REQUIRED_CONSECUTIVE_FRAMES) {
                        currentPhase = SquatPhase.STANDING
                        consecutiveFramesInPhase = 0
                        hasReachedBottom = false
                        feedbackCue = "Squat deeper to count a full rep"
                    }
                } else {
                    consecutiveFramesInPhase = 0
                    if (bothLegsVisible && kneeAsymmetry > 35f) {
                        feedbackCue = "Keep both legs symmetrical"
                    } else {
                        feedbackCue = "Squatting down"
                    }
                }
            }

            SquatPhase.BOTTOM -> {
                lowestKneeAngleInRep = min(lowestKneeAngleInRep, activeKneeAngle)
                maxDescentRatioInRep = max(maxDescentRatioInRep, normalizedHipDescent)

                // Meaningful bilateral ascent from bottom
                if (isBothLegsAscending) {
                    consecutiveFramesInPhase++
                    if (consecutiveFramesInPhase >= REQUIRED_CONSECUTIVE_FRAMES) {
                        currentPhase = SquatPhase.ASCENDING
                        consecutiveFramesInPhase = 0
                        feedbackCue = "Drive through your heels"
                    }
                } else {
                    consecutiveFramesInPhase = 0
                    feedbackCue = "Drive up from the bottom"
                }
            }

            SquatPhase.ASCENDING -> {
                if (isBothLegsStanding) {
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
                        maxDescentRatioInRep = 0f
                    }
                } else if (isBothLegsAtBottom) {
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
            leftKneeAngle = smoothedLeftKnee,
            rightKneeAngle = smoothedRightKnee,
            avgKneeAngle = avgKneeAngle,
            leftHipAngle = smoothedLeftHip,
            rightHipAngle = smoothedRightHip,
            avgHipAngle = avgHipAngle,
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
            maxDescentRatioInRep = 0f
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
        smoothedLeftKnee = 180f
        smoothedRightKnee = 180f
        smoothedLeftHip = 180f
        smoothedRightHip = 180f
        standingHipYBaseline = null
        standingLegLengthBaseline = 0.5f
        maxDescentRatioInRep = 0f
        hasReachedBottom = false
        consecutiveFramesInPhase = 0
        lostFramesCount = 0
    }
}
