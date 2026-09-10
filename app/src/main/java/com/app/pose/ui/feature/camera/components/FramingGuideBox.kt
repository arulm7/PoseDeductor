package com.app.pose.ui.feature.camera.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.app.pose.domain.model.CameraState
import com.app.pose.ui.theme.Bad400

@Composable
fun FramingGuideBox(
    state: CameraState,
    modifier: Modifier = Modifier
) {
    if (state != CameraState.READY && state != CameraState.LOST) return

    val color = if (state == CameraState.LOST) Bad400 else Color.White.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 140.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 4.dp.toPx()
            val cornerLen = 32.dp.toPx()
            val w = size.width
            val h = size.height

            // Top-Left
            drawLine(color, Offset(0f, 0f), Offset(cornerLen, 0f), strokePx, StrokeCap.Round)
            drawLine(color, Offset(0f, 0f), Offset(0f, cornerLen), strokePx, StrokeCap.Round)

            // Top-Right
            drawLine(color, Offset(w, 0f), Offset(w - cornerLen, 0f), strokePx, StrokeCap.Round)
            drawLine(color, Offset(w, 0f), Offset(w, cornerLen), strokePx, StrokeCap.Round)

            // Bottom-Left
            drawLine(color, Offset(0f, h), Offset(cornerLen, h), strokePx, StrokeCap.Round)
            drawLine(color, Offset(0f, h), Offset(0f, h - cornerLen), strokePx, StrokeCap.Round)

            // Bottom-Right
            drawLine(color, Offset(w, h), Offset(w - cornerLen, h), strokePx, StrokeCap.Round)
            drawLine(color, Offset(w, h), Offset(w, h - cornerLen), strokePx, StrokeCap.Round)
        }
    }
}
