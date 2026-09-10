package com.app.pose.ui.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.pose.ui.components.AppButton
import com.app.pose.ui.components.AppCard
import com.app.pose.ui.components.AppChip
import com.app.pose.ui.components.AppScoreRing
import com.app.pose.ui.components.CardTone
import com.app.pose.ui.components.ChipTone
import com.app.pose.ui.components.SectionHeader
import com.app.pose.ui.components.StatCard
import com.app.pose.ui.theme.Accent500
import com.app.pose.ui.theme.Bad50
import com.app.pose.ui.theme.Bad500
import com.app.pose.ui.theme.CanvasBackground
import com.app.pose.ui.theme.Good50
import com.app.pose.ui.theme.Good600
import com.app.pose.ui.theme.HeroCardShape
import com.app.pose.ui.theme.Ink600
import com.app.pose.ui.theme.Ink900
import com.app.pose.ui.theme.LineBorder
import com.app.pose.ui.theme.MutedText
import com.app.pose.ui.theme.SurfaceWhite
import com.app.pose.ui.theme.Warn50
import com.app.pose.ui.theme.Warn500

@Composable
fun HomeScreen(
    onOpenExercise: (String) -> Unit,
    onStartExercise: (String) -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.userProfile
    val today = uiState.todaySummary
    val pct = if (today != null && today.planned > 0) (today.completed * 100) / today.planned else 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Good morning, ${user?.firstName ?: "Maya"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
                Text(
                    text = "Ready to train?",
                    style = MaterialTheme.typography.displaySmall,
                    color = Ink900
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Ink600,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Accent500)
                        .clickable { onNavigateToProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = SurfaceWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Main Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Progress Card
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                AppCard(
                    tone = CardTone.DARK,
                    shape = HeroCardShape,
                    padding = 20.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AppScoreRing(
                                progressPercent = pct,
                                size = 88.dp,
                                strokeWidth = 9.dp,
                                onDark = true
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = "${today?.completed ?: 3}",
                                            style = MaterialTheme.typography.headlineLarge,
                                            color = SurfaceWhite
                                        )
                                        Text(
                                            text = "/${today?.planned ?: 5}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.5f),
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = "EXERCISES",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Today's Progress",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = SurfaceWhite
                                )
                                Text(
                                    text = "${today?.completed ?: 3} exercises completed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    AppChip(
                                        text = "${today?.minutes ?: 18} min",
                                        tone = ChipTone.DARK,
                                        icon = Icons.Default.Timer
                                    )
                                    AppChip(
                                        text = "${today?.calories ?: 214} kcal",
                                        tone = ChipTone.DARK,
                                        icon = Icons.Default.Whatshot
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AppButton(
                                text = "Start Workout",
                                onClick = { onStartExercise("squat") },
                                leadingIcon = Icons.Default.CameraAlt,
                                fullWidth = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Next up · Squat · 12 reps with live form check",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // Quick Stats Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    label = "Form",
                    value = "${today?.formScore ?: 88}",
                    unit = "%",
                    delta = "+4 this week",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Streak",
                    value = "${user?.streak ?: 6}",
                    unit = "d",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Reps",
                    value = "${uiState.totalReps}",
                    delta = "+32",
                    modifier = Modifier.weight(1f)
                )
            }

            // Weekly Activity
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionHeader(
                    title = "This week",
                    action = "Details",
                    onAction = onNavigateToProgress
                )

                AppCard(modifier = Modifier.fillMaxWidth()) {
                    val maxMinutes = (uiState.weeklyActivity.maxOfOrNull { it.minutes } ?: 34).coerceAtLeast(1)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        uiState.weeklyActivity.forEachIndexed { i, day ->
                            val isCurrent = i == uiState.weeklyActivity.size - 1
                            val barHeightFraction = (day.minutes.toFloat() / maxMinutes).coerceIn(0.08f, 1f)
                            val barColor = if (isCurrent) Accent500 else if (day.active) Ink900 else LineBorder

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .fillMaxWidth(0.6f)
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

            // Recommended For You
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    SectionHeader(
                        title = "Recommended for you",
                        action = "All",
                        onAction = onNavigateToLibrary
                    )
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp)
                ) {
                    items(uiState.recommendedExercises) { ex ->
                        AppCard(
                            modifier = Modifier
                                .width(152.dp)
                                .clickable { onOpenExercise(ex.id) },
                            padding = 12.dp
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(96.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFEFECE6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ex.iconEmoji,
                                        fontSize = 40.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = ex.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Ink900,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${ex.durationMin} min · ${ex.difficulty.label}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedText,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Recent Activity
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionHeader(
                    title = "Recent activity",
                    action = "History",
                    onAction = onNavigateToProgress
                )

                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 0.dp
                ) {
                    Column {
                        uiState.recentActivity.forEachIndexed { index, item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenExercise(item.exerciseId) }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val (tagBg, tagText) = when {
                                    item.formScore >= 85 -> Pair(Good50, Good600)
                                    item.formScore >= 75 -> Pair(Warn50, Warn500)
                                    else -> Pair(Bad50, Bad500)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(tagBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${item.formScore}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = tagText
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Ink900
                                        )
                                        Text(
                                            text = "${item.whenTime} · ${item.reps} reps",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MutedText
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MutedText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            if (index < uiState.recentActivity.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(LineBorder.copy(alpha = 0.6f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
