package com.app.pose.ui.feature.camera.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.app.pose.ui.theme.Bad400
import com.app.pose.ui.theme.Bad500
import com.app.pose.ui.theme.Ink900
import com.app.pose.ui.theme.SurfaceWhite

@Composable
fun CameraControlsBar(
    paused: Boolean,
    sound: Boolean,
    onTogglePause: () -> Unit,
    onToggleSound: () -> Unit,
    onFlip: () -> Unit,
    onEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mute / Unmute
        ControlButton(
            icon = if (sound) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
            contentDescription = if (sound) "Mute voice coach" else "Unmute voice coach",
            onClick = onToggleSound
        )

        // Switch Camera
        ControlButton(
            icon = Icons.Default.Cameraswitch,
            contentDescription = "Switch camera",
            onClick = onFlip
        )

        // Play / Pause Main Trigger
        val playInteraction = remember { MutableInteractionSource() }
        val playPressed by playInteraction.collectIsPressedAsState()
        val playScale by animateFloatAsState(
            targetValue = if (playPressed) 0.93f else 1f,
            label = "playBtnScale"
        )

        Box(
            modifier = Modifier
                .size(68.dp)
                .scale(playScale)
                .clip(CircleShape)
                .background(SurfaceWhite)
                .clickable(
                    interactionSource = playInteraction,
                    indication = null,
                    onClick = onTogglePause
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (paused) Icons.Default.PlayArrow else Icons.Default.Pause,
                contentDescription = if (paused) "Resume workout" else "Pause workout",
                tint = Ink900,
                modifier = Modifier.size(32.dp)
            )
        }

        // Stop / End Workout
        ControlButton(
            icon = Icons.Default.Stop,
            contentDescription = "End workout",
            onClick = onEnd,
            isDanger = true
        )

        // HD Badge
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)), CircleShape)
                .clickable(onClick = onFlip),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "HD",
                style = MaterialTheme.typography.labelSmall,
                color = SurfaceWhite
            )
        }
    }
}

@Composable
private fun ControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    isDanger: Boolean = false
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        label = "ctrlBtnScale"
    )

    val bg = if (isDanger) Bad500.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f)
    val tint = if (isDanger) Bad400 else SurfaceWhite
    val borderClr = if (isDanger) Bad400.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.2f)

    Box(
        modifier = Modifier
            .size(48.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(bg)
            .border(BorderStroke(1.dp, borderClr), CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}
