package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.Badge
import com.example.ui.GamificationData
import com.example.ui.NoFapViewModel

@Composable
fun GamificationBadgesSection(
    viewModel: NoFapViewModel,
    theme: GlowTheme,
    gamificationData: GamificationData
) {
    var selectedBadgeForDetail by remember { mutableStateOf<Badge?>(null) }

    // Defined all standard badges in the game
    val masterBadgeList = remember {
        listOf(
            Badge("1", "Clean Slate", "Your first 1 clean day logged.", "🌱", "Bronze"),
            Badge("2", "Seedling Growth", "Logged 3 clean days.", "🌿", "Bronze"),
            Badge("3", "Golden Week", "Maintained a 7-day clean streak.", "🔥", "Silver"),
            Badge("4", "Unyielding Resolve", "Reached a 14-day streak.", "⚔️", "Silver"),
            Badge("5", "Aura Alchemist", "Achieved 30 days of clean energy.", "🪷", "Gold"),
            Badge("6", "Half-Century Mind", "Accomplished 50 total clean days.", "🦅", "Platinum")
        )
    }

    val earnedIds = remember(gamificationData.earnedBadges) {
        gamificationData.earnedBadges.map { it.id }.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        GlowText(
            text = "Aura Progression",
            theme = theme,
            fontSize = 24f,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Level up and gain divine badges as you maintain selfdiscipline.",
            color = theme.textSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Level Widget
        GlassCard(
            theme = theme,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            glowColor = theme.accentGlow
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Circular Level Display
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(theme.primaryGradient[0], theme.primaryGradient[1], theme.primaryGradient[0])
                            )
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(theme.backgroundColors.first()),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "LVL",
                                color = theme.textSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                gamificationData.level.toString(),
                                color = theme.textPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aura Level Progress",
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${gamificationData.totalXp} Total XP Earned",
                        color = theme.textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Linear level xp bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.outlineBright.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(gamificationData.progressPercent.coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(theme.primaryGradient[0], theme.primaryGradient[1])
                                    )
                                )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${gamificationData.progressXp} XP",
                            color = theme.textSecondary,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "100 XP to next level",
                            color = theme.accentGlow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Text(
            "Badge Matrix",
            color = theme.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(masterBadgeList) { badge ->
                val isUnlocked = earnedIds.contains(badge.id)
                BadgeGridCell(
                    badge = badge,
                    isUnlocked = isUnlocked,
                    theme = theme,
                    onClick = {
                        viewModel.triggerHapticFeedback()
                        selectedBadgeForDetail = badge
                    }
                )
            }
        }

        // Selected Badge details screen popup
        if (selectedBadgeForDetail != null) {
            val badge = selectedBadgeForDetail!!
            val isUnlocked = earnedIds.contains(badge.id)

            AlertDialog(
                onDismissRequest = { selectedBadgeForDetail = null },
                containerColor = theme.backgroundColors.first(),
                shape = RoundedCornerShape(28.dp),
                icon = {
                    Text(badge.iconEmoji, fontSize = 54.sp)
                },
                title = {
                    Text(
                        text = badge.title,
                        color = theme.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Tier: ${badge.tier}",
                            color = when (badge.tier) {
                                "Platinum" -> Color(0xFF38BDF8)
                                "Gold" -> Color(0xFFFBBF24)
                                "Silver" -> Color(0xFF94A3B8)
                                else -> Color(0xFFD97706)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )

                        Text(
                            text = badge.description,
                            color = theme.textSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isUnlocked) "🎉 This badge is fully unlocked!" else "🔒 Keep fighting to earn this badge.",
                            color = if (isUnlocked) Color(0xFF10B981) else theme.textSecondary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedBadgeForDetail = null }) {
                        Text("Wonderful", color = theme.accentGlow)
                    }
                }
            )
        }
    }
}

@Composable
fun BadgeGridCell(
    badge: Badge,
    isUnlocked: Boolean,
    theme: GlowTheme,
    onClick: () -> Unit
) {
    val outlineColor = if (isUnlocked) theme.accentGlow else theme.outlineBright.copy(alpha = 0.2f)
    
    GlassCard(
        theme = theme,
        onClick = onClick,
        glowColor = outlineColor,
        contentPadding = PaddingValues(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        if (isUnlocked) theme.outlineBright.copy(alpha = 0.4f)
                        else Color.Gray.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isUnlocked) badge.iconEmoji else "🔒",
                    fontSize = 28.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = badge.title,
                color = if (isUnlocked) theme.textPrimary else theme.textPrimary.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = badge.tier,
                color = if (isUnlocked) {
                    when (badge.tier) {
                        "Platinum" -> Color(0xFF0284C7)
                        "Gold" -> Color(0xFFD97706)
                        "Silver" -> theme.textSecondary
                        else -> Color(0xFFB45309)
                    }
                } else theme.textSecondary.copy(alpha = 0.4f),
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
