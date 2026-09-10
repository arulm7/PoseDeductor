package com.app.pose.ui.feature.camera.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.app.pose.domain.model.CameraState
import com.app.pose.domain.model.PoseLandmarkIndices
import com.app.pose.domain.model.PosePoint

@Composable
fun PoseSkeletonCanvas(
    landmarks: List<PosePoint>,
    state: CameraState,
    modifier: Modifier = Modifier
) {
    if (landmarks.isEmpty() || state == CameraState.LOST || state == CameraState.READY) return

    val (boneColor, jointColor, faultColor) = when (state) {
        CameraState.INCORRECT -> Triple(
            Color(0xFFFFC24D),
            Color(0xFFFFD98A),
            Color(0xFFFF6B60)
        )
        CameraState.COMPLETE -> Triple(
            Color(0xFF2ECF92),
            Color(0xFFFFFFFF),
            Color(0xFF2ECF92)
        )
        CameraState.CORRECT -> Triple(
            Color(0xFF2ECF92),
            Color(0xFFFFFFFF),
            Color(0xFF2ECF92)
        )
        else -> Triple(
            Color(0xFF2ECF92).copy(alpha = 0.9f),
            Color(0xFFFFFFFF),
            Color(0xFF2ECF92)
        )
    }

    val isDim = state == CameraState.TRACKING

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val baseAlpha = if (isDim) 0.85f else 1.0f

        // Convert normalized landmarks (0..1) to pixel offsets
        val landmarkOffsets = landmarks.map { point ->
            Offset(point.x * w, point.y * h)
        }

        // Draw Skeleton Bones from MediaPipe connections
        PoseLandmarkIndices.CONNECTIONS.forEach { (a, b) ->
            if (a < landmarkOffsets.size && b < landmarkOffsets.size) {
                val ptA = landmarks[a]
                val ptB = landmarks[b]

                if (ptA.visibility > 0.4f && ptB.visibility > 0.4f) {
                    val isFaulty = state == CameraState.INCORRECT &&
                            (a == PoseLandmarkIndices.LEFT_KNEE || a == PoseLandmarkIndices.RIGHT_KNEE ||
                             b == PoseLandmarkIndices.LEFT_KNEE || b == PoseLandmarkIndices.RIGHT_KNEE)

                    val strokeColor = if (isFaulty) faultColor else boneColor
                    val strokeWidthPx = if (isFaulty) 7.dp.toPx() else 4.5.dp.toPx()

                    drawLine(
                        color = strokeColor.copy(alpha = baseAlpha),
                        start = landmarkOffsets[a],
                        end = landmarkOffsets[b],
                        strokeWidth = strokeWidthPx,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Draw Key Joints
        landmarkOffsets.forEachIndexed { idx, offset ->
            if (idx < landmarks.size && landmarks[idx].visibility > 0.4f) {
                if (idx == PoseLandmarkIndices.NOSE) {
                    // Head indicator
                    drawCircle(
                        color = jointColor.copy(alpha = baseAlpha * 0.2f),
                        radius = 22.dp.toPx(),
                        center = offset
                    )
                    drawCircle(
                        color = boneColor.copy(alpha = baseAlpha),
                        radius = 14.dp.toPx(),
                        center = offset,
                        style = Stroke(width = 3.dp.toPx())
                    )
                } else if (isMajorJoint(idx)) {
                    // Outer glow
                    drawCircle(
                        color = jointColor.copy(alpha = baseAlpha * 0.3f),
                        radius = 8.dp.toPx(),
                        center = offset
                    )
                    // Inner solid joint
                    drawCircle(
                        color = jointColor.copy(alpha = baseAlpha),
                        radius = 4.5.dp.toPx(),
                        center = offset
                    )
                }
            }
        }
    }
}

private fun isMajorJoint(index: Int): Boolean {
    return when (index) {
        PoseLandmarkIndices.LEFT_SHOULDER,
        PoseLandmarkIndices.RIGHT_SHOULDER,
        PoseLandmarkIndices.LEFT_ELBOW,
        PoseLandmarkIndices.RIGHT_ELBOW,
        PoseLandmarkIndices.LEFT_WRIST,
        PoseLandmarkIndices.RIGHT_WRIST,
        PoseLandmarkIndices.LEFT_HIP,
        PoseLandmarkIndices.RIGHT_HIP,
        PoseLandmarkIndices.LEFT_KNEE,
        PoseLandmarkIndices.RIGHT_KNEE,
        PoseLandmarkIndices.LEFT_ANKLE,
        PoseLandmarkIndices.RIGHT_ANKLE -> true
        else -> false
    }
}
