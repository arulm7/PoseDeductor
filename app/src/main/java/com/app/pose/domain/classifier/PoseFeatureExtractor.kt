package com.app.pose.domain.classifier

import com.app.pose.domain.model.PoseLandmarkIndices
import com.app.pose.domain.model.PosePoint
import kotlin.math.acos
import kotlin.math.sqrt

object PoseFeatureExtractor {

    private data class Vec3(val x: Float, val y: Float, val z: Float) {
        operator fun minus(other: Vec3): Vec3 = Vec3(x - other.x, y - other.y, z - other.z)
        operator fun plus(other: Vec3): Vec3 = Vec3(x + other.x, y + other.y, z + other.z)
        operator fun div(scalar: Float): Vec3 = Vec3(x / scalar, y / scalar, z / scalar)

        fun dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z

        fun norm(): Float = sqrt(x * x + y * y + z * z)
    }

    fun extract170Features(landmarks: List<PosePoint>): FloatArray {
        require(landmarks.size >= 33) { "Expected at least 33 landmarks, got ${landmarks.size}" }

        // Mid-hip center
        val leftHipRaw = landmarks[PoseLandmarkIndices.LEFT_HIP]
        val rightHipRaw = landmarks[PoseLandmarkIndices.RIGHT_HIP]
        val midHipX = (leftHipRaw.x + rightHipRaw.x) / 2f
        val midHipY = (leftHipRaw.y + rightHipRaw.y) / 2f
        val midHipZ = (leftHipRaw.z + rightHipRaw.z) / 2f

        // 1. Convert all 33 landmarks centered at mid-hip and scaled by 100
        val points = Array(33) { i ->
            val lm = landmarks[i]
            Vec3(
                x = (lm.x - midHipX) * 100f,
                y = (lm.y - midHipY) * 100f,
                z = (lm.z - midHipZ) * 100f
            )
        }

        val rawFeatures = FloatArray(170)
        var idx = 0

        // Features 0..98: 33 landmarks * 3 coordinates
        for (p in points) {
            rawFeatures[idx++] = p.x
            rawFeatures[idx++] = p.y
            rawFeatures[idx++] = p.z
        }

        // Mid-hip in transformed space (0, 0, 0)
        val midHip = (points[PoseLandmarkIndices.LEFT_HIP] + points[PoseLandmarkIndices.RIGHT_HIP]) / 2f

        // Features 99..105: 7 Joint Angles (in degrees)
        // 99: right_elbow_right_shoulder_right_hip
        rawFeatures[idx++] = calculateAngle(
            points[PoseLandmarkIndices.RIGHT_ELBOW],
            points[PoseLandmarkIndices.RIGHT_SHOULDER],
            points[PoseLandmarkIndices.RIGHT_HIP]
        )
        // 100: left_elbow_left_shoulder_left_hip
        rawFeatures[idx++] = calculateAngle(
            points[PoseLandmarkIndices.LEFT_ELBOW],
            points[PoseLandmarkIndices.LEFT_SHOULDER],
            points[PoseLandmarkIndices.LEFT_HIP]
        )
        // 101: right_knee_mid_hip_left_knee
        rawFeatures[idx++] = calculateAngle(
            points[PoseLandmarkIndices.RIGHT_KNEE],
            midHip,
            points[PoseLandmarkIndices.LEFT_KNEE]
        )
        // 102: right_hip_right_knee_right_ankle
        rawFeatures[idx++] = calculateAngle(
            points[PoseLandmarkIndices.RIGHT_HIP],
            points[PoseLandmarkIndices.RIGHT_KNEE],
            points[PoseLandmarkIndices.RIGHT_ANKLE]
        )
        // 103: left_hip_left_knee_left_ankle
        rawFeatures[idx++] = calculateAngle(
            points[PoseLandmarkIndices.LEFT_HIP],
            points[PoseLandmarkIndices.LEFT_KNEE],
            points[PoseLandmarkIndices.LEFT_ANKLE]
        )
        // 104: right_wrist_right_elbow_right_shoulder
        rawFeatures[idx++] = calculateAngle(
            points[PoseLandmarkIndices.RIGHT_WRIST],
            points[PoseLandmarkIndices.RIGHT_ELBOW],
            points[PoseLandmarkIndices.RIGHT_SHOULDER]
        )
        // 105: left_wrist_left_elbow_left_shoulder
        rawFeatures[idx++] = calculateAngle(
            points[PoseLandmarkIndices.LEFT_WRIST],
            points[PoseLandmarkIndices.LEFT_ELBOW],
            points[PoseLandmarkIndices.LEFT_SHOULDER]
        )

        // Midpoints for hips-wrists-ankles
        val avgLeftWristAnkle = (points[PoseLandmarkIndices.LEFT_WRIST] + points[PoseLandmarkIndices.LEFT_ANKLE]) / 2f
        val avgRightWristAnkle = (points[PoseLandmarkIndices.RIGHT_WRIST] + points[PoseLandmarkIndices.RIGHT_ANKLE]) / 2f

        val distPairs = arrayOf(
            Pair(points[PoseLandmarkIndices.LEFT_SHOULDER], points[PoseLandmarkIndices.LEFT_WRIST]),
            Pair(points[PoseLandmarkIndices.RIGHT_SHOULDER], points[PoseLandmarkIndices.RIGHT_WRIST]),
            Pair(points[PoseLandmarkIndices.LEFT_HIP], points[PoseLandmarkIndices.LEFT_ANKLE]),
            Pair(points[PoseLandmarkIndices.RIGHT_HIP], points[PoseLandmarkIndices.RIGHT_ANKLE]),
            Pair(points[PoseLandmarkIndices.LEFT_HIP], points[PoseLandmarkIndices.LEFT_WRIST]),
            Pair(points[PoseLandmarkIndices.RIGHT_HIP], points[PoseLandmarkIndices.RIGHT_WRIST]),
            Pair(points[PoseLandmarkIndices.LEFT_SHOULDER], points[PoseLandmarkIndices.LEFT_ANKLE]),
            Pair(points[PoseLandmarkIndices.RIGHT_SHOULDER], points[PoseLandmarkIndices.RIGHT_ANKLE]),
            Pair(points[PoseLandmarkIndices.LEFT_HIP], points[PoseLandmarkIndices.RIGHT_WRIST]),
            Pair(points[PoseLandmarkIndices.RIGHT_HIP], points[PoseLandmarkIndices.LEFT_WRIST]),
            Pair(points[PoseLandmarkIndices.LEFT_ELBOW], points[PoseLandmarkIndices.RIGHT_ELBOW]),
            Pair(points[PoseLandmarkIndices.LEFT_KNEE], points[PoseLandmarkIndices.RIGHT_KNEE]),
            Pair(points[PoseLandmarkIndices.LEFT_WRIST], points[PoseLandmarkIndices.RIGHT_WRIST]),
            Pair(points[PoseLandmarkIndices.LEFT_ANKLE], points[PoseLandmarkIndices.RIGHT_ANKLE]),
            Pair(points[PoseLandmarkIndices.LEFT_HIP], avgLeftWristAnkle),
            Pair(points[PoseLandmarkIndices.RIGHT_HIP], avgRightWristAnkle)
        )

        // Features 106..121: 16 3D Euclidean distances
        for (pair in distPairs) {
            val delta = pair.second - pair.first
            rawFeatures[idx++] = delta.norm()
        }

        // Features 122..163: 14 pairs * 3 XYZ distances (P2 - P1)
        for (i in 0 until 14) {
            val delta = distPairs[i].second - distPairs[i].first
            rawFeatures[idx++] = delta.x
            rawFeatures[idx++] = delta.y
            rawFeatures[idx++] = delta.z
        }

        // Features 164..169: 2 average pairs * 3 XYZ distances (Hip - Avg)
        val leftHipAvgDelta = points[PoseLandmarkIndices.LEFT_HIP] - avgLeftWristAnkle
        rawFeatures[idx++] = leftHipAvgDelta.x
        rawFeatures[idx++] = leftHipAvgDelta.y
        rawFeatures[idx++] = leftHipAvgDelta.z

        val rightHipAvgDelta = points[PoseLandmarkIndices.RIGHT_HIP] - avgRightWristAnkle
        rawFeatures[idx++] = rightHipAvgDelta.x
        rawFeatures[idx++] = rightHipAvgDelta.y
        rawFeatures[idx++] = rightHipAvgDelta.z

        // Normalize with exact StandardScaler mean and scale
        val scaledFeatures = FloatArray(170)
        for (i in 0 until 170) {
            val mean = PoseFeatureScaler.MEAN[i]
            val scale = PoseFeatureScaler.SCALE[i]
            scaledFeatures[i] = if (scale != 0f) (rawFeatures[i] - mean) / scale else 0f
        }

        return scaledFeatures
    }

    private fun calculateAngle(a: Vec3, b: Vec3, c: Vec3): Float {
        val ba = a - b
        val bc = c - b
        val dot = ba.dot(bc)
        val norm = ba.norm() * bc.norm() + 1e-8f
        val cosine = (dot / norm).coerceIn(-1.0f, 1.0f)
        return Math.toDegrees(acos(cosine.toDouble())).toFloat()
    }
}
