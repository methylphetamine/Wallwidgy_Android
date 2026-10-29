package com.notayan.wallwidgy.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Animated "liquid" backdrop used behind every screen. Soft, slowly drifting colour blobs
 * give the translucent glass surfaces something to refract, which is what makes the glass
 * read as glass instead of a flat grey panel.
 */
@Composable
fun LiquidBackground(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme()
) {
    val transition = rememberInfiniteTransition(label = "liquid-backdrop")
    val driftX by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(26000, easing = LinearEasing), RepeatMode.Reverse),
        label = "driftX"
    )
    val driftY by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(34000, easing = LinearEasing), RepeatMode.Reverse),
        label = "driftY"
    )

    val base = if (darkTheme) Color(0xFF07080A) else Color(0xFFF7F6F2)
    val accent = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    val blobs = if (darkTheme) listOf(
        accent.copy(alpha = 0.50f),
        Color(0xFF2E6D8C).copy(alpha = 0.42f),
        Color(0xFF6B3F8C).copy(alpha = 0.38f),
        Color(0xFF1E5B4A).copy(alpha = 0.40f),
        tertiary.copy(alpha = 0.32f)
    ) else listOf(
        accent.copy(alpha = 0.38f),
        Color(0xFF9CC7E8).copy(alpha = 0.55f),
        Color(0xFFF2C9B0).copy(alpha = 0.55f),
        Color(0xFFBFD9C0).copy(alpha = 0.50f),
        tertiary.copy(alpha = 0.30f)
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val longest = maxOf(w, h)

        drawRect(base)

        val centers = listOf(
            Offset(w * (0.10f + 0.28f * driftX), h * (0.10f + 0.22f * driftY)),
            Offset(w * (0.92f - 0.26f * driftY), h * (0.20f + 0.24f * driftX)),
            Offset(w * (0.18f + 0.20f * driftY), h * (0.88f - 0.30f * driftX)),
            Offset(w * (0.80f - 0.24f * driftX), h * (0.78f - 0.20f * driftY)),
            Offset(w * (0.50f + 0.18f * (driftX - driftY)), h * (0.50f + 0.18f * (driftY - driftX)))
        )
        val radii = listOf(0.62f, 0.55f, 0.60f, 0.52f, 0.45f)

        centers.forEachIndexed { index, center ->
            val radius = longest * radii[index]
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(blobs[index], Color.Transparent),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }

        // Gentle vignette so the corners settle down and the glass edges stay readable.
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, base.copy(alpha = if (darkTheme) 0.62f else 0.45f)),
                center = Offset(w / 2f, h / 2f),
                radius = longest * 0.78f
            )
        )
    }
}

data class GlassStyle(
    val container: Brush,
    val border: Brush,
    val highlight: Color
)

@Composable
fun rememberGlassStyle(darkTheme: Boolean = isSystemInDarkTheme()): GlassStyle {
    val primary = MaterialTheme.colorScheme.primary
    return remember(darkTheme, primary) {
        if (darkTheme) {
            GlassStyle(
                container = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.14f),
                        Color.White.copy(alpha = 0.05f),
                        primary.copy(alpha = 0.06f)
                    )
                ),
                border = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.07f),
                        Color.White.copy(alpha = 0.28f)
                    )
                ),
                highlight = Color.White.copy(alpha = 0.16f)
            )
        } else {
            GlassStyle(
                container = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.72f),
                        Color.White.copy(alpha = 0.40f),
                        primary.copy(alpha = 0.05f)
                    )
                ),
                border = Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.95f),
                        Color.White.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.55f)
                    )
                ),
                highlight = Color.White.copy(alpha = 0.50f)
            )
        }
    }
}

/**
 * A translucent, refractive-looking panel: gradient fill, a bright specular top-left sheen,
 * a soft inner shade at the bottom and a gradient hairline border.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    onClick: (() -> Unit)? = null,
    tint: Color = Color.Transparent,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val style = rememberGlassStyle()
    Box(
        modifier = modifier
            .clip(shape)
            .background(style.container)
            .background(tint)
            .drawBehind {
                // Specular highlight sweeping from the top-left corner.
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(style.highlight, Color.Transparent),
                        start = Offset.Zero,
                        end = Offset(size.width * 0.72f, size.height * 0.9f)
                    )
                )
                // Very soft shade along the bottom so the surface has depth.
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.08f)),
                        startY = size.height * 0.45f
                    )
                )
                // Crisp top edge, like light catching the rim of the glass.
                drawLine(
                    color = style.highlight.copy(alpha = (style.highlight.alpha * 0.9f).coerceAtMost(0.6f)),
                    start = Offset(size.width * 0.12f, 0f),
                    end = Offset(size.width * 0.88f, 0f),
                    strokeWidth = 1.2f
                )
            }
            .border(1.dp, style.border, shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(contentPadding),
        content = content
    )
}

/** Convenience pill-shaped glass surface used for chips and small controls. */
@Composable
fun GlassPill(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
    content: @Composable BoxScope.() -> Unit
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        onClick = onClick,
        contentPadding = contentPadding,
        content = content
    )
}
