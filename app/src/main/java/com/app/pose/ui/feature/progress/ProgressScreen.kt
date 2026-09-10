package com.app.pose.ui.feature.progress

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.pose.ui.components.AppCard
import com.app.pose.ui.components.AppProgressBar
import com.app.pose.ui.components.CardTone
import com.app.pose.ui.components.FilterChip
import com.app.pose.ui.components.ProgressTone
import com.app.pose.ui.components.SectionHeader
import com.app.pose.ui.components.StatCard
import com.app.pose.ui.feature.progress.components.FormTrendChart
import com.app.pose.ui.theme.Accent500
import com.app.pose.ui.theme.CanvasBackground
import com.app.pose.ui.theme.Good50
import com.app.pose.ui.theme.Good400
import com.app.pose.ui.theme.Good600
import com.app.pose.ui.theme.Ink800
import com.app.pose.ui.theme.Ink900
import com.app.pose.ui.theme.LineBorder
import com.app.pose.ui.theme.MutedText
import com.app.pose.ui.theme.SurfaceWhite
import com.app.pose.ui.theme.Warn50
import com.app.pose.ui.theme.Warn500

private val timeRanges = listOf("Week", "Month", "Year")

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val best = uiState.exercisePerformance.maxByOrNull { it.score }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .statusBarsPadding()
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.displaySmall,
                color = Ink900
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Your form is trending up — keep going.",
                style = MaterialTheme.typography.bodySmall,
                color = MutedText
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Range Selectors
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                timeRanges.forEach { range ->
                    FilterChip(
                        label = range,
                        selected = uiState.selectedRange == range,
                        onClick = { viewModel.onRangeSelected(range) }
                    )
                }
            }
        }

        // Scrollable Metrics & Charts
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // High level 3-card stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    label = "Sessions",
                    value = "${uiState.sessionsCount}",
                    delta = "+3",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Avg form",
                    value = "${uiState.avgFormScore}",
                    unit = "%",
                    delta = "+9",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Reps",
                    value = "${uiState.totalReps}",
                    delta = "+112",
                    modifier = Modifier.weight(1f)
                )
            }

            // Form Score Trend Chart Card
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(title = "Form score trend")
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "88%",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = Ink900
                                )
                                Text(
                                    text = "6-week average quality",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedText
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Good50)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NorthEast,
                                    contentDescription = null,
                                    tint = Good600,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "+20 pts",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Good600
                                )
                            }
                        }

                        FormTrendChart(trendItems = uiState.formTrend)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            uiState.formTrend.forEach { item ->
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedText
                                )
                            }
                        }
                    }
                }
            }

            // Weekly Activity Distribution
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(title = "Weekly activity")
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    val maxMinutes = (uiState.weeklyActivity.maxOfOrNull { it.minutes } ?: 34).coerceAtLeast(1)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        uiState.weeklyActivity.forEachIndexed { i, day ->
                            val isCurrent = i == uiState.weeklyActivity.size - 1
                            val barHeightFraction = (day.minutes.toFloat() / maxMinutes).coerceIn(0.08f, 1f)
                            val barColor = if (isCurrent) Accent500 else if (day.active) Ink800 else LineBorder

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (day.minutes > 0) {
                                    Text(
                                        text = "${day.minutes}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedText
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .fillMaxWidth(0.65f)
                                        .height((80 * barHeightFraction).dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(barColor)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = day.day,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedText
                                )
                            }
                        }
                    }
                }
            }

            // Exercise Performance
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(title = "Exercise performance")
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        uiState.exercisePerformance.forEach { ex ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ex.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Ink900
                                    )
                                    Text(
                                        text = "${ex.score}%",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Ink900
                                    )
                                }

                                val tone = when {
                                    ex.score >= 85 -> ProgressTone.GOOD
                                    ex.score >= 75 -> ProgressTone.ACCENT
                                    else -> ProgressTone.WARN
                                }
                                AppProgressBar(
                                    progressPercent = ex.score,
                                    tone = tone,
                                    height = 6.dp
                                )
                            }
                        }
                    }
                }
            }

            // Coach Insights
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(title = "Coach insights")

                if (best != null) {
                    AppCard(
                        tone = CardTone.DARK,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Good400.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = Good400,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "BEST PERFORMING",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "${best.name} · ${best.score}% form",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = SurfaceWhite
                                )
                            }
                        }
                    }
                }

                uiState.improvementAreas.forEach { area ->
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Warn50)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = area.metric,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Warn500
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = area.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Ink900
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = area.detail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedText
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
