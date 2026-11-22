package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.VyroBorderSubtle
import com.example.ui.theme.VyroDeepBlack
import com.example.ui.theme.VyroElectricPurple
import com.example.ui.theme.VyroElectricPurpleBright
import com.example.ui.theme.VyroGraphite
import com.example.ui.theme.VyroGraphiteElevated
import com.example.ui.theme.VyroNeonBlue
import com.example.ui.theme.VyroNeonCyan
import com.example.ui.theme.VyroTextPrimary
import com.example.ui.theme.VyroTextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun VyroPrismLogo(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    animateGlow: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vyro_prism_glow")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val glowAlpha = if (animateGlow) pulse else 0.85f

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f

        // Subtle ambient radial halo
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    VyroElectricPurple.copy(alpha = 0.32f * glowAlpha),
                    VyroNeonBlue.copy(alpha = 0.14f * glowAlpha),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = w * 0.56f
            ),
            radius = w * 0.56f,
            center = Offset(cx, cy)
        )

        // Outer V-Prism silhouette
        val outerV = Path().apply {
            moveTo(w * 0.14f, h * 0.22f)
            lineTo(w * 0.50f, h * 0.88f)
            lineTo(w * 0.86f, h * 0.22f)
            lineTo(w * 0.68f, h * 0.22f)
            lineTo(w * 0.50f, h * 0.58f)
            lineTo(w * 0.32f, h * 0.22f)
            close()
        }

        drawPath(
            path = outerV,
            brush = Brush.linearGradient(
                colors = listOf(
                    VyroElectricPurple.copy(alpha = 0.22f),
                    VyroNeonBlue.copy(alpha = 0.18f)
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )

        drawPath(
            path = outerV,
            brush = Brush.linearGradient(
                colors = listOf(VyroElectricPurpleBright, VyroNeonBlue),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            ),
            style = Stroke(width = w * 0.055f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Inner optical aperture ring
        val ringCenter = Offset(cx, h * 0.34f)
        val ringRadius = w * 0.14f
        drawCircle(
            brush = Brush.linearGradient(listOf(VyroNeonCyan, VyroElectricPurple)),
            radius = ringRadius,
            center = ringCenter,
            style = Stroke(width = w * 0.036f)
        )

        // Aperture facets
        for (i in 0 until 6) {
            val angle = Math.toRadians((i * 60).toDouble())
            val start = Offset(
                x = ringCenter.x + (ringRadius * 0.45f * cos(angle)).toFloat(),
                y = ringCenter.y + (ringRadius * 0.45f * sin(angle)).toFloat()
            )
            val end = Offset(
                x = ringCenter.x + (ringRadius * 0.92f * cos(angle + 0.45)).toFloat(),
                y = ringCenter.y + (ringRadius * 0.92f * sin(angle + 0.45)).toFloat()
            )
            drawLine(
                color = VyroNeonBlue.copy(alpha = 0.9f),
                start = start,
                end = end,
                strokeWidth = w * 0.02f,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun VyroTopBar(
    title: String,
    subtitle: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onNavigateBack != null) {
            Surface(
                shape = CircleShape,
                color = VyroGraphiteElevated,
                border = BorderStroke(1.dp, VyroBorderSubtle),
                modifier = Modifier
                    .size(44.dp)
                    .minimumInteractiveComponentSize()
                    .testTag("top_bar_back_button")
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.cd_navigate_back),
                        tint = VyroTextPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = VyroTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = VyroTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}

@Composable
fun VyroGlassCard(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val borderBrush = if (highlighted) {
        Brush.linearGradient(listOf(VyroElectricPurple, VyroNeonBlue))
    } else {
        Brush.linearGradient(listOf(VyroBorderSubtle, VyroBorderSubtle.copy(alpha = 0.5f)))
    }
    val containerColor = if (highlighted) VyroGraphiteElevated else VyroGraphite

    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
            .border(
                width = if (highlighted) 1.5.dp else 1.dp,
                brush = borderBrush,
                shape = MaterialTheme.shapes.large
            ),
        color = containerColor,
        shape = MaterialTheme.shapes.large,
        tonalElevation = if (highlighted) 6.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun VyroPillBadge(
    text: String,
    accentColor: Color = VyroNeonBlue,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = accentColor.copy(alpha = 0.14f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun VyroGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String = "vyro_primary_button"
) {
    val gradient = Brush.horizontalGradient(
        colors = if (enabled) {
            listOf(VyroElectricPurple, VyroElectricPurpleBright, VyroNeonBlue)
        } else {
            listOf(VyroGraphiteElevated, VyroGraphiteElevated)
        }
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContentColor = VyroTextSecondary
        ),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .height(52.dp)
            .minimumInteractiveComponentSize()
            .background(brush = gradient, shape = RoundedCornerShape(14.dp))
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun VyroTimecodeChip(
    currentMs: Long,
    totalMs: Long,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = VyroDeepBlack.copy(alpha = 0.85f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, VyroBorderSubtle)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = com.example.core.util.VyroTimeFormatter.formatTimecode(currentMs),
                fontFamily = JetBrainsMonoFontFamily,
                style = MaterialTheme.typography.labelSmall,
                color = VyroNeonBlue
            )
            Text(
                text = " / ",
                fontFamily = JetBrainsMonoFontFamily,
                style = MaterialTheme.typography.labelSmall,
                color = VyroTextSecondary
            )
            Text(
                text = com.example.core.util.VyroTimeFormatter.formatTimecode(totalMs),
                fontFamily = JetBrainsMonoFontFamily,
                style = MaterialTheme.typography.labelSmall,
                color = VyroTextPrimary
            )
        }
    }
}

@Composable
fun VyroAspectFramePreview(
    ratio: Float,
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .height(86.dp)
                .aspectRatio(ratio.coerceIn(0.45f, 2.4f), matchHeightConstraintsFirst = true)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            VyroElectricPurple.copy(alpha = 0.25f),
                            VyroNeonBlue.copy(alpha = 0.18f)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(VyroElectricPurple, VyroNeonBlue)),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontFamily = JetBrainsMonoFontFamily,
                style = MaterialTheme.typography.labelSmall,
                color = VyroTextPrimary
            )
        }
    }
}
