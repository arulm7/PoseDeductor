package com.app.pose.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.pose.ui.theme.Accent500
import com.app.pose.ui.theme.Bad500
import com.app.pose.ui.theme.Good500
import com.app.pose.ui.theme.LineBorder
import com.app.pose.ui.theme.Warn500

enum class ProgressTone {
    ACCENT,
    GOOD,
    WARN,
    BAD
}

@Composable
fun AppProgressBar(
    progressPercent: Int,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    tone: ProgressTone = ProgressTone.ACCENT,
    trackColor: Color = LineBorder.copy(alpha = 0.6f)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (progressPercent.coerceIn(0, 100)) / 100f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progressBarAnim"
    )

    val progressColor = when (tone) {
        ProgressTone.ACCENT -> Accent500
        ProgressTone.GOOD -> Good500
        ProgressTone.WARN -> Warn500
        ProgressTone.BAD -> Bad500
    }

    val shape = RoundedCornerShape(height / 2)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .clip(shape)
                .background(progressColor)
        )
    }
}
