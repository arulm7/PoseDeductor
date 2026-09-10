package com.app.pose.ui.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.app.pose.domain.model.Difficulty
import com.app.pose.ui.components.AppButton
import com.app.pose.ui.components.AppCard
import com.app.pose.ui.components.AppChip
import com.app.pose.ui.components.CardTone
import com.app.pose.ui.components.ChipTone
import com.app.pose.ui.theme.Bad50
import com.app.pose.ui.theme.Bad500
import com.app.pose.ui.theme.CanvasBackground
import com.app.pose.ui.theme.Good500
import com.app.pose.ui.theme.Good600
import com.app.pose.ui.theme.HeroCardShape
import com.app.pose.ui.theme.Ink600
import com.app.pose.ui.theme.Ink900
import com.app.pose.ui.theme.MutedText
import com.app.pose.ui.theme.SurfaceWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    onBack: () -> Unit,
    onStart: (String) -> Unit,
    viewModel: ExerciseDetailViewModel = viewModel()
) {
    val exercise by viewModel.exercise.collectAsState()

    LaunchedEffect(exerciseId) {
        viewModel.loadExercise(exerciseId)
    }

    val current = exercise ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceWhite)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Ink900,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = current.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Ink900
            )
        }

        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Illustration Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(HeroCardShape)
                    .background(Color(0xFFECE7DE)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = current.iconEmoji,
                    fontSize = 84.sp
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Ink900.copy(alpha = 0.85f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = SurfaceWhite,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Watch the motion",
                        style = MaterialTheme.typography.labelSmall,
                        color = SurfaceWhite
                    )
                }
            }

            // Title & Quick Meta
            Column {
                Text(
                    text = current.name,
                    style = MaterialTheme.typography.displaySmall,
                    color = Ink900
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val diffTone = when (current.difficulty) {
                        Difficulty.BEGINNER -> ChipTone.GOOD
                        Difficulty.INTERMEDIATE -> ChipTone.WARN
                        Difficulty.ADVANCED -> ChipTone.BAD
                    }
                    AppChip(
                        text = current.difficulty.label,
                        tone = diffTone,
                        icon = Icons.Default.Whatshot
                    )
                    AppChip(
                        text = "${current.durationMin} min",
                        tone = ChipTone.DEFAULT,
                        icon = Icons.Default.Timer
                    )
                    AppChip(
                        text = "${current.targetReps} reps",
                        tone = ChipTone.DEFAULT,
                        icon = Icons.Default.Repeat
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = current.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink600
                )
            }

            // Target Muscles
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Target muscles",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink900
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    current.muscles.forEach { muscle ->
                        AppChip(text = muscle)
                    }
                }
            }

            // How To Instructions
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "How to perform it",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink900
                )
                current.instructions.forEachIndexed { i, step ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Ink900),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${i + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SurfaceWhite
                            )
                        }
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink600,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Correct Form Callout Card
            AppCard(
                tone = CardTone.GOOD,
                modifier = Modifier.fillMaxWidth(),
                padding = 16.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Good500),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = SurfaceWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Correct form",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Ink900
                        )
                    }

                    Text(
                        text = "Coach AI checks these points on every rep.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedText
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        current.checkpoints.forEach { chk ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Good600,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = chk,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Ink900
                                )
                            }
                        }
                    }
                }
            }

            // Common Mistakes
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Common mistakes",
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink900
                )
                current.mistakes.forEach { m ->
                    AppCard(
                        modifier = Modifier.fillMaxWidth(),
                        padding = 14.dp
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Bad50),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Bad500,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = m.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Ink900
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = m.detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedText
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sticky Bottom Action CTA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceWhite)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AppButton(
                    text = "Start Camera",
                    onClick = { onStart(current.id) },
                    leadingIcon = Icons.Default.CameraAlt,
                    fullWidth = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Place your phone so your whole body is in frame",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
            }
        }
    }
}
