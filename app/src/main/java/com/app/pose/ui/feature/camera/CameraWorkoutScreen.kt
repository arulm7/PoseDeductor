package com.app.pose.ui.feature.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.pose.camera.PoseLandmarkerHelper
import com.app.pose.domain.model.CameraState
import com.app.pose.domain.model.WorkoutResult
import com.app.pose.ui.components.AppButton
import com.app.pose.ui.components.AppButtonVariant
import com.app.pose.ui.feature.camera.components.CameraControlsBar
import com.app.pose.ui.feature.camera.components.CameraPreview
import com.app.pose.ui.feature.camera.components.CoachCard
import com.app.pose.ui.feature.camera.components.FormStatusBadge
import com.app.pose.ui.feature.camera.components.FramingGuideBox
import com.app.pose.ui.feature.camera.components.PoseSkeletonCanvas
import com.app.pose.ui.feature.camera.components.RepCounterHud
import com.app.pose.ui.theme.Good400
import com.app.pose.ui.theme.Ink900
import com.app.pose.ui.theme.MutedText
import com.app.pose.ui.theme.SurfaceWhite
import com.app.pose.ui.theme.Warn400

@Composable
fun CameraWorkoutScreen(
    exerciseId: String,
    onExit: () -> Unit,
    onFinish: (WorkoutResult) -> Unit,
    viewModel: CameraWorkoutViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onCameraPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.onCameraPermissionResult(hasPermission)
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(exerciseId) {
        viewModel.initializeSession(exerciseId)
    }

    val poseLandmarkerHelper = remember {
        PoseLandmarkerHelper(
            context = context,
            onPoseDetected = { points, timestamp ->
                viewModel.onPoseLandmarksDetected(points, timestamp)
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            poseLandmarkerHelper.close()
        }
    }

    val exercise = uiState.exercise

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink900)
    ) {
        // Real CameraX Live Preview or fallback gradient
        if (uiState.isCameraPermissionGranted) {
            CameraPreview(
                isFrontCamera = uiState.isFrontCamera,
                onFrameAnalyzed = { imageProxy, isFront ->
                    viewModel.processCameraFrame(imageProxy, isFront, poseLandmarkerHelper)
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Gradient placeholder when camera permission is requested
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1B2027),
                                Color(0xFF0F141A),
                                Color(0xFF0A0D10)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = SurfaceWhite,
                        modifier = Modifier.size(54.dp)
                    )
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SurfaceWhite
                    )
                    Text(
                        text = "Grant camera access so Coach AI can analyze your movement in real time.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    AppButton(
                        text = "Grant Camera Permission",
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                    )
                }
            }
        }

        // Camera Scrim Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Ink900.copy(alpha = 0.75f),
                            Color.Transparent,
                            Ink900.copy(alpha = 0.90f)
                        )
                    )
                )
        )

        // Live Real Pose Skeleton Overlay
        PoseSkeletonCanvas(
            landmarks = uiState.landmarks,
            state = uiState.state,
            modifier = Modifier.fillMaxSize()
        )

        // Calibration Framing Guide
        FramingGuideBox(state = uiState.state)

        // Camera Screen HUD
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top HUD Header
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onExit,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f))
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Exit workout",
                                tint = SurfaceWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = (exercise?.name ?: "Workout").uppercase(),
                                style = MaterialTheme.typography.headlineLarge,
                                color = SurfaceWhite
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = formatTime(uiState.elapsedSeconds),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Memory,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Coach AI on",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    FormStatusBadge(state = uiState.state)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Segmented Reps Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (i in 0 until uiState.targetReps) {
                        val segmentColor = when {
                            i < uiState.correctReps -> Good400
                            i < uiState.reps -> Warn400
                            else -> Color.White.copy(alpha = 0.2f)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(segmentColor)
                        )
                    }
                }
            }

            // Bottom HUD: Coach Card + Counter Box + Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Live AI Diagnostics: TFLite + Movement Rep Engine
                if (uiState.predictedClass != null || uiState.landmarks.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Ink900.copy(alpha = 0.90f))
                            .border(1.dp, Good400.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Exercise: ${uiState.exercise?.name ?: "Squats"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "Reps: ${uiState.reps} / ${uiState.targetReps}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Good400
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Class: ${uiState.predictedClass ?: "detecting..."} (${"%.1f".format(uiState.predictedConfidence * 100)}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Good400
                                )
                                Text(
                                    text = "State: ${uiState.repState}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Knee Angle: ${"%.1f".format(uiState.kneeAngle)}°",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = "Movement Δ: ${"%.1f".format(uiState.movementFromBaseline)}°",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                }

                CoachCard(
                    state = uiState.state,
                    cue = uiState.cue,
                    reps = uiState.reps,
                    targetReps = uiState.targetReps
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Ink900.copy(alpha = 0.85f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
                        .padding(18.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RepCounterHud(
                            reps = uiState.reps,
                            target = uiState.targetReps,
                            correctReps = uiState.correctReps,
                            formScore = uiState.formScore
                        )

                        if (uiState.state == CameraState.COMPLETE) {
                            AppButton(
                                text = "See your results",
                                onClick = { onFinish(viewModel.getWorkoutResult()) },
                                fullWidth = true
                            )
                        } else {
                            CameraControlsBar(
                                paused = uiState.isPaused,
                                sound = uiState.isSoundOn,
                                onTogglePause = { viewModel.togglePause() },
                                onToggleSound = { viewModel.toggleSound() },
                                onFlip = { viewModel.flipCamera() },
                                onEnd = { onFinish(viewModel.getWorkoutResult()) }
                            )
                        }
                    }
                }
            }
        }

        // Workout Paused Overlay
        AnimatedVisibility(
            visible = uiState.isPaused,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Ink900.copy(alpha = 0.85f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(SurfaceWhite)
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Workout paused",
                            style = MaterialTheme.typography.displaySmall,
                            color = Ink900
                        )
                        Text(
                            text = "${uiState.reps} of ${uiState.targetReps} reps done · ${uiState.formScore}% form score",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedText
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AppButton(
                            text = "Resume",
                            onClick = { viewModel.togglePause() },
                            fullWidth = true
                        )
                        AppButton(
                            text = "End workout",
                            onClick = { onFinish(viewModel.getWorkoutResult()) },
                            variant = AppButtonVariant.SECONDARY,
                            fullWidth = true
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}
