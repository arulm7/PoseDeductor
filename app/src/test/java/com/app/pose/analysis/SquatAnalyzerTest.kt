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
    fun testSmallKneeMovement_doesNotIncrementRep() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f)
        val slightMovePose = createMockSquatPose(kneeAngleDeg = 148f) // Small movement > 140 threshold

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
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f)
        val partialBendPose = createMockSquatPose(kneeAngleDeg = 125f) // Bends but doesn't reach <= 105 bottom

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
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f)
        val descendingPose = createMockSquatPose(kneeAngleDeg = 130f)
        val bottomPose = createMockSquatPose(kneeAngleDeg = 88f)
        val ascendingPose = createMockSquatPose(kneeAngleDeg = 135f)

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
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f)
        val descendingPose = createMockSquatPose(kneeAngleDeg = 130f)
        val bottomPose = createMockSquatPose(kneeAngleDeg = 85f)

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
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f)
        val descendingPose = createMockSquatPose(kneeAngleDeg = 130f)
        val bottomPose = createMockSquatPose(kneeAngleDeg = 88f)
        val ascendingPose = createMockSquatPose(kneeAngleDeg = 135f)

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
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f)
        val descendingPose = createMockSquatPose(kneeAngleDeg = 130f)
        val bottomPose = createMockSquatPose(kneeAngleDeg = 88f)
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

    private fun createMockSquatPose(kneeAngleDeg: Float): List<PosePoint> {
        val list = MutableList(33) { PosePoint(0.5f, 0.5f, 0f, 0.95f) }

        // Hip at (0.5, 0.2)
        val hipX = 0.5f
        val hipY = 0.2f
        list[23] = PosePoint(hipX, hipY, 0f, 0.95f)
        list[24] = PosePoint(hipX, hipY, 0f, 0.95f)

        // Ankle at (0.5, 0.8)
        val ankleX = 0.5f
        val ankleY = 0.8f
        list[27] = PosePoint(ankleX, ankleY, 0f, 0.95f)
        list[28] = PosePoint(ankleX, ankleY, 0f, 0.95f)

        // Angle theta formula: offset = (0.3) * tan((180 - theta) / 2)
        val clampedAngle = kneeAngleDeg.coerceIn(45f, 180f)
        val halfComplementRad = Math.toRadians(((180.0 - clampedAngle) / 2.0))
        val kneeY = 0.5f
        val kneeOffset = (0.3 * Math.tan(halfComplementRad)).toFloat()

        list[25] = PosePoint(hipX - kneeOffset, kneeY, 0f, 0.95f)
        list[26] = PosePoint(hipX - kneeOffset, kneeY, 0f, 0.95f)

        // Shoulders
        list[11] = PosePoint(0.45f, 0.1f, 0f, 0.95f)
        list[12] = PosePoint(0.55f, 0.1f, 0f, 0.95f)

        return list
    }
}
