package com.app.pose.analysis

import com.app.pose.domain.analysis.JointAngleCalculator
import com.app.pose.domain.analysis.SquatAnalyzer
import com.app.pose.domain.analysis.SquatDepth
import com.app.pose.domain.analysis.SquatPhase
import com.app.pose.domain.model.PosePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SquatAnalyzerTest {

    private lateinit var analyzer: SquatAnalyzer

    @Before
    fun setup() {
        analyzer = SquatAnalyzer()
    }

    @Test
    fun testJointAngleCalculator_rightAngle() {
        val a = PosePoint(0f, 1f)
        val b = PosePoint(0f, 0f)
        val c = PosePoint(1f, 0f)

        val angle = JointAngleCalculator.calculateAngle(a, b, c)
        assertEquals(90f, angle, 0.5f)
    }

    @Test
    fun testJointAngleCalculator_straightLine() {
        val a = PosePoint(0f, 1f)
        val b = PosePoint(0f, 2f)
        val c = PosePoint(0f, 3f)

        val angle = JointAngleCalculator.calculateAngle(a, b, c)
        assertEquals(180f, angle, 0.5f)
    }

    @Test
    fun testOneKneeBendWhileOtherLegStraight_producesZeroReps() {
        val standingPose = createMockSquatPose(leftKneeAngleDeg = 175f, rightKneeAngleDeg = 175f)
        // User stands on right leg and bends left knee completely to 45 deg
        val oneKneeBentPose = createMockSquatPose(leftKneeAngleDeg = 45f, rightKneeAngleDeg = 175f, hipDrop = 0.01f)

        // Initialize standing
        for (i in 1..6) {
            analyzer.analyze(standingPose, i * 100L)
        }

        // Hold single knee bent
        for (i in 7..20) {
            analyzer.analyze(oneKneeBentPose, i * 100L)
        }

        // Return to standing
        var result = analyzer.analyze(standingPose, 2100L)
        for (i in 22..30) {
            result = analyzer.analyze(standingPose, i * 100L)
        }

        // MUST be 0 reps!
        assertEquals(0, result.repCount)
        assertEquals(0, result.validRepCount)
        assertEquals(SquatPhase.STANDING, result.phase)
    }

    @Test
    fun testSmallKneeMovement_doesNotIncrementRep() {
        val standingPose = createMockSquatPose(leftKneeAngleDeg = 175f, rightKneeAngleDeg = 175f)
        val slightMovePose = createMockSquatPose(leftKneeAngleDeg = 148f, rightKneeAngleDeg = 148f, hipDrop = 0.02f)

        // Initialize standing
        for (i in 1..6) {
            analyzer.analyze(standingPose, i * 100L)
        }

        // Slight twitch / small knee movement
        for (i in 7..14) {
            analyzer.analyze(slightMovePose, i * 100L)
        }

        // Return to standing
        var result = analyzer.analyze(standingPose, 1500L)
        for (i in 16..22) {
            result = analyzer.analyze(standingPose, i * 100L)
        }

        assertEquals(0, result.repCount)
        assertEquals(SquatPhase.STANDING, result.phase)
    }

    @Test
    fun testPartialKneeBend_doesNotIncrementRep() {
        val standingPose = createMockSquatPose(leftKneeAngleDeg = 175f, rightKneeAngleDeg = 175f)
        val partialBendPose = createMockSquatPose(leftKneeAngleDeg = 125f, rightKneeAngleDeg = 125f, hipDrop = 0.04f)

        // Initialize standing
        for (i in 1..6) {
            analyzer.analyze(standingPose, i * 100L)
        }

        // Partial descent
        for (i in 7..14) {
            analyzer.analyze(partialBendPose, i * 100L)
        }

        // Ascend back to standing without reaching bottom
        var result = analyzer.analyze(standingPose, 1500L)
        for (i in 16..24) {
            result = analyzer.analyze(standingPose, i * 100L)
        }

        assertEquals(0, result.repCount)
        assertEquals(SquatPhase.STANDING, result.phase)
    }

    @Test
    fun testValidSquatCycle_completesExactlyOneRep() {
        val standingPose = createMockSquatPose(leftKneeAngleDeg = 175f, rightKneeAngleDeg = 175f, hipDrop = 0f)
        val descendingPose = createMockSquatPose(leftKneeAngleDeg = 130f, rightKneeAngleDeg = 130f, hipDrop = 0.06f)
        val bottomPose = createMockSquatPose(leftKneeAngleDeg = 88f, rightKneeAngleDeg = 88f, hipDrop = 0.15f)
        val ascendingPose = createMockSquatPose(leftKneeAngleDeg = 135f, rightKneeAngleDeg = 135f, hipDrop = 0.06f)

        // 1. Standing
        var result = analyzer.analyze(standingPose, 0L)
        for (i in 1..6) {
            result = analyzer.analyze(standingPose, i * 100L)
        }
        assertEquals(SquatPhase.STANDING, result.phase)
        assertEquals(0, result.repCount)

        // 2. Descending
        for (i in 7..14) {
            result = analyzer.analyze(descendingPose, i * 100L)
        }
        assertEquals(SquatPhase.DESCENDING, result.phase)

        // 3. Bottom reached (parallel/deep)
        for (i in 15..22) {
            result = analyzer.analyze(bottomPose, i * 100L)
        }
        assertEquals(SquatPhase.BOTTOM, result.phase)
        assertEquals(SquatDepth.PARALLEL, result.depth)

        // 4. Ascending
        for (i in 23..30) {
            result = analyzer.analyze(ascendingPose, i * 100L)
        }
        assertEquals(SquatPhase.ASCENDING, result.phase)

        // 5. Back to standing (Completed Rep)
        for (i in 31..40) {
            result = analyzer.analyze(standingPose, i * 100L)
        }

        assertEquals(1, result.repCount)
        assertEquals(1, result.validRepCount)
        assertEquals(100, result.formScore)
        assertTrue(result.isStartingPositionValid)
        assertEquals(SquatPhase.STANDING, result.phase)
    }

    @Test
    fun testSquatHeldAtBottom_doesNotIncrementAdditionalReps() {
        val standingPose = createMockSquatPose(leftKneeAngleDeg = 175f, rightKneeAngleDeg = 175f, hipDrop = 0f)
        val descendingPose = createMockSquatPose(leftKneeAngleDeg = 130f, rightKneeAngleDeg = 130f, hipDrop = 0.06f)
        val bottomPose = createMockSquatPose(leftKneeAngleDeg = 85f, rightKneeAngleDeg = 85f, hipDrop = 0.15f)

        for (i in 1..6) {
            analyzer.analyze(standingPose, i * 100L)
        }
        for (i in 7..14) {
            analyzer.analyze(descendingPose, i * 100L)
        }

        // Hold at bottom for 50 frames
        var result = analyzer.analyze(bottomPose, 1500L)
        for (i in 16..65) {
            result = analyzer.analyze(bottomPose, i * 100L)
        }

        assertEquals(SquatPhase.BOTTOM, result.phase)
        assertEquals(0, result.repCount)
    }

    @Test
    fun testTwoCompleteSquats_incrementsExactlyTwoReps() {
        val standingPose = createMockSquatPose(leftKneeAngleDeg = 175f, rightKneeAngleDeg = 175f, hipDrop = 0f)
        val descendingPose = createMockSquatPose(leftKneeAngleDeg = 130f, rightKneeAngleDeg = 130f, hipDrop = 0.06f)
        val bottomPose = createMockSquatPose(leftKneeAngleDeg = 88f, rightKneeAngleDeg = 88f, hipDrop = 0.15f)
        val ascendingPose = createMockSquatPose(leftKneeAngleDeg = 135f, rightKneeAngleDeg = 135f, hipDrop = 0.06f)

        // Rep 1
        for (i in 1..5) analyzer.analyze(standingPose, i * 100L)
        for (i in 6..12) analyzer.analyze(descendingPose, i * 100L)
        for (i in 13..20) analyzer.analyze(bottomPose, i * 100L)
        for (i in 21..28) analyzer.analyze(ascendingPose, i * 100L)
        for (i in 29..36) analyzer.analyze(standingPose, i * 100L)

        assertEquals(1, analyzer.analyze(standingPose, 3700L).repCount)

        // Rep 2
        for (i in 38..44) analyzer.analyze(descendingPose, i * 100L)
        for (i in 45..52) analyzer.analyze(bottomPose, i * 100L)
        for (i in 53..60) analyzer.analyze(ascendingPose, i * 100L)
        var result = analyzer.analyze(standingPose, 6100L)
        for (i in 62..70) result = analyzer.analyze(standingPose, i * 100L)

        assertEquals(2, result.repCount)
        assertEquals(2, result.validRepCount)
    }

    @Test
    fun testLandmarkLossDuringMovement_doesNotCreateRep() {
        val standingPose = createMockSquatPose(leftKneeAngleDeg = 175f, rightKneeAngleDeg = 175f, hipDrop = 0f)
        val descendingPose = createMockSquatPose(leftKneeAngleDeg = 130f, rightKneeAngleDeg = 130f, hipDrop = 0.06f)
        val bottomPose = createMockSquatPose(leftKneeAngleDeg = 88f, rightKneeAngleDeg = 88f, hipDrop = 0.15f)
        val occludedPose = MutableList(33) { PosePoint(0.5f, 0.5f, 0f, 0.1f) }

        for (i in 1..5) analyzer.analyze(standingPose, i * 100L)
        for (i in 6..12) analyzer.analyze(descendingPose, i * 100L)
        for (i in 13..20) analyzer.analyze(bottomPose, i * 100L)

        // Landmarks lost during ascent
        for (i in 21..30) {
            analyzer.analyze(occludedPose, i * 100L)
        }

        // Return to standing
        var result = analyzer.analyze(standingPose, 3100L)
        for (i in 32..40) {
            result = analyzer.analyze(standingPose, i * 100L)
        }

        assertEquals(0, result.repCount)
    }

    private fun createMockSquatPose(
        leftKneeAngleDeg: Float = 175f,
        rightKneeAngleDeg: Float = 175f,
        hipDrop: Float = 0f
    ): List<PosePoint> {
        val list = MutableList(33) { PosePoint(0.5f, 0.5f, 0f, 0.95f) }

        // Hip base Y at 0.2 + hipDrop
        val hipXLeft = 0.45f
        val hipXRight = 0.55f
        val hipY = 0.2f + hipDrop
        list[23] = PosePoint(hipXLeft, hipY, 0f, 0.95f)
        list[24] = PosePoint(hipXRight, hipY, 0f, 0.95f)

        // Ankle at (0.45, 0.8) and (0.55, 0.8)
        val ankleY = 0.8f
        list[27] = PosePoint(hipXLeft, ankleY, 0f, 0.95f)
        list[28] = PosePoint(hipXRight, ankleY, 0f, 0.95f)

        // Knee positions calculated from angles
        val kneeY = (hipY + ankleY) / 2f
        val legSpanHalf = (ankleY - hipY) / 2f

        val clampedLeft = leftKneeAngleDeg.coerceIn(45f, 180f)
        val leftHalfRad = Math.toRadians(((180.0 - clampedLeft) / 2.0))
        val leftOffset = (legSpanHalf * Math.tan(leftHalfRad)).toFloat()

        val clampedRight = rightKneeAngleDeg.coerceIn(45f, 180f)
        val rightHalfRad = Math.toRadians(((180.0 - clampedRight) / 2.0))
        val rightOffset = (legSpanHalf * Math.tan(rightHalfRad)).toFloat()

        list[25] = PosePoint(hipXLeft - leftOffset, kneeY, 0f, 0.95f)
        list[26] = PosePoint(hipXRight - rightOffset, kneeY, 0f, 0.95f)

        // Shoulders
        val shoulderY = hipY - 0.1f
        list[11] = PosePoint(0.45f, shoulderY, 0f, 0.95f)
        list[12] = PosePoint(0.55f, shoulderY, 0f, 0.95f)

        return list
    }
}
