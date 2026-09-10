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
    fun testSquatRepCycle_completesRepetition() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f)
        val descendingPose = createMockSquatPose(kneeAngleDeg = 130f)
        val bottomPose = createMockSquatPose(kneeAngleDeg = 88f)
        val ascendingPose = createMockSquatPose(kneeAngleDeg = 135f)

        // 1. Standing
        var result = analyzer.analyze(standingPose, 0L)
        for (i in 1..5) {
            result = analyzer.analyze(standingPose, i * 100L)
        }
        assertEquals(SquatPhase.STANDING, result.phase)
        assertEquals(0, result.repCount)

        // 2. Descending
        for (i in 6..12) {
            result = analyzer.analyze(descendingPose, i * 100L)
        }
        assertEquals(SquatPhase.DESCENDING, result.phase)

        // 3. Bottom reached (parallel/deep)
        for (i in 13..20) {
            result = analyzer.analyze(bottomPose, i * 100L)
        }
        assertEquals(SquatPhase.BOTTOM, result.phase)

        // 4. Ascending
        for (i in 21..28) {
            result = analyzer.analyze(ascendingPose, i * 100L)
        }
        assertEquals(SquatPhase.ASCENDING, result.phase)

        // 5. Back to standing (Completed Rep)
        for (i in 29..36) {
            result = analyzer.analyze(standingPose, i * 100L)
        }

        assertEquals(1, result.repCount)
        assertEquals(1, result.validRepCount)
        assertEquals(100, result.formScore)
        assertTrue(result.isStartingPositionValid)
    }

    @Test
    fun testShallowSquat_flagsRepAsInvalid() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f)
        val shallowPose = createMockSquatPose(kneeAngleDeg = 125f) // Doesn't reach <= 105 deg

        var result = analyzer.analyze(standingPose, 0L)
        for (i in 1..4) {
            result = analyzer.analyze(standingPose, i * 100L)
        }

        // Descend only partially
        for (i in 5..10) {
            result = analyzer.analyze(shallowPose, i * 100L)
        }

        // Rise back up
        for (i in 11..18) {
            result = analyzer.analyze(standingPose, i * 100L)
        }

        assertEquals(1, result.repCount)
        assertEquals(0, result.validRepCount)
        assertEquals(0, result.formScore)
    }

    @Test
    fun testOccludedPose_promptsPositioning() {
        // Occlude lower body landmarks (visibility = 0.1)
        val occludedPose = MutableList(33) { PosePoint(0.5f, 0.5f, 0f, 0.1f) }
        val result = analyzer.analyze(occludedPose, 0L)

        assertEquals(SquatPhase.STARTING_POSITION, result.phase)
        assertEquals(false, result.isStartingPositionValid)
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
