package com.app.pose.ui.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pose.ui.components.AppButton
import com.app.pose.ui.theme.Accent500
import com.app.pose.ui.theme.Ink900
import com.app.pose.ui.theme.SurfaceWhite

data class OnboardingSlide(
    val eyebrow: String,
    val title: String,
    val body: String,
    val emoji: String
)

private val slides = listOf(
    OnboardingSlide(
        eyebrow = "COACH AI",
        title = "A coach that\nactually watches",
        body = "Pose estimation reads 14 body landmarks through your camera and checks every rep against correct technique.",
        emoji = "🤖"
    ),
    OnboardingSlide(
        eyebrow = "REAL TIME",
        title = "Corrections\nas you move",
        body = "The moment your knees cave in or your back rounds, you get a short, specific cue — no video review needed.",
        emoji = "⚡"
    ),
    OnboardingSlide(
        eyebrow = "SMART COUNTING",
        title = "Only clean\nreps count",
        body = "Half reps and rushed tempo are flagged, not counted. Your form score tracks the quality of your training.",
        emoji = "🎯"
    )
)

@Composable
fun OnboardingScreen(
    onDone: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    val isLast = currentIndex == slides.size - 1
    val currentSlide = slides[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Ink900)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Brand Header + Skip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "form",
                    style = MaterialTheme.typography.headlineLarge,
                    color = SurfaceWhite
                )
                Text(
                    text = ".ai",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Accent500
                )
            }

            if (!isLast) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.clickable { onDone() }
                )
            }
        }

        // Slide Content Carousel
        AnimatedContent(
            targetState = currentSlide,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "onboardingSlideAnim"
        ) { slide ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                // Large Graphic Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(Color.White.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = slide.emoji,
                        fontSize = 96.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = slide.eyebrow,
                    style = MaterialTheme.typography.labelSmall,
                    color = Accent500
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = slide.title,
                    style = MaterialTheme.typography.displayMedium,
                    color = SurfaceWhite
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = slide.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
        }

        // Bottom Indicators + Action Button
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                slides.forEachIndexed { i, _ ->
                    val isSelected = i == currentIndex
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 32.dp else 12.dp,
                        label = "indicatorWidth"
                    )
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(if (isSelected) Accent500 else Color.White.copy(alpha = 0.25f))
                            .clickable { currentIndex = i }
                    )
                }
            }

            AppButton(
                text = if (isLast) "Get Started" else "Continue",
                onClick = {
                    if (isLast) onDone() else currentIndex++
                },
                fullWidth = true
            )
        }
    }
}
