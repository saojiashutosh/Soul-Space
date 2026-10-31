package com.soulspace.app.ui.components

import androidx.compose.foundation.border
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

enum class CardMotif { Moon, Stones, Sun, Disc }

/**
 * Illustrated category card: flat sky, layered hills and a small motif,
 * matching the Sleep / Soothe / Unwind / Restore reference.
 */
@Composable
fun SanctuaryCategoryCard(
    title: String,
    subtitle: String,
    backgroundColor: Color,
    accentColor: Color,
    motif: CardMotif = CardMotif.Disc,
    hillColor: Color = IllusRose,
    buttonText: String = "Start",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .clickable { onClick() }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // Motif sits on the right, behind the hills
            when (motif) {
                CardMotif.Moon -> {
                    drawCircle(Color(0xFFF1E3E0), radius = h * 0.26f, center = Offset(w * 0.86f, h * 0.36f))
                    drawCircle(backgroundColor, radius = h * 0.22f, center = Offset(w * 0.83f, h * 0.30f))
                }
                CardMotif.Stones -> {
                    val cx = w * 0.88f
                    drawCircle(IllusPebbleDark, radius = h * 0.07f, center = Offset(cx, h * 0.14f))
                    drawArc(accentColor, 0f, 180f, true, Offset(cx - h * 0.2f, h * 0.2f), Size(h * 0.4f, h * 0.34f))
                    drawArc(IllusPebbleDark, 180f, 180f, true, Offset(cx - h * 0.26f, h * 0.5f), Size(h * 0.52f, h * 0.9f))
                }
                CardMotif.Sun -> {
                    drawCircle(Color(0xFFE6B79A), radius = h * 0.24f, center = Offset(w * 0.86f, h * 0.38f))
                }
                CardMotif.Disc -> {
                    drawCircle(IllusMint, radius = h * 0.32f, center = Offset(w * 0.86f, h * 0.45f))
                }
            }

            // Back hill
            drawPath(
                Path().apply {
                    moveTo(0f, h * 0.62f)
                    cubicTo(w * 0.25f, h * 0.42f, w * 0.5f, h * 0.78f, w, h * 0.5f)
                    lineTo(w, h); lineTo(0f, h); close()
                },
                color = IllusGrey.copy(alpha = if (motif == CardMotif.Moon) 0f else 0.85f)
            )
            // Front hill
            drawPath(
                Path().apply {
                    moveTo(0f, h * 0.78f)
                    cubicTo(w * 0.3f, h * 0.58f, w * 0.6f, h * 0.95f, w, h * 0.68f)
                    lineTo(w, h); lineTo(0f, h); close()
                },
                color = hillColor
            )
        }

        Column(modifier = Modifier.padding(start = 16.dp, top = 12.dp)) {
            Text(
                text = title,
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                color = CharcoalDeep
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = CharcoalDeep.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.9f)) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelSmall,
                    color = CharcoalDeep,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Terracotta call-to-action pill, as in "Start Session".
 */
@Composable
fun ClayButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = TerracottaClay, contentColor = Color.White),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
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


/**
 * Explicit colours for every text input so text, label, cursor and border
 * stay readable on the cream / beige surfaces.
 */
@Composable
fun soulTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = CharcoalDeep,
    unfocusedTextColor = CharcoalDeep,
    disabledTextColor = CharcoalMuted,
    focusedContainerColor = SanctuaryCardBg,
    unfocusedContainerColor = SanctuaryCardBg,
    focusedBorderColor = TerracottaClay,
    unfocusedBorderColor = CharcoalDivider,
    focusedLabelColor = TerracottaClay,
    unfocusedLabelColor = CharcoalMuted,
    focusedPlaceholderColor = CharcoalMuted,
    unfocusedPlaceholderColor = CharcoalMuted,
    focusedLeadingIconColor = CharcoalMuted,
    unfocusedLeadingIconColor = CharcoalMuted,
    cursorColor = TerracottaClay
)

/** Switch colours with a visible, light off-state on cream surfaces. */
@Composable
fun soulSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = SagePrimary,
    checkedBorderColor = SagePrimary,
    uncheckedThumbColor = Color.White,
    uncheckedTrackColor = CharcoalDivider,
    uncheckedBorderColor = CharcoalDivider
)

/**
 * Pill filter chip: single-line, centered label and a soft border.
 * Give it Modifier.weight(1f) to share a row evenly.
 */
@Composable
fun SoulFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: androidx.compose.ui.unit.Dp = 14.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .heightIn(min = 40.dp)
            .clip(CircleShape)
            .background(if (selected) SagePrimary else SanctuarySurface)
            .border(1.dp, if (selected) SagePrimary else CharcoalDivider, CircleShape)
            .clickable { onClick() }
            .padding(horizontal = horizontalPadding, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else CharcoalDeep,
            maxLines = 1,
            softWrap = false
        )
    }
}

/** Equal-width segmented tabs; labels never wrap. */
@Composable
fun SoulSegmentedTabs(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(SanctuarySurface)
            .padding(4.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (selected) SagePrimary else Color.Transparent)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) Color.White else CharcoalMuted,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

/** Centered placeholder for lists with nothing in them yet. */
@Composable
fun EmptyHint(title: String, body: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontFamily = FontFamily.Serif,
            fontSize = 20.sp,
            color = CharcoalDeep
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = CharcoalMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
