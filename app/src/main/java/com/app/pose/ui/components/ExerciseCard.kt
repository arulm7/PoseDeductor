package com.app.pose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pose.domain.model.Difficulty
import com.app.pose.domain.model.Exercise
import com.app.pose.ui.theme.Accent500
import com.app.pose.ui.theme.CardShape
import com.app.pose.ui.theme.ImageThumbShape
import com.app.pose.ui.theme.Ink900
import com.app.pose.ui.theme.MutedText
import com.app.pose.ui.theme.SurfaceWhite

@Composable
fun ExerciseCard(
    exercise: Exercise,
    onOpen: (String) -> Unit,
    onStart: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        shape = CardShape,
        padding = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpen(exercise.id) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exercise Thumbnail Icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(ImageThumbShape)
                    .background(Color(0xFFEFECE6)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = exercise.iconEmoji,
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Exercise Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Ink900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = exercise.muscles.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val diffTone = when (exercise.difficulty) {
                        Difficulty.BEGINNER -> ChipTone.GOOD
                        Difficulty.INTERMEDIATE -> ChipTone.WARN
                        Difficulty.ADVANCED -> ChipTone.BAD
                    }
                    AppChip(
                        text = exercise.difficulty.label,
                        tone = diffTone,
                        icon = Icons.Default.Whatshot
                    )
                    AppChip(
                        text = "${exercise.durationMin} min",
                        tone = ChipTone.DEFAULT,
                        icon = Icons.Default.Timer
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Start Camera Play Action Button
            IconButton(
                onClick = { onStart(exercise.id) },
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Ink900)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start ${exercise.name} with AI Coach",
                    tint = SurfaceWhite,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
