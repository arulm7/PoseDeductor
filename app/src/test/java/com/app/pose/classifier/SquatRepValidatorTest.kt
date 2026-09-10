package com.app.pose.classifier

import com.app.pose.domain.classifier.ExercisePoseClass
import com.app.pose.domain.classifier.PoseClassificationResult
import com.app.pose.domain.engine.RepMovementState
import com.app.pose.domain.engine.SquatRepValidator
import com.app.pose.domain.model.PosePoint
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class SquatRepValidatorTest {

    private lateinit var validator: SquatRepValidator

    @Before
    fun setup() {
        validator = SquatRepValidator()
    }

    @Test
    fun test1_standingOnly_producesZeroReps() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f)
        val standingTflite = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.95f)

        for (i in 1..25) {
            val res = validator.processFrame(standingPose, standingTflite, i * 100L)
            assertEquals(0, res.repCount)
            assertEquals(RepMovementState.WAITING_UP, res.state)
        }
    }

    @Test
    fun test2_smallKneeBend_producesZeroReps() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f)
        val smallBendPose = createMockSquatPose(kneeAngleDeg = 160f) // 15° drop, not reaching 35° / 128°
        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.85f)

        // Stand
        for (i in 1..6) validator.processFrame(standingPose, tfliteUp, i * 100L)

        // Hold small bend
        for (i in 7..15) {
            val res = validator.processFrame(smallBendPose, tfliteUp, i * 100L)
            assertEquals(0, res.repCount)
        }
    }

    @Test
    fun test3_smallBendAndReturn_producesZeroReps() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f)
        val smallBendPose = createMockSquatPose(kneeAngleDeg = 158f)
        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.85f)

        // 175° -> 160° -> 175°
        for (i in 1..6) validator.processFrame(standingPose, tfliteUp, i * 100L)
        for (i in 7..14) validator.processFrame(smallBendPose, tfliteUp, i * 100L)
        for (i in 15..24) {
            val res = validator.processFrame(standingPose, tfliteUp, i * 100L)
            assertEquals(0, res.repCount)
            assertEquals(RepMovementState.WAITING_UP, res.state)
        }
    }

    @Test
    fun test4_deepSquatAndReturn_producesExactlyOneRep() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f)
        val descendingPose = createMockSquatPose(kneeAngleDeg = 145f)
        val bottomPose = createMockSquatPose(kneeAngleDeg = 95f) // Deep depth <= 128°, movement >= 35°
        val ascendingPose = createMockSquatPose(kneeAngleDeg = 140f)

        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.90f)
        val tfliteDown = createMockTfliteResult(ExercisePoseClass.SQUATS_DOWN, 0.92f)

        // 1. Standing (WAITING_UP)
        for (i in 1..6) validator.processFrame(standingPose, tfliteUp, i * 100L)

        // 2. Descending (DOWN_CANDIDATE)
        for (i in 7..12) validator.processFrame(descendingPose, tfliteDown, i * 100L)

        // 3. Bottom depth confirmed (DOWN_CONFIRMED)
        for (i in 13..18) {
            validator.processFrame(bottomPose, tfliteDown, i * 100L)
        }
        val bottomRes = validator.processFrame(bottomPose, tfliteDown, 1900L)
        assertEquals(RepMovementState.DOWN_CONFIRMED, bottomRes.state)
        assertEquals(0, bottomRes.repCount)

        // 4. Ascending (RETURNING_UP)
        for (i in 20..26) {
            validator.processFrame(ascendingPose, tfliteUp, i * 100L)
        }

        // 5. Complete return to standing (+1 REP)
        var finalResult = validator.processFrame(standingPose, tfliteUp, 2700L)
        for (i in 28..36) {
            finalResult = validator.processFrame(standingPose, tfliteUp, i * 100L)
        }

        assertEquals(1, finalResult.repCount)
    }

    @Test
    fun test5_twoCompleteSquats_producesExactlyTwoReps() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f)
        val descendingPose = createMockSquatPose(kneeAngleDeg = 145f)
        val bottomPose = createMockSquatPose(kneeAngleDeg = 95f)
        val ascendingPose = createMockSquatPose(kneeAngleDeg = 140f)

        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.90f)
        val tfliteDown = createMockTfliteResult(ExercisePoseClass.SQUATS_DOWN, 0.92f)

        // --- Rep 1 ---
        for (i in 1..5) validator.processFrame(standingPose, tfliteUp, i * 100L)
        for (i in 6..10) validator.processFrame(descendingPose, tfliteDown, i * 100L)
        for (i in 11..16) validator.processFrame(bottomPose, tfliteDown, i * 100L)
        for (i in 17..22) validator.processFrame(ascendingPose, tfliteUp, i * 100L)
        for (i in 23..30) validator.processFrame(standingPose, tfliteUp, i * 100L)

        assertEquals(1, validator.processFrame(standingPose, tfliteUp, 3100L).repCount)

        // --- Rep 2 ---
        for (i in 32..37) validator.processFrame(descendingPose, tfliteDown, i * 100L)
        for (i in 38..44) validator.processFrame(bottomPose, tfliteDown, i * 100L)
        for (i in 45..50) validator.processFrame(ascendingPose, tfliteUp, i * 100L)
        var result = validator.processFrame(standingPose, tfliteUp, 5100L)
        for (i in 52..60) {
            result = validator.processFrame(standingPose, tfliteUp, i * 100L)
        }

        assertEquals(2, result.repCount)
    }

    @Test
    fun test6_deepSquatWithoutReturningUp_producesZeroReps() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f)
        val descendingPose = createMockSquatPose(kneeAngleDeg = 145f)
        val bottomPose = createMockSquatPose(kneeAngleDeg = 90f)

        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.90f)
        val tfliteDown = createMockTfliteResult(ExercisePoseClass.SQUATS_DOWN, 0.95f)

        for (i in 1..5) validator.processFrame(standingPose, tfliteUp, i * 100L)
        for (i in 6..12) validator.processFrame(descendingPose, tfliteDown, i * 100L)

        // Hold at bottom for 50 frames
        for (i in 13..20) {
            validator.processFrame(bottomPose, tfliteDown, i * 100L)
        }
        for (i in 21..65) {
            val res = validator.processFrame(bottomPose, tfliteDown, i * 100L)
            assertEquals(0, res.repCount)
            assertEquals(RepMovementState.DOWN_CONFIRMED, res.state)
        }
    }

    @Test
    fun test7_noisyJitteryLandmarks_noFalseRep() {
        val random = Random(42)
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f)
        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.90f)

        for (i in 1..40) {
            // Add jitter noise +/- 4 degrees to knee
            val jitter = (random.nextFloat() - 0.5f) * 8f
            val jitterPose = createMockSquatPose(kneeAngleDeg = 175f + jitter)
            val res = validator.processFrame(jitterPose, tfliteUp, i * 100L)
            assertEquals(0, res.repCount)
        }
    }

    @Test
    fun test8_repeatedUpFramesAfterOneSquat_remainsOneRep() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f)
        val descendingPose = createMockSquatPose(kneeAngleDeg = 145f)
        val bottomPose = createMockSquatPose(kneeAngleDeg = 95f)
        val ascendingPose = createMockSquatPose(kneeAngleDeg = 140f)

        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.90f)
        val tfliteDown = createMockTfliteResult(ExercisePoseClass.SQUATS_DOWN, 0.92f)

        for (i in 1..5) validator.processFrame(standingPose, tfliteUp, i * 100L)
        for (i in 6..10) validator.processFrame(descendingPose, tfliteDown, i * 100L)
        for (i in 11..16) validator.processFrame(bottomPose, tfliteDown, i * 100L)
        for (i in 17..22) validator.processFrame(ascendingPose, tfliteUp, i * 100L)
        for (i in 23..30) validator.processFrame(standingPose, tfliteUp, i * 100L)

        // Continue standing for 60 more frames
        for (i in 31..90) {
            val res = validator.processFrame(standingPose, tfliteUp, i * 100L)
            assertEquals(1, res.repCount)
        }
    }

    @Test
    fun test9_tfliteFlickeringWithoutMovement_noFalseRep() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f)
        val tfliteDown = createMockTfliteResult(ExercisePoseClass.SQUATS_DOWN, 0.98f)
        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.98f)

        // TFLite model predictions flicker down and up, but physical person remains standing straight
        for (i in 1..30) {
            val pred = if (i % 2 == 0) tfliteDown else tfliteUp
            val res = validator.processFrame(standingPose, pred, i * 100L)
            assertEquals(0, res.repCount)
            assertEquals(RepMovementState.WAITING_UP, res.state)
        }
    }

    private fun createMockSquatPose(kneeAngleDeg: Float): List<PosePoint> {
        val list = MutableList(33) { PosePoint(0.5f, 0.5f, 0f, 0.95f) }

        val hipY = 0.2f
        list[23] = PosePoint(0.45f, hipY, 0f, 0.95f)
        list[24] = PosePoint(0.55f, hipY, 0f, 0.95f)

        val ankleY = 0.8f
        list[27] = PosePoint(0.45f, ankleY, 0f, 0.95f)
        list[28] = PosePoint(0.55f, ankleY, 0f, 0.95f)

        val kneeY = (hipY + ankleY) / 2f
        val legSpanHalf = (ankleY - hipY) / 2f

        val clamped = kneeAngleDeg.coerceIn(45f, 180f)
        val halfRad = Math.toRadians(((180.0 - clamped) / 2.0))
        val offset = (legSpanHalf * Math.tan(halfRad)).toFloat()

        list[25] = PosePoint(0.45f - offset, kneeY, 0f, 0.95f)
        list[26] = PosePoint(0.55f - offset, kneeY, 0f, 0.95f)

        list[11] = PosePoint(0.45f, hipY - 0.1f, 0f, 0.95f)
        list[12] = PosePoint(0.55f, hipY - 0.1f, 0f, 0.95f)

        return list
    }

    private fun createMockTfliteResult(topClass: ExercisePoseClass, confidence: Float): PoseClassificationResult {
        val probs = FloatArray(10)
        probs[topClass.id] = confidence
        val secondClass = if (topClass == ExercisePoseClass.SQUATS_UP) ExercisePoseClass.SQUATS_DOWN else ExercisePoseClass.SQUATS_UP
        probs[secondClass.id] = 1f - confidence
        return PoseClassificationResult(
            topClass = topClass,
            topConfidence = confidence,
            secondClass = secondClass,
            secondConfidence = 1f - confidence,
            probabilities = probs
        )
    }
}
