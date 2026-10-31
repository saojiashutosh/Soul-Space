package com.soulspace.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soulspace.app.ui.theme.*

/**
 * Zen Continuous-Line Meditating Figure
 * Hand-drawn with Compose Canvas Path matching the aesthetic reference image
 */
@Composable
fun ZenMeditatingIcon(
    modifier: Modifier = Modifier,
    color: Color = CharcoalDeep,
    strokeWidthDp: Float = 3f
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Head (Oval)
        drawOval(
            color = color,
            topLeft = Offset(w * 0.38f, h * 0.08f),
            size = Size(w * 0.24f, h * 0.22f),
            style = Stroke(width = strokeWidthDp.dp.toPx(), cap = StrokeCap.Round)
        )

        // Fluid Body & Crossed Legs
        val bodyPath = Path().apply {
            // Left shoulder down to left crossed knee
            moveTo(w * 0.38f, h * 0.38f)
            cubicTo(w * 0.18f, h * 0.50f, w * 0.08f, h * 0.76f, w * 0.22f, h * 0.86f)
            // Left knee across to center base
            cubicTo(w * 0.32f, h * 0.94f, w * 0.45f, h * 0.92f, w * 0.50f, h * 0.86f)
            // Center base to right knee
            cubicTo(w * 0.55f, h * 0.92f, w * 0.68f, h * 0.94f, w * 0.78f, h * 0.86f)
            // Right crossed knee up to right shoulder
            cubicTo(w * 0.92f, h * 0.76f, w * 0.82f, h * 0.50f, w * 0.62f, h * 0.38f)

            // Inner meditating arm curve
            moveTo(w * 0.32f, h * 0.56f)
            cubicTo(w * 0.42f, h * 0.72f, w * 0.58f, h * 0.72f, w * 0.68f, h * 0.56f)
        }

        drawPath(
            path = bodyPath,
            color = color,
            style = Stroke(width = strokeWidthDp.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

/**
 * Aesthetic Category Card matching reference image (Sleep, Soothe, Unwind, Restore)
 */
@Composable
fun SanctuaryCategoryCard(
    title: String,
    subtitle: String,
    backgroundColor: Color,
    accentColor: Color,
    buttonText: String = "Start",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    fontFamily = FontFamily.Serif,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    color = CharcoalDeep
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = CharcoalMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.clickable { onClick() }
                ) {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.labelMedium,
                        color = CharcoalDeep,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            // Organic Graphic Accent (Pebbles & Crescent Moon)
            Canvas(modifier = Modifier.size(64.dp)) {
                val cw = size.width
                val ch = size.height

                // Soft background wave
                val wavePath = Path().apply {
                    moveTo(0f, ch * 0.6f)
                    cubicTo(cw * 0.3f, ch * 0.4f, cw * 0.7f, ch * 0.8f, cw, ch * 0.5f)
                    lineTo(cw, ch)
                    lineTo(0f, ch)
                    close()
                }
                drawPath(wavePath, color = accentColor.copy(alpha = 0.4f))

                // Balanced Zen Pebble
                drawOval(
                    color = accentColor,
                    topLeft = Offset(cw * 0.35f, ch * 0.2f),
                    size = Size(cw * 0.35f, ch * 0.28f)
                )
                drawOval(
                    color = CharcoalDeep.copy(alpha = 0.75f),
                    topLeft = Offset(cw * 0.25f, ch * 0.45f),
                    size = Size(cw * 0.55f, ch * 0.24f)
                )
            }
        }
    }
}

/**
 * Animated Breathing Pacer Canvas
 */
@Composable
fun BreathingPacerCanvas(
    modifier: Modifier = Modifier,
    isInhaling: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pacer")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pacerScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // Outer aura
        Box(
            modifier = Modifier
                .size(240.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(SageLight.copy(alpha = 0.6f), Color.Transparent)
                    )
                )
        )
        // Mid ring
        Box(
            modifier = Modifier
                .size(170.dp)
                .scale(scale * 0.85f)
                .clip(CircleShape)
                .background(SagePrimary.copy(alpha = 0.25f))
        )
        // Core stone
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(SagePrimary)
        ) {
            ZenMeditatingIcon(
                modifier = Modifier.size(50.dp),
                color = Color.White
            )
        }
    }
}

/**
 * Minimalist Screen-Time Bar Chart (Compose Canvas)
 */
@Composable
fun ScreenTimeBarChart(
    hoursPerDay: List<Float>, // 7 values for Mon..Sun
    modifier: Modifier = Modifier,
    barColor: Color = SagePrimary
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val barCount = hoursPerDay.size
        val barWidth = (w / barCount) * 0.45f
        val maxHour = (hoursPerDay.maxOrNull() ?: 6f).coerceAtLeast(4f)

        hoursPerDay.forEachIndexed { index, value ->
            val barHeight = (value / maxHour) * (h * 0.85f)
            val x = (index * (w / barCount)) + ((w / barCount) - barWidth) / 2
            val y = h - barHeight

            // Draw Bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
