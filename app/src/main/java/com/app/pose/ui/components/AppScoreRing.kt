package com.app.pose.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.pose.ui.theme.Accent500
import com.app.pose.ui.theme.Bad400
import com.app.pose.ui.theme.Good400
import com.app.pose.ui.theme.Warn400

enum class RingTone {
    ACCENT,
    GOOD,
    WARN,
    BAD
}

@Composable
fun AppScoreRing(
    progressPercent: Int,
    modifier: Modifier = Modifier,
    size: Dp = 92.dp,
    strokeWidth: Dp = 9.dp,
    tone: RingTone = RingTone.ACCENT,
    onDark: Boolean = false,
    content: @Composable (() -> Unit)? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (progressPercent.coerceIn(0, 100)) / 100f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "scoreRingProgress"
    )

    val trackColor = if (onDark) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }

    val progressColor = when (tone) {
        RingTone.ACCENT -> Accent500
        RingTone.GOOD -> Good400
        RingTone.WARN -> Warn400
        RingTone.BAD -> Bad400
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val radius = (size.toPx() - strokePx) / 2f

            // Draw Background Track Circle
            drawCircle(
                color = trackColor,
                radius = radius,
                style = Stroke(width = strokePx)
            )

            // Draw Animated Progress Arc
            if (animatedProgress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(
                        width = strokePx,
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        content?.invoke()
    }
}
