package com.app.pose.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.app.pose.ui.theme.Accent500
import com.app.pose.ui.theme.ChipShape
import com.app.pose.ui.theme.Good400
import com.app.pose.ui.theme.Good600
import com.app.pose.ui.theme.Ink600
import com.app.pose.ui.theme.Ink900
import com.app.pose.ui.theme.LineBorder
import com.app.pose.ui.theme.MutedText
import com.app.pose.ui.theme.PillShape
import com.app.pose.ui.theme.SurfaceWhite
import com.app.pose.ui.theme.Warn500

enum class ChipTone {
    DEFAULT,
    ACCENT,
    GOOD,
    WARN,
    BAD,
    DARK
}

@Composable
fun AppChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: ChipTone = ChipTone.DEFAULT,
    icon: ImageVector? = null,
    iconEmoji: String? = null,
    iconNew: String? = null
) {
    val (bgColor, textColor, borderColor) = when (tone) {
        ChipTone.DEFAULT -> Triple(CanvasBg(), Ink600, LineBorder.copy(alpha = 0.6f))
        ChipTone.ACCENT -> Triple(Color(0xFFFFF1EB), Accent500, Color(0xFFFFDFD2))
        ChipTone.GOOD -> Triple(Color(0xFFE6F7EF), Good600, Color(0xFF2ECF92).copy(alpha = 0.3f))
        ChipTone.WARN -> Triple(Color(0xFFFFF6E4), Warn500, Color(0xFFFFC24D).copy(alpha = 0.3f))
        ChipTone.BAD -> Triple(Color(0xFFFDECEA), Color(0xFFE23B32), Color(0xFFFF6B60).copy(alpha = 0.3f))
        ChipTone.DARK -> Triple(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.15f))
    }

    Row(
        modifier = modifier
            .clip(ChipShape)
            .background(bgColor)
            .border(BorderStroke(1.dp, borderColor), ChipShape)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = textColor
            )
            Spacer(modifier = Modifier.width(4.dp))
        } else if (iconEmoji != null) {
            Text(
                text = iconEmoji,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}

@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) Ink900 else SurfaceWhite,
        label = "chipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) SurfaceWhite else Ink600,
        label = "chipText"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) Ink900 else LineBorder,
        label = "chipBorder"
    )

    Box(
        modifier = modifier
            .height(38.dp)
            .clip(PillShape)
            .background(bgColor)
            .border(BorderStroke(1.dp, borderColor), PillShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = textColor
        )
    }
}

@Composable
private fun CanvasBg(): Color = Color(0xFFECEAE5)
