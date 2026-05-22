package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Centralized Theme Color Palettes
data class GlowTheme(
    val id: String,
    val name: String,
    val primaryGradient: List<Color>,
    val backgroundColors: List<Color>,
    val cardBackground: Color,
    val outlineBright: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accentGlow: Color,
    val isDark: Boolean
)

object ThemeRegistry {
    val CosmicSlateDark = GlowTheme(
        id = "cosmic_slate_dark",
        name = "Frosted Glass",
        primaryGradient = listOf(Color(0xFFA855F7), Color(0xFF60A5FA)), // Purple-500, Blue-400
        backgroundColors = listOf(Color(0xFF090A0F), Color(0xFF07080B)), // Ultra Dark Space Slate
        cardBackground = Color(0x13FFFFFF), // Highly translucent glass overlay (white/8% alpha)
        outlineBright = Color(0x26FFFFFF), // High-contrast translucent shiny outline (white/15%)
        textPrimary = Color(0xFFF1F5F9), // Slate 100
        textSecondary = Color(0xFF94A3B8), // Slate 400
        accentGlow = Color(0xFFA855F7), // Purple-500
        isDark = true
    )

    val CosmicSlateLight = GlowTheme(
        id = "cosmic_slate_light",
        name = "Frosted Light Glass",
        primaryGradient = listOf(Color(0xFF8B5CF6), Color(0xFF3B82F6)), // Violet, Blue
        backgroundColors = listOf(Color(0xFFF8FAFC), Color(0xFFEDF2F7)), // Soft Slate light
        cardBackground = Color(0xB5FFFFFF), // Soft translucent white backdrop
        outlineBright = Color(0x2EA855F7),
        textPrimary = Color(0xFF090A0F),
        textSecondary = Color(0xFF475569),
        accentGlow = Color(0xFF8B5CF6),
        isDark = false
    )

    val EmeraldGlowDark = GlowTheme(
        id = "emerald_glow_dark",
        name = "Emerald Forest",
        primaryGradient = listOf(Color(0xFF10B981), Color(0xFF06B6D4)), // Mint, Cyan
        backgroundColors = listOf(Color(0xFF051D18), Color(0xFF020E0B)), // Deep Emerald Space Dark
        cardBackground = Color(0x13FFFFFF), // Highly translucent glass overlay
        outlineBright = Color(0x26FFFFFF),
        textPrimary = Color(0xFFECFDF5), // Mint light primary text
        textSecondary = Color(0xFFA7F3D0), // Mint secondary text
        accentGlow = Color(0xFF10B981),
        isDark = true
    )

    val EmeraldGlowLight = GlowTheme(
        id = "emerald_glow_light",
        name = "Emerald Forest",
        primaryGradient = listOf(Color(0xFF047857), Color(0xFF059669)),
        backgroundColors = listOf(Color(0xFFECFDF5), Color(0xFFD1FAE5)),
        cardBackground = Color(0xC0FFFFFF),
        outlineBright = Color(0x2B047857),
        textPrimary = Color(0xFF064E3B),
        textSecondary = Color(0xFF065F46),
        accentGlow = Color(0xFF10B981),
        isDark = false
    )

    val CyberGlowDark = GlowTheme(
        id = "cyber_glow_dark",
        name = "Cyberpunk Neon",
        primaryGradient = listOf(Color(0xFFFF007F), Color(0xFF00F0FF)), // Rose, Cyan
        backgroundColors = listOf(Color(0xFF1A010F), Color(0xFF04000C)), // Cyber Dark Slate
        cardBackground = Color(0x13FFFFFF), // Highly translucent glass overlay
        outlineBright = Color(0x26FFFFFF),
        textPrimary = Color(0xFFFFF2FA),
        textSecondary = Color(0xFFFFA5D2),
        accentGlow = Color(0xFF00F0FF),
        isDark = true
    )

    val CyberGlowLight = GlowTheme(
        id = "cyber_glow_light",
        name = "Cyberpunk Neon",
        primaryGradient = listOf(Color(0xFFE0006C), Color(0xFF00ADB5)),
        backgroundColors = listOf(Color(0xFFFFF0F5), Color(0xFFE8F4F8)),
        cardBackground = Color(0xB3FFFFFF),
        outlineBright = Color(0x3D00ADB5),
        textPrimary = Color(0xFF2D001C),
        textSecondary = Color(0xFF8E0C4F),
        accentGlow = Color(0xFFFF007F),
        isDark = false
    )

