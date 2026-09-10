package com.app.pose.ui.feature.progress.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.app.pose.domain.model.FormTrendItem
import com.app.pose.ui.theme.Accent500
import com.app.pose.ui.theme.LineBorder

@Composable
fun FormTrendChart(
    trendItems: List<FormTrendItem>,
    modifier: Modifier = Modifier
) {
    if (trendItems.isEmpty()) return

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        val w = size.width
        val h = size.height
        val minScore = 60f
        val maxScore = 95f
        val scoreRange = maxScore - minScore

        // Grid lines
        listOf(0.25f, 0.5f, 0.75f).forEach { g ->
            val y = h * g
            drawLine(
                color = LineBorder.copy(alpha = 0.6f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Calculate points
        val count = trendItems.size
        val points = trendItems.mapIndexed { index, item ->
            val x = (index.toFloat() / (count - 1).coerceAtLeast(1)) * w
            val yFraction = ((item.score.toFloat() - minScore) / scoreRange).coerceIn(0f, 1f)
            val y = h - (yFraction * h)
            Offset(x, y)
        }

        // Draw gradient area
        if (points.isNotEmpty()) {
            val areaPath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }

            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Accent500.copy(alpha = 0.25f),
                        Accent500.copy(alpha = 0.02f)
                    ),
                    startY = 0f,
                    endY = h
                )
            )

            // Draw line
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }

            drawPath(
                path = linePath,
                color = Accent500,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Draw points
            points.forEachIndexed { i, pt ->
                val isLast = i == points.size - 1
                if (isLast) {
                    drawCircle(
                        color = Accent500,
                        radius = 6.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = pt
                    )
                } else {
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = Accent500,
                        radius = 4.dp.toPx(),
                        center = pt,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }
}
