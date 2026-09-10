package com.app.pose.ui.feature.camera.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pose.ui.theme.Bad400
import com.app.pose.ui.theme.Good400
import com.app.pose.ui.theme.SurfaceWhite
import com.app.pose.ui.theme.Warn400

@Composable
fun RepCounterHud(
    reps: Int,
    target: Int,
    correctReps: Int,
    formScore: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // Large Rep Count
        Column {
            Text(
                text = "REP",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f)
            )
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = reps.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 52.sp, lineHeight = 52.sp),
                    color = SurfaceWhite
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "/ $target",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.alignByBaseline()
                )
            }
        }

        // Secondary Metrics
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "VALID",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$correctReps",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SurfaceWhite
                )
            }

            val scoreColor = when {
                formScore >= 85 -> Good400
                formScore >= 70 -> Warn400
                else -> Bad400
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "FORM SCORE",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$formScore%",
                    style = MaterialTheme.typography.headlineLarge,
                    color = scoreColor
                )
            }
        }
    }
}