    val AuroraSoftDark = GlowTheme(
        id = "aurora_soft_dark",
        name = "Aurora Polaris",
        primaryGradient = listOf(Color(0xFF38BDF8), Color(0xFF34D399)), // Frost Sky, Mint Aurora
        backgroundColors = listOf(Color(0xFF031E2F), Color(0xFF020B13)), // Arctic Deep Blue
        cardBackground = Color(0x13FFFFFF), // Highly translucent glass overlay
        outlineBright = Color(0x26FFFFFF),
        textPrimary = Color(0xFFF0F9FF),
        textSecondary = Color(0xFF7DD3FC),
        accentGlow = Color(0xFF38BDF8),
        isDark = true
    )

    val AuroraSoftLight = GlowTheme(
        id = "aurora_soft_light",
        name = "Aurora Polaris",
        primaryGradient = listOf(Color(0xFF0284C7), Color(0xFF059669)),
        backgroundColors = listOf(Color(0xFFF0F9FF), Color(0xFFE0F2FE)),
        cardBackground = Color(0xC8FFFFFF),
        outlineBright = Color(0x220284C7),
        textPrimary = Color(0xFF0369A1),
        textSecondary = Color(0xFF0F766E),
        accentGlow = Color(0xFF0284C7),
        isDark = false
    )

    fun getTheme(themeId: String, useDark: Boolean): GlowTheme {
        return when (themeId) {
            "emerald_glow" -> if (useDark) EmeraldGlowDark else EmeraldGlowLight
            "cyber_glow" -> if (useDark) CyberGlowDark else CyberGlowLight
            "aurora_soft" -> if (useDark) AuroraSoftDark else AuroraSoftLight
            else -> if (useDark) CosmicSlateDark else CosmicSlateLight
        }
    }
}

// 1. Reusable Glassmorphic Card
@Composable
fun GlassCard(
    theme: GlowTheme,
    modifier: Modifier = Modifier,
    blurRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    glowColor: Color? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else Modifier

    val actualGlow = glowColor ?: theme.outlineBright

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .then(clickableModifier)
            .background(theme.cardBackground)
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        actualGlow.copy(alpha = 0.5f),
                        theme.outlineBright.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .drawBehind {
                val r = size.maxDimension / 2
                if (r > 0f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(actualGlow.copy(alpha = 0.08f), Color.Transparent),
                            center = center,
                            radius = r
                        ),
                        radius = r
                    )
                }
            }
    ) {
        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            content()
        }
    }
}

// 2. Beautiful Neumorphic Clickable Button
@Composable
fun NeumorphicButton(
    theme: GlowTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    glowOnSelected: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1.0f,
        animationSpec = tween(100),
        label = "PressState"
    )

    val backgroundBrush = if (theme.isDark) {
        // Dark theme: deep glossy metallic gradients
        Brush.verticalGradient(
            colors = if (isPressed) {
                listOf(Color(0x3DFFFFFF), Color(0x1F000000))
            } else {
                listOf(Color(0x1FFFFFFF), Color(0x0A000000))
            }
        )
    } else {
        // Light theme: elegant double-shadow light gradients
        Brush.verticalGradient(
            colors = if (isPressed) {
                listOf(Color(0xFFE2E8F0), Color(0xFFF8FAFC))
            } else {
                listOf(Color(0xFFFFFFFF), Color(0xFFCBD5E1))
            }
        )
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = {
                    isPressed = true
                    onClick()
                    isPressed = false
                }
            )
            .background(backgroundBrush)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = if (isPressed) {
                        listOf(Color.Transparent, theme.primaryGradient.first().copy(alpha = 0.4f))
                    } else {
                        listOf(theme.outlineBright.copy(alpha = 0.6f), Color.Transparent)
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

// 3. Glowing Text Label Composable
@Composable
fun GlowText(
    text: String,
    theme: GlowTheme,
    modifier: Modifier = Modifier,
    fontSize: Float = 24f,
    fontWeight: FontWeight = FontWeight.Bold,
    glowColor: Color? = null
) {
    val activeGlow = glowColor ?: theme.accentGlow
    Box(
        modifier = modifier.drawBehind {
            drawCircle(
                color = activeGlow.copy(alpha = 0.15f),
                radius = size.maxDimension * 0.8f,
                center = center
            )
        }
    ) {
        Text(
            text = text,
            color = theme.textPrimary,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            letterSpacing = 0.5.sp
        )
    }
}
