package com.app.pose.classifier

import com.app.pose.domain.classifier.ExercisePoseClass
import com.app.pose.domain.classifier.PoseClassificationResult
import com.app.pose.domain.engine.RepMovementState
import com.app.pose.domain.engine.SquatRepValidator
import com.app.pose.domain.model.PosePoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class SquatRepValidatorTest {

    private lateinit var validator: SquatRepValidator

    @Before
    fun setup() {
        validator = SquatRepValidator()
    }

    /**
     * Test 1:
     * Standing frames only
     * -> 0 reps
     */
    @Test
    fun test1_standingFramesOnly_producesZeroReps() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f, hipDrop = 0f)
        val standingTflite = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.95f)

        for (i in 1..30) {
            val res = validator.processFrame(standingPose, standingTflite, i * 100L)
            assertEquals(0, res.repCount)
            assertEquals(RepMovementState.WAITING_FOR_UP, res.state)
        }
    }

    /**
     * Test 2:
     * Standing -> 10° shallow knee movement -> standing
     * -> 0 reps
     */
    @Test
    fun test2_small10DegreeKneeMovement_producesZeroReps() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f, hipDrop = 0f)
        val small10BendPose = createMockSquatPose(kneeAngleDeg = 165f, hipDrop = 0.01f)
        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.85f)

        // 1. Stand
        for (i in 1..10) validator.processFrame(standingPose, tfliteUp, i * 100L)

        // 2. 10° slight knee bend
        for (i in 11..20) {
            val res = validator.processFrame(small10BendPose, tfliteUp, i * 100L)
            assertEquals(0, res.repCount)
        }

        // 3. Return to stand
        for (i in 21..30) {
            val res = validator.processFrame(standingPose, tfliteUp, i * 100L)
            assertEquals(0, res.repCount)
            assertEquals(RepMovementState.WAITING_FOR_UP, res.state)
        }
    }

    /**
     * Test 3:
     * Standing -> 15° shallow knee movement -> standing
     * -> 0 reps
     */
    @Test
    fun test3_small15DegreeKneeMovement_producesZeroReps() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f, hipDrop = 0f)
        val small15BendPose = createMockSquatPose(kneeAngleDeg = 160f, hipDrop = 0.015f)
        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.85f)

        // 1. Stand
        for (i in 1..10) validator.processFrame(standingPose, tfliteUp, i * 100L)

        // 2. 15° knee movement
        for (i in 11..20) {
            val res = validator.processFrame(small15BendPose, tfliteUp, i * 100L)
            assertEquals(0, res.repCount)
        }

        // 3. Return to stand
        for (i in 21..30) {
            val res = validator.processFrame(standingPose, tfliteUp, i * 100L)
            assertEquals(0, res.repCount)
            assertEquals(RepMovementState.WAITING_FOR_UP, res.state)
        }
    }

    /**
     * Test 4:
     * Single-leg knee lift (standing on one leg, bending/lifting the other)
     * -> 0 reps
     */
    @Test
    fun test4_singleLegKneeLift_producesZeroReps() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f, hipDrop = 0f)
        // Standing on left leg (170°), lifting right knee up (bent to 70°, ankle lifted to 0.50f, no hip drop)
        val singleKneeLiftPose = createMockSquatPose(
            leftKneeAngleDeg = 170f,
            rightKneeAngleDeg = 70f,
            hipDrop = 0.01f,
            rightAnkleY = 0.50f
        )
        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.85f)

        // 1. Stand
        for (i in 1..8) validator.processFrame(standingPose, tfliteUp, i * 100L)

        // 2. Lift single knee in air
        for (i in 9..25) {
            val res = validator.processFrame(singleKneeLiftPose, tfliteUp, i * 100L)
            assertEquals(0, res.repCount)
        }

        // 3. Put foot back down
        for (i in 26..35) {
            val res = validator.processFrame(standingPose, tfliteUp, i * 100L)
            assertEquals(0, res.repCount)
            assertEquals(RepMovementState.WAITING_FOR_UP, res.state)
        }
    }

    /**
     * Test 5:
     * Single-leg kick (standing on one leg, kicking other foot forward/up)
     * -> 0 reps
     */
    @Test
    fun test5_singleLegKick_producesZeroReps() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f, hipDrop = 0f)
        // Kicking right leg forward (ankle lifted, hip center stays high)
        val singleKickPose = createMockSquatPose(
            leftKneeAngleDeg = 168f,
            rightKneeAngleDeg = 140f,
            hipDrop = 0.01f,
            rightAnkleY = 0.45f
        )
        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.85f)

        for (i in 1..8) validator.processFrame(standingPose, tfliteUp, i * 100L)

        for (i in 9..25) {
            val res = validator.processFrame(singleKickPose, tfliteUp, i * 100L)
            assertEquals(0, res.repCount)
        }

        for (i in 26..35) {
            val res = validator.processFrame(standingPose, tfliteUp, i * 100L)
            assertEquals(0, res.repCount)
            assertEquals(RepMovementState.WAITING_FOR_UP, res.state)
        }
    }

    /**
     * Test 6:
     * Deep squat with proper hip descent
     * -> exactly 1 rep
     */
    @Test
    fun test6_deepSquatWithHipDescent_producesExactlyOneRep() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f, hipDrop = 0f)
        val descendingPose = createMockSquatPose(kneeAngleDeg = 140f, hipDrop = 0.05f)
        val bottomPose = createMockSquatPose(kneeAngleDeg = 95f, hipDrop = 0.12f)
        val ascendingPose = createMockSquatPose(kneeAngleDeg = 138f, hipDrop = 0.05f)

        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.90f)
        val tfliteDown = createMockTfliteResult(ExercisePoseClass.SQUATS_DOWN, 0.92f)

        // 1. Stand
        for (i in 1..8) validator.processFrame(standingPose, tfliteUp, i * 100L)

        // 2. Descending
        for (i in 9..14) validator.processFrame(descendingPose, tfliteDown, i * 100L)

        // 3. Bottom depth confirmed (VALID_DOWN)
        for (i in 15..20) {
            validator.processFrame(bottomPose, tfliteDown, i * 100L)
        }
        val bottomRes = validator.processFrame(bottomPose, tfliteDown, 2100L)
        assertEquals(RepMovementState.VALID_DOWN, bottomRes.state)
        assertEquals(0, bottomRes.repCount)

        // 4. Ascending
        for (i in 22..28) {
            validator.processFrame(ascendingPose, tfliteUp, i * 100L)
        }

        // 5. Return to standing (+1 REP)
        var finalResult = validator.processFrame(standingPose, tfliteUp, 2900L)
        for (i in 30..40) {
            finalResult = validator.processFrame(standingPose, tfliteUp, i * 100L)
        }

        assertEquals(1, finalResult.repCount)
    }

    /**
     * Test 7:
     * Two genuine squats
     * -> exactly 2 reps
     */
    @Test
    fun test7_twoGenuineSquats_producesExactlyTwoReps() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f, hipDrop = 0f)
        val descendingPose = createMockSquatPose(kneeAngleDeg = 140f, hipDrop = 0.05f)
        val bottomPose = createMockSquatPose(kneeAngleDeg = 95f, hipDrop = 0.12f)
        val ascendingPose = createMockSquatPose(kneeAngleDeg = 138f, hipDrop = 0.05f)

        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.90f)
        val tfliteDown = createMockTfliteResult(ExercisePoseClass.SQUATS_DOWN, 0.92f)

        // --- Rep 1 ---
        for (i in 1..8) validator.processFrame(standingPose, tfliteUp, i * 100L)
        for (i in 9..14) validator.processFrame(descendingPose, tfliteDown, i * 100L)
        for (i in 15..20) validator.processFrame(bottomPose, tfliteDown, i * 100L)
        for (i in 21..27) validator.processFrame(ascendingPose, tfliteUp, i * 100L)
        for (i in 28..36) validator.processFrame(standingPose, tfliteUp, i * 100L)

        assertEquals(1, validator.processFrame(standingPose, tfliteUp, 3700L).repCount)

        // --- Rep 2 ---
        for (i in 38..43) validator.processFrame(descendingPose, tfliteDown, i * 100L)
        for (i in 44..50) validator.processFrame(bottomPose, tfliteDown, i * 100L)
        for (i in 51..57) validator.processFrame(ascendingPose, tfliteUp, i * 100L)
        var result = validator.processFrame(standingPose, tfliteUp, 5800L)
        for (i in 59..68) {
            result = validator.processFrame(standingPose, tfliteUp, i * 100L)
        }

        assertEquals(2, result.repCount)
    }

    /**
     * Test 8:
     * Deep squat without returning upright
     * -> 0 reps
     */
    @Test
    fun test8_deepSquatWithoutReturningUpright_producesZeroReps() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f, hipDrop = 0f)
        val descendingPose = createMockSquatPose(kneeAngleDeg = 140f, hipDrop = 0.05f)
        val bottomPose = createMockSquatPose(kneeAngleDeg = 90f, hipDrop = 0.13f)

        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.90f)
        val tfliteDown = createMockTfliteResult(ExercisePoseClass.SQUATS_DOWN, 0.95f)

        for (i in 1..8) validator.processFrame(standingPose, tfliteUp, i * 100L)
        for (i in 9..15) validator.processFrame(descendingPose, tfliteDown, i * 100L)

        for (i in 16..20) {
            validator.processFrame(bottomPose, tfliteDown, i * 100L)
        }

        // Hold at bottom indefinitely
        for (i in 21..65) {
            val res = validator.processFrame(bottomPose, tfliteDown, i * 100L)
            assertEquals(0, res.repCount)
            assertEquals(RepMovementState.VALID_DOWN, res.state)
        }
    }

    /**
     * Test 9:
     * TFLite says `squats_down` during standing
     * -> 0 reps
     */
    @Test
    fun test9_tfliteSquatsDownWhileStanding_producesZeroReps() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f, hipDrop = 0f)
        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.90f)
        val tfliteDown = createMockTfliteResult(ExercisePoseClass.SQUATS_DOWN, 0.95f)

        for (i in 1..8) validator.processFrame(standingPose, tfliteUp, i * 100L)

        for (i in 9..14) {
            val res = validator.processFrame(standingPose, tfliteDown, i * 100L)
            assertEquals(0, res.repCount)
            assertEquals(RepMovementState.WAITING_FOR_UP, res.state)
        }

        for (i in 15..25) {
            val res = validator.processFrame(standingPose, tfliteUp, i * 100L)
            assertEquals(0, res.repCount)
            assertEquals(RepMovementState.WAITING_FOR_UP, res.state)
        }
    }

    /**
     * Test 10:
     * Genuine squat but TFLite predicts `pullups_up` (e.g. back-facing view)
     * -> still exactly 1 rep if geometry is strongly valid
     */
    @Test
    fun test10_genuineSquatWithTflitePullupsUp_producesExactlyOneRep() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f, hipDrop = 0f)
        val descendingPose = createMockSquatPose(kneeAngleDeg = 140f, hipDrop = 0.05f)
        val bottomPose = createMockSquatPose(kneeAngleDeg = 92f, hipDrop = 0.12f)
        val ascendingPose = createMockSquatPose(kneeAngleDeg = 138f, hipDrop = 0.05f)

        // TFLite misclassifies as pullups_up throughout the back-facing squat!
        val tflitePullups = createMockTfliteResult(ExercisePoseClass.PULLUPS_UP, 0.75f)

        for (i in 1..8) validator.processFrame(standingPose, tflitePullups, i * 100L)
        for (i in 9..14) validator.processFrame(descendingPose, tflitePullups, i * 100L)
        for (i in 15..20) validator.processFrame(bottomPose, tflitePullups, i * 100L)
        for (i in 21..27) validator.processFrame(ascendingPose, tflitePullups, i * 100L)
        var res = validator.processFrame(standingPose, tflitePullups, 2800L)
        for (i in 29..38) res = validator.processFrame(standingPose, tflitePullups, i * 100L)

        assertEquals(1, res.repCount)
    }

    /**
     * Test 11:
     * Genuine squat but TFLite predicts `jumping_jacks_down` (e.g. back-facing deep squat)
     * -> still exactly 1 rep if geometry is strongly valid
     */
    @Test
    fun test11_genuineSquatWithTfliteJumpingJacksDown_producesExactlyOneRep() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f, hipDrop = 0f)
        val descendingPose = createMockSquatPose(kneeAngleDeg = 140f, hipDrop = 0.05f)
        val bottomPose = createMockSquatPose(kneeAngleDeg = 90f, hipDrop = 0.13f)
        val ascendingPose = createMockSquatPose(kneeAngleDeg = 138f, hipDrop = 0.05f)

        // TFLite misclassifies as jumping_jacks_down
        val tfliteJumpingJacks = createMockTfliteResult(ExercisePoseClass.JUMPING_JACKS_DOWN, 0.70f)

        for (i in 1..8) validator.processFrame(standingPose, tfliteJumpingJacks, i * 100L)
        for (i in 9..14) validator.processFrame(descendingPose, tfliteJumpingJacks, i * 100L)
        for (i in 15..20) validator.processFrame(bottomPose, tfliteJumpingJacks, i * 100L)
        for (i in 21..27) validator.processFrame(ascendingPose, tfliteJumpingJacks, i * 100L)
        var res = validator.processFrame(standingPose, tfliteJumpingJacks, 2800L)
        for (i in 29..38) res = validator.processFrame(standingPose, tfliteJumpingJacks, i * 100L)

        assertEquals(1, res.repCount)
    }

    /**
     * Test 12:
     * Landmark jitter around thresholds
     * -> no duplicate reps
     */
    @Test
    fun test12_landmarkJitterAroundThresholds_noDuplicateReps() {
        val random = Random(42)
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f, hipDrop = 0f)
        val descendingPose = createMockSquatPose(kneeAngleDeg = 140f, hipDrop = 0.05f)
        val bottomPose = createMockSquatPose(kneeAngleDeg = 95f, hipDrop = 0.12f)
        val ascendingPose = createMockSquatPose(kneeAngleDeg = 138f, hipDrop = 0.05f)

        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.90f)
        val tfliteDown = createMockTfliteResult(ExercisePoseClass.SQUATS_DOWN, 0.92f)

        // Perform 1 genuine rep
        for (i in 1..8) validator.processFrame(standingPose, tfliteUp, i * 100L)
        for (i in 9..14) validator.processFrame(descendingPose, tfliteDown, i * 100L)
        for (i in 15..20) validator.processFrame(bottomPose, tfliteDown, i * 100L)
        for (i in 21..27) validator.processFrame(ascendingPose, tfliteUp, i * 100L)
        for (i in 28..36) validator.processFrame(standingPose, tfliteUp, i * 100L)

        assertEquals(1, validator.processFrame(standingPose, tfliteUp, 3700L).repCount)

        // Apply noise/jitter around threshold zones with noise
        for (i in 38..100) {
            val jitter = (random.nextFloat() - 0.5f) * 10f
            val jitterPose = createMockSquatPose(kneeAngleDeg = 165f + jitter, hipDrop = 0.01f)
            val res = validator.processFrame(jitterPose, tfliteUp, i * 100L)
            assertEquals(1, res.repCount) // Must strictly remain 1, never increment to 2
        }
    }

    /**
     * Test 13:
     * One-leg knee lift with strong TFLite `squats_down` prediction
     * -> 0 reps (geometry protects against false classification!)
     */
    @Test
    fun test13_oneLegKneeLiftWithStrongTfliteSquatsDown_producesZeroReps() {
        val standingPose = createMockSquatPose(kneeAngleDeg = 175f, hipDrop = 0f)
        // User lifts right knee, stands on left leg (no hip drop, ankle lifted)
        val oneLegLiftPose = createMockSquatPose(
            leftKneeAngleDeg = 170f,
            rightKneeAngleDeg = 65f,
            hipDrop = 0.01f,
            rightAnkleY = 0.48f
        )
        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.90f)
        val tfliteDownStrong = createMockTfliteResult(ExercisePoseClass.SQUATS_DOWN, 0.99f)

        for (i in 1..8) validator.processFrame(standingPose, tfliteUp, i * 100L)

        // Even with 99% confident TFLite squats_down, single-leg lift MUST NOT trigger a squat!
        for (i in 9..25) {
            val res = validator.processFrame(oneLegLiftPose, tfliteDownStrong, i * 100L)
            assertEquals(0, res.repCount)
        }

        // Return to standing
        for (i in 26..35) {
            val res = validator.processFrame(standingPose, tfliteUp, i * 100L)
            assertEquals(0, res.repCount)
            assertEquals(RepMovementState.WAITING_FOR_UP, res.state)
        }
    }

    /**
     * Test 14:
     * Genuine squat with asymmetric but realistic landmark noise
     * -> should remain valid (1 rep)
     */
    @Test
    fun test14_genuineSquatWithRealisticAsymmetry_producesExactlyOneRep() {
        val standingPose = createMockSquatPose(leftKneeAngleDeg = 172f, rightKneeAngleDeg = 176f, hipDrop = 0f)
        val descendingPose = createMockSquatPose(leftKneeAngleDeg = 135f, rightKneeAngleDeg = 143f, hipDrop = 0.05f)
        // Real-world bilateral asymmetry of ~12°
        val bottomPose = createMockSquatPose(leftKneeAngleDeg = 92f, rightKneeAngleDeg = 104f, hipDrop = 0.11f)
        val ascendingPose = createMockSquatPose(leftKneeAngleDeg = 136f, rightKneeAngleDeg = 142f, hipDrop = 0.05f)

        val tfliteUp = createMockTfliteResult(ExercisePoseClass.SQUATS_UP, 0.88f)
        val tfliteDown = createMockTfliteResult(ExercisePoseClass.SQUATS_DOWN, 0.90f)

        for (i in 1..8) validator.processFrame(standingPose, tfliteUp, i * 100L)
        for (i in 9..14) validator.processFrame(descendingPose, tfliteDown, i * 100L)
        for (i in 15..20) validator.processFrame(bottomPose, tfliteDown, i * 100L)
        for (i in 21..27) validator.processFrame(ascendingPose, tfliteUp, i * 100L)
        var res = validator.processFrame(standingPose, tfliteUp, 2800L)
        for (i in 29..38) res = validator.processFrame(standingPose, tfliteUp, i * 100L)

        assertEquals(1, res.repCount)
    }

    private fun createMockSquatPose(
        kneeAngleDeg: Float = 175f,
        hipDrop: Float = 0f,
        leftKneeAngleDeg: Float = kneeAngleDeg,
        rightKneeAngleDeg: Float = kneeAngleDeg,
        leftAnkleY: Float = 0.80f,
        rightAnkleY: Float = 0.80f
    ): List<PosePoint> {
        val list = MutableList(33) { PosePoint(0.5f, 0.5f, 0f, 0.95f) }

        val hipY = 0.2f + hipDrop
        val hipXLeft = 0.45f
        val hipXRight = 0.55f
        list[23] = PosePoint(hipXLeft, hipY, 0f, 0.95f)
        list[24] = PosePoint(hipXRight, hipY, 0f, 0.95f)

        list[27] = PosePoint(hipXLeft, leftAnkleY, 0f, 0.95f)
        list[28] = PosePoint(hipXRight, rightAnkleY, 0f, 0.95f)

        val leftKneeY = (hipY + leftAnkleY) / 2f
        val rightKneeY = (hipY + rightAnkleY) / 2f

        val leftLegSpanHalf = (leftAnkleY - hipY) / 2f
        val rightLegSpanHalf = (rightAnkleY - hipY) / 2f

        val clampedLeft = leftKneeAngleDeg.coerceIn(45f, 180f)
        val leftHalfRad = Math.toRadians(((180.0 - clampedLeft) / 2.0))
        val leftOffset = (leftLegSpanHalf * Math.tan(leftHalfRad)).toFloat()

        val clampedRight = rightKneeAngleDeg.coerceIn(45f, 180f)
        val rightHalfRad = Math.toRadians(((180.0 - clampedRight) / 2.0))
        val rightOffset = (rightLegSpanHalf * Math.tan(rightHalfRad)).toFloat()

        list[25] = PosePoint(hipXLeft - leftOffset, leftKneeY, 0f, 0.95f)
        list[26] = PosePoint(hipXRight - rightOffset, rightKneeY, 0f, 0.95f)

        list[11] = PosePoint(hipXLeft, hipY - 0.1f, 0f, 0.95f)
        list[12] = PosePoint(hipXRight, hipY - 0.1f, 0f, 0.95f)

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
