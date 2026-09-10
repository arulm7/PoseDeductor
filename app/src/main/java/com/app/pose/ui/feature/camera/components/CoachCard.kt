package com.app.pose.ui.feature.camera.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.app.pose.domain.model.CameraState
import com.app.pose.ui.theme.Accent500
import com.app.pose.ui.theme.Bad500
import com.app.pose.ui.theme.Good400
import com.app.pose.ui.theme.Good500
import com.app.pose.ui.theme.Good600
import com.app.pose.ui.theme.Ink900
import com.app.pose.ui.theme.SurfaceWhite
import com.app.pose.ui.theme.Warn400

@Composable
fun CoachCard(
    state: CameraState,
    cue: String?,
    reps: Int,
    targetReps: Int,
    modifier: Modifier = Modifier
) {
    val (eyebrow, title, icon, shellBg, iconBg, eyebrowColor, titleColor, borderColor) = when (state) {
        CameraState.READY -> CoachCardContent(
            eyebrow = "GET SET",
            title = "Position yourself in frame",
            icon = Icons.Default.PersonSearch,
            shellBg = Ink900.copy(alpha = 0.75f),
            iconBg = Color.White.copy(alpha = 0.15f),
            eyebrowColor = Color.White.copy(alpha = 0.6f),
            titleColor = SurfaceWhite,
            borderColor = Color.White.copy(alpha = 0.15f)
        )
        CameraState.TRACKING -> CoachCardContent(
            eyebrow = "BODY DETECTED",
            title = "Ready when you are",
            icon = Icons.Default.AutoAwesome,
            shellBg = SurfaceWhite.copy(alpha = 0.95f),
            iconBg = Good400.copy(alpha = 0.25f),
            eyebrowColor = Good600,
            titleColor = Ink900,
            borderColor = SurfaceWhite
        )
        CameraState.CORRECT -> CoachCardContent(
            eyebrow = "PERFECT FORM",
            title = "Rep ${reps.toString().padStart(2, '0')} counted",
            icon = Icons.Default.CheckCircle,
            shellBg = Good500.copy(alpha = 0.95f),
            iconBg = Color.White.copy(alpha = 0.25f),
            eyebrowColor = Color.White.copy(alpha = 0.8f),
            titleColor = SurfaceWhite,
            borderColor = Color.White.copy(alpha = 0.3f)
        )
        CameraState.INCORRECT -> CoachCardContent(
            eyebrow = "CORRECT YOUR FORM",
            title = cue ?: "Keep your knees aligned",
            icon = Icons.Default.Warning,
            shellBg = Warn400,
            iconBg = Ink900.copy(alpha = 0.15f),
            eyebrowColor = Ink900.copy(alpha = 0.7f),
            titleColor = Ink900,
            borderColor = Warn400
        )
        CameraState.LOST -> CoachCardContent(
            eyebrow = "BODY NOT DETECTED",
            title = "Step back so your full body is visible",
            icon = Icons.Default.PersonSearch,
            shellBg = Bad500.copy(alpha = 0.95f),
            iconBg = Color.White.copy(alpha = 0.25f),
            eyebrowColor = Color.White.copy(alpha = 0.8f),
            titleColor = SurfaceWhite,
            borderColor = Color.White.copy(alpha = 0.3f)
        )
        CameraState.COMPLETE -> CoachCardContent(
            eyebrow = "GREAT JOB!",
            title = "$targetReps / $targetReps reps completed",
            icon = Icons.Default.Celebration,
            shellBg = Accent500,
            iconBg = Color.White.copy(alpha = 0.25f),
            eyebrowColor = Color.White.copy(alpha = 0.8f),
            titleColor = SurfaceWhite,
            borderColor = Color.White.copy(alpha = 0.3f)
        )
    }

    val cardShape = RoundedCornerShape(24.dp)

    AnimatedContent(
        targetState = Pair(state, title),
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "coachCardAnim",
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(shellBg)
                .border(BorderStroke(1.dp, borderColor), cardShape)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = titleColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = eyebrow,
                    style = MaterialTheme.typography.labelSmall,
                    color = eyebrowColor
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = titleColor
                )
            }
        }
    }
}

private data class CoachCardContent(
    val eyebrow: String,
    val title: String,
    val icon: ImageVector,
    val shellBg: Color,
    val iconBg: Color,
    val eyebrowColor: Color,
    val titleColor: Color,
    val borderColor: Color
)
