package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GeminiAqua
import com.example.ui.theme.GeminiViolet
import com.example.ui.theme.StatusCyan
import com.example.ui.theme.TacticalBorder
import com.example.ui.theme.TacticalSurface
import com.example.ui.theme.TerminalTextSecondary

/**
 * Smooth iPhone/Gemini squircle glassmorphic container with animated color transitions
 * and a soft top specular highlight.
 */
@Composable
fun ChamferedTacticalBox(
    modifier: Modifier = Modifier,
    containerColor: Color = TacticalSurface,
    borderColor: Color = TacticalBorder,
    accentTickColor: Color = StatusCyan,
    strokeWidth: Dp = 1.dp,
    cutTopLeft: Dp = 0.dp,
    cutTopRight: Dp = 12.dp,
    cutBottomRight: Dp = 0.dp,
    cutBottomLeft: Dp = 12.dp,
    showCornerReticles: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    // Map legacy cut sizes to smooth iOS/Gemini continuous corner radii
    val baseRadius = ((cutTopRight + cutBottomLeft) / 2f).coerceIn(12.dp, 26.dp)
    val cornerRadius = if (baseRadius <= 10.dp) 14.dp else if (baseRadius <= 14.dp) 20.dp else 24.dp
    val shape = RoundedCornerShape(cornerRadius)

    val animatedContainer by animateColorAsState(
        targetValue = containerColor,
        animationSpec = tween(durationMillis = 240),
        label = "smooth_container_color"
    )
    val animatedBorder by animateColorAsState(
        targetValue = borderColor,
        animationSpec = tween(durationMillis = 240),
        label = "smooth_border_color"
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(animatedContainer)
            .drawWithContent {
                // Subtle Gemini/iOS top-down ambient sheen
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.045f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.45f
                    )
                )
                drawContent()
                if (showCornerReticles) {
                    // Subtle luminous top specular accent line (Gemini aura highlight)
                    drawLine(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                accentTickColor.copy(alpha = 0.55f),
                                Color.Transparent
                            )
                        ),
                        start = Offset(size.width * 0.15f, 0f),
                        end = Offset(size.width * 0.85f, 0f),
                        strokeWidth = (strokeWidth * 1.6f).toPx()
                    )
                }
            }
            .border(
                width = strokeWidth,
                color = animatedBorder.copy(alpha = 0.85f),
                shape = shape
            ),
        content = content
    )
}

/**
 * Smooth Gemini/iOS fluid pill progress bar with spring physics and iridescent gradient fill.
 */
@Composable
fun SegmentedHudBar(
    totalSegments: Int,
    activeIndex: Int,
    activeColor: Color = StatusCyan,
    completedColor: Color = GeminiViolet,
    inactiveColor: Color = TacticalBorder,
    modifier: Modifier = Modifier
) {
    val safeSegments = totalSegments.coerceAtLeast(1)
    val targetFraction = ((activeIndex + 1).toFloat() / safeSegments.toFloat()).coerceIn(0.06f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "smooth_progress_fraction"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(CircleShape)
            .background(inactiveColor.copy(alpha = 0.55f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedFraction)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            activeColor,
                            completedColor,
                            GeminiAqua
                        )
                    )
                )
        )
    }
}

/**
 * Smooth glowing status orb (iPhone Dynamic Island style).
 */
@Composable
fun PulsingStatusLed(
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hud_led_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "led_alpha"
    )

    Canvas(modifier = modifier.size(10.dp)) {
        drawCircle(
            color = color.copy(alpha = alpha * 0.32f),
            radius = size.minDimension / 1.8f
        )
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = size.minDimension / 3.0f
        )
    }
}

/**
 * Smooth iOS/Gemini section header with subtle pill indicator.
 */
@Composable
fun BrutalistSectionDivider(
    leftLabel: String,
    rightLabel: String,
    accentColor: Color = StatusCyan,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 8.dp, height = 8.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(accentColor, GeminiViolet)
                        )
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = leftLabel,
                style = MaterialTheme.typography.labelLarge,
                color = accentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = rightLabel,
            style = MaterialTheme.typography.labelSmall,
            color = TerminalTextSecondary
        )
    }
}
