package com.app.pose.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.app.pose.ui.theme.Accent500
import com.app.pose.ui.theme.Accent600
import com.app.pose.ui.theme.Bad500
import com.app.pose.ui.theme.ButtonShape
import com.app.pose.ui.theme.Ink900
import com.app.pose.ui.theme.LineBorder
import com.app.pose.ui.theme.SurfaceWhite

enum class AppButtonVariant {
    PRIMARY,
    SECONDARY,
    GHOST,
    DANGER
}

enum class AppButtonSize {
    MEDIUM,
    LARGE
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.PRIMARY,
    size: AppButtonSize = AppButtonSize.LARGE,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    fullWidth: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "btnScale"
    )

    val containerColor = when (variant) {
        AppButtonVariant.PRIMARY -> Accent500
        AppButtonVariant.SECONDARY -> SurfaceWhite
        AppButtonVariant.GHOST -> Color.Transparent
        AppButtonVariant.DANGER -> Bad500.copy(alpha = 0.1f)
    }

    val contentColor = when (variant) {
        AppButtonVariant.PRIMARY -> SurfaceWhite
        AppButtonVariant.SECONDARY -> Ink900
        AppButtonVariant.GHOST -> Accent500
        AppButtonVariant.DANGER -> Bad500
    }

    val border = when (variant) {
        AppButtonVariant.SECONDARY -> BorderStroke(1.dp, LineBorder)
        AppButtonVariant.DANGER -> BorderStroke(1.dp, Bad500.copy(alpha = 0.25f))
        else -> null
    }

    val height = when (size) {
        AppButtonSize.MEDIUM -> 44.dp
        AppButtonSize.LARGE -> 54.dp
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(height)
            .scale(scale),
        enabled = enabled && !loading,
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        border = border,
        contentPadding = PaddingValues(horizontal = 20.dp),
        interactionSource = interactionSource,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (variant == AppButtonVariant.PRIMARY) 2.dp else 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = contentColor,
                    strokeWidth = 2.5.dp
                )
            } else {
                if (leadingIcon != null) {
                    androidx.compose.material3.Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = if (size == AppButtonSize.LARGE) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                    color = contentColor
                )
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    androidx.compose.material3.Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
