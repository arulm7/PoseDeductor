package com.app.pose.ui.feature.camera.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.pose.domain.model.CameraState
import com.app.pose.ui.theme.Bad500
import com.app.pose.ui.theme.Good500
import com.app.pose.ui.theme.Ink900
import com.app.pose.ui.theme.SurfaceWhite
import com.app.pose.ui.theme.Warn400

@Composable
fun FormStatusBadge(
    state: CameraState,
    modifier: Modifier = Modifier
) {
    val (label, icon, bg, textClr) = when (state) {
        CameraState.CORRECT -> Quadruple("GOOD", Icons.Default.Check, Good500, SurfaceWhite)
        CameraState.INCORRECT -> Quadruple("ADJUST", Icons.Default.Warning, Warn400, Ink900)
        CameraState.LOST -> Quadruple("NO BODY", Icons.Default.PersonSearch, Bad500, SurfaceWhite)
        CameraState.READY -> Quadruple("SCANNING", Icons.Default.PersonSearch, Color.White.copy(alpha = 0.2f), SurfaceWhite)
        CameraState.TRACKING -> Quadruple("TRACKING", Icons.Default.AutoAwesome, SurfaceWhite, Ink900)
        CameraState.COMPLETE -> Quadruple("COMPLETE", Icons.Default.Check, Good500, SurfaceWhite)
    }

    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
    ) {
        Text(
            text = "FORM",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.6f)
        )
        Row(
            modifier = Modifier
                .padding(top = 2.dp)
                .clip(CircleShape)
                .background(bg)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textClr,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = textClr
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
