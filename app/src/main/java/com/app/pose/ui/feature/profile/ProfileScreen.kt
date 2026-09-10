package com.app.pose.ui.feature.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.pose.ui.components.AppButton
import com.app.pose.ui.components.AppButtonVariant
import com.app.pose.ui.components.AppCard
import com.app.pose.ui.components.AppChip
import com.app.pose.ui.components.ChipTone
import com.app.pose.ui.components.SectionHeader
import com.app.pose.ui.theme.Accent500
import com.app.pose.ui.theme.CanvasBackground
import com.app.pose.ui.theme.Ink600
import com.app.pose.ui.theme.Ink900
import com.app.pose.ui.theme.LineBorder
import com.app.pose.ui.theme.MutedText
import com.app.pose.ui.theme.SurfaceWhite

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.userProfile

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .statusBarsPadding()
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.displaySmall,
                color = Ink900
            )
        }

        // Scrollable Settings
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // User Profile Card
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Accent500),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = SurfaceWhite,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user?.fullName ?: "Maya Bennett",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Ink900
                        )
                        Text(
                            text = "${user?.goal ?: "Build strength"} · ${user?.level ?: "Intermediate"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            AppChip(
                                text = "${user?.streak ?: 6}-day streak",
                                tone = ChipTone.ACCENT
                            )
                            AppChip(
                                text = "Level ${user?.levelNumber ?: 4}",
                                tone = ChipTone.DEFAULT
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MutedText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Training Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(title = "Training")
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 0.dp
                ) {
                    Column {
                        SettingNavigationRow(
                            icon = Icons.Default.TrackChanges,
                            title = "Workout goals",
                            value = "4 sessions / week"
                        )
                        Divider()
                        SettingNavigationRow(
                            icon = Icons.Default.FitnessCenter,
                            title = "Fitness preferences",
                            value = "Strength, Mobility"
                        )
                        Divider()
                        SettingSegmentRow(
                            icon = Icons.Default.Straighten,
                            title = "Units",
                            options = listOf("Metric", "Imperial"),
                            selected = uiState.unitSystem,
                            onSelect = { viewModel.setUnitSystem(it) }
                        )
                    }
                }
            }

            // Camera & Feedback Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(title = "Camera & feedback")
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 0.dp
                ) {
                    Column {
                        SettingNavigationRow(
                            icon = Icons.Default.Videocam,
                            title = "Camera settings",
                            value = "Front · 1080p"
                        )
                        Divider()
                        SettingSwitchRow(
                            icon = Icons.Default.VolumeUp,
                            title = "Voice coaching",
                            subtitle = "Spoken cues during your set",
                            checked = uiState.isVoiceCoachingEnabled,
                            onCheckedChange = { viewModel.toggleVoiceCoaching() }
                        )
                        Divider()
                        SettingSwitchRow(
                            icon = Icons.Default.Vibration,
                            title = "Haptic rep feedback",
                            subtitle = "Buzz on each valid rep",
                            checked = uiState.isHapticsEnabled,
                            onCheckedChange = { viewModel.toggleHaptics() }
                        )
                        Divider()
                        SettingNavigationRow(
                            icon = Icons.Default.Notifications,
                            title = "Reminders",
                            value = "Weekdays, 7:00"
                        )
                    }
                }
            }

            // Privacy & Account
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(title = "Privacy & account")
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = 0.dp
                ) {
                    Column {
                        SettingSwitchRow(
                            icon = Icons.Default.Security,
                            title = "Save workout video",
                            subtitle = "Off — pose analysis runs on device",
                            checked = uiState.isSaveVideoEnabled,
                            onCheckedChange = { viewModel.toggleSaveVideo() }
                        )
                        Divider()
                        SettingNavigationRow(
                            icon = Icons.Default.Security,
                            title = "Privacy policy"
                        )
                    }
                }
            }

            // Sign Out Action
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppButton(
                    text = "Sign out",
                    onClick = {},
                    variant = AppButtonVariant.DANGER,
                    leadingIcon = Icons.Default.ExitToApp,
                    fullWidth = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "form.ai · version 2.4.0 (Native Android)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingNavigationRow(
    icon: ImageVector,
    title: String,
    value: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFECE7DE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Ink600,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Ink900
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedText
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MutedText,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFECE7DE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Ink600,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink900
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedText
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SurfaceWhite,
                checkedTrackColor = Accent500,
                uncheckedThumbColor = SurfaceWhite,
                uncheckedTrackColor = LineBorder
            )
        )
    }
}

@Composable
private fun SettingSegmentRow(
    icon: ImageVector,
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFECE7DE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Ink600,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Ink900
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFECE7DE))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            options.forEach { opt ->
                val isOptSelected = opt == selected
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isOptSelected) SurfaceWhite else Color.Transparent)
                        .clickable { onSelect(opt) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = opt,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOptSelected) Ink900 else MutedText
                    )
                }
            }
        }
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(LineBorder.copy(alpha = 0.6f))
    )
}
