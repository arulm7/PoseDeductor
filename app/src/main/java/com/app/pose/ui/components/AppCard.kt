package com.app.pose.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.pose.ui.theme.Accent500
import com.app.pose.ui.theme.CardShape
import com.app.pose.ui.theme.Ink900
import com.app.pose.ui.theme.LineBorder
import com.app.pose.ui.theme.SurfaceWhite

enum class CardTone {
    DEFAULT,
    DARK,
    ACCENT,
    GOOD,
    WARN,
    BAD
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    tone: CardTone = CardTone.DEFAULT,
    shape: Shape = CardShape,
    elevation: Dp = 0.dp,
    padding: Dp = 16.dp,
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val containerColor = when (tone) {
        CardTone.DEFAULT -> SurfaceWhite
        CardTone.DARK -> Ink900
        CardTone.ACCENT -> Color(0xFFFFF1EB)
        CardTone.GOOD -> Color(0xFFE6F7EF)
        CardTone.WARN -> Color(0xFFFFF6E4)
        CardTone.BAD -> Color(0xFFFDECEA)
    }

    val defaultBorder = when (tone) {
        CardTone.DEFAULT -> BorderStroke(1.dp, LineBorder.copy(alpha = 0.8f))
        CardTone.DARK -> BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        CardTone.ACCENT -> BorderStroke(1.dp, Color(0xFFFFDFD2))
        CardTone.GOOD -> BorderStroke(1.dp, Color(0xFF2ECF92).copy(alpha = 0.25f))
        CardTone.WARN -> BorderStroke(1.dp, Color(0xFFFFC24D).copy(alpha = 0.3f))
        CardTone.BAD -> BorderStroke(1.dp, Color(0xFFFF6B60).copy(alpha = 0.3f))
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = border ?: defaultBorder,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Box(
                modifier = Modifier.padding(padding),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            border = border ?: defaultBorder,
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Box(
                modifier = Modifier.padding(padding),
                content = content
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (action != null && onAction != null) {
            TextButton(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = action,
                    style = MaterialTheme.typography.titleSmall,
                    color = Accent500
                )
            }
        }
    }
}
