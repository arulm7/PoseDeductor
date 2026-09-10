package com.app.pose.ui.feature.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.pose.ui.components.AppButton
import com.app.pose.ui.components.AppButtonVariant
import com.app.pose.ui.components.AppCard
import com.app.pose.ui.components.AppProgressBar
import com.app.pose.ui.components.AppScoreRing
import com.app.pose.ui.components.CardTone
import com.app.pose.ui.components.ProgressTone
import com.app.pose.ui.components.RingTone
import com.app.pose.ui.components.StatCard
import com.app.pose.ui.theme.Accent500
import com.app.pose.ui.theme.Bad400
import com.app.pose.ui.theme.CanvasBackground
import com.app.pose.ui.theme.Good400
import com.app.pose.ui.theme.Good500
import com.app.pose.ui.theme.Ink900
import com.app.pose.ui.theme.MutedText
import com.app.pose.ui.theme.SurfaceWhite
import com.app.pose.ui.theme.Warn400

@Composable
fun WorkoutSummaryScreen(
    exerciseId: String,
    reps: Int,
    correctReps: Int,
    formScore: Int,
    durationSec: Int,
    onContinue: () -> Unit,
    onViewProgress: () -> Unit,
    viewModel: WorkoutSummaryViewModel = viewModel()
) {
    val result by viewModel.result.collectAsState()

    LaunchedEffect(exerciseId, reps, correctReps, formScore, durationSec) {
        viewModel.setResult(exerciseId, reps, correctReps, formScore, durationSec)
    }

    val currentResult = result ?: return
    val accuracy = if (currentResult.reps > 0) (currentResult.correctReps * 100) / currentResult.reps else 100
    val scoreTone = when {
        currentResult.formScore >= 85 -> RingTone.GOOD
        currentResult.formScore >= 70 -> RingTone.WARN
        else -> RingTone.BAD
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBackground)
    ) {
        // Dark Hero Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Ink900)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Completion Badge
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Good500.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Good400,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "WORKOUT COMPLETE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Good400
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${currentResult.exerciseName} done",
                style = MaterialTheme.typography.displaySmall,
                color = SurfaceWhite
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${currentResult.reps} reps · ${formatDuration(currentResult.durationSec)} · Coach AI analysed every rep",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Large Form Score Ring
            AppScoreRing(
                progressPercent = currentResult.formScore,
                size = 140.dp,
                strokeWidth = 12.dp,
                tone = scoreTone,
                onDark = true
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${currentResult.formScore}",
                            style = MaterialTheme.typography.displayMedium,
                            color = SurfaceWhite
                        )
                        Text(
                            text = "%",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        text = "FORM SCORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // Body Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(CanvasBackground)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    label = "Reps",
                    value = "${currentResult.reps}",
                    icon = Icons.Default.Repeat,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Correct",
                    value = "${currentResult.correctReps}",
                    icon = Icons.Default.TrackChanges,
                    tone = CardTone.GOOD,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Time",
                    value = formatDuration(currentResult.durationSec),
                    icon = Icons.Default.Timer,
                    modifier = Modifier.weight(1f)
                )
            }

            // Accuracy Card
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rep accuracy",
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink900
                        )
                        Text(
                            text = "$accuracy%",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Ink900
                        )
                    }

                    AppProgressBar(
                        progressPercent = accuracy,
                        tone = if (accuracy >= 85) ProgressTone.GOOD else ProgressTone.WARN
                    )

                    Text(
                        text = "${currentResult.correctReps} of ${currentResult.reps} reps met every form checkpoint.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )
                }
            }

            // Next Set AI Tip Focus Card
            AppCard(
                tone = CardTone.ACCENT,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Accent500),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = SurfaceWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Focus for your next set",
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink900
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentResult.focus.startsWith("Keep", ignoreCase = true)) {
                                "${currentResult.focus} on your next set — it slipped on ${(currentResult.reps - currentResult.correctReps).coerceAtLeast(1)} reps."
                            } else {
                                currentResult.focus
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink900
                        )
                    }
                }
            }
        }

        // Bottom Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppButton(
                text = "Continue",
                onClick = onContinue,
                fullWidth = true
            )
            AppButton(
                text = "View Progress",
                onClick = onViewProgress,
                variant = AppButtonVariant.SECONDARY,
                fullWidth = true
            )
        }
    }
}

private fun formatDuration(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}
