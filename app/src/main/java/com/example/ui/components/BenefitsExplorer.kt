package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BenefitItem(
    val title: String,
    val timeline: String,
    val category: String,
    val description: String,
    val details: String,
    val iconEmoji: String,
    val progressMarkerDays: Int
)

@Composable
fun BenefitsExplorerSection(
    theme: GlowTheme,
    currentStreakDays: Int
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val benefits = remember {
        listOf(
            BenefitItem(
                title = "Androgen Receptor Recovery",
                timeline = "Day 1 - 3",
                category = "Physiological Reset",
                description = "Your endocrine pathways recover from continuous hyper-stimulation.",
                details = "Initially, you start overcoming the constant dopamine crash. Cortisol levels begin returning to baselines, resulting in less baseline irritation and early morning energy increases. Androgen receptors in the brain begin reclaiming sensitivity.",
                iconEmoji = "🌱",
                progressMarkerDays = 2
            ),
            BenefitItem(
                title = "The Day-7 Testosterone Surge",
                timeline = "Day 4 - 7",
                category = "Hormonal Peak",
                description = "An endocrine study peak of up to 145% of free circulating testosterone occurs.",
                details = "Around the 7th day, studies observe a powerful hormonal spike. Mental vigor reaches an initial peak. Voice resonance improves, workout endurance increases, and confidence spikes as natural androgen binding functions recover fully.",
                iconEmoji = "⚡",
                progressMarkerDays = 7
            ),
            BenefitItem(
                title = "Dopamine Threshold Up-regulation",
                timeline = "Day 8 - 14",
                category = "Neurological Reset",
                description = "Brain fog dissipates. Normal everyday events start producing genuine joy.",
                details = "Chronic over-stimulation reduces baseline dopamine sensitivity. At this point, receptors begin up-regulating. Conversation flows easier, focus span rises significantly, skin brightness improves, and passive social anxiety begins fading away.",
                iconEmoji = "🧠",
                progressMarkerDays = 14
            ),
            BenefitItem(
                title = "Amygdala Healing & Willpower",
                timeline = "Day 15 - 30",
                category = "Emotional Resilience",
                description = "Enhanced prefrontal cortex connection provides absolute mastery of cravings.",
                details = "You regain logical dominance over primitive brain receptors. Urges no longer feel like compulsive emergencies. Heightened visual charisma, profound sleep depth, complete recovery of confidence in social lookups, and clear morning focus.",
                iconEmoji = "🪷",
                progressMarkerDays = 30
            ),
            BenefitItem(
                title = "Neural Rewiring & Spiritual Climax",
                timeline = "Day 30 - 90+",
                category = "Full Rebirth",
                description = "Primal energy is fully transmuted into creative and professional power.",
                details = "Complete neuroplastic revision. Deep inner-peace, continuous magnetic presence, intense manifestation capacity, high physical muscle growth response, and total freedom from ancient compulsive dopamine loop models.",
                iconEmoji = "🦅",
                progressMarkerDays = 90
            )
        )
    }

    val filteredBenefits = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            benefits
        } else {
            benefits.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        GlowText(
            text = "Benefits Timeline",
            theme = theme,
            fontSize = 24f,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Explore the cumulative benefits waiting for you as you reclaim your focus.",
            color = theme.textSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Custom Glass-Outlined Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Search benefit records...", color = theme.textSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = theme.accentGlow) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.accentGlow,
                unfocusedBorderColor = theme.outlineBright,
                focusedContainerColor = theme.cardBackground,
                unfocusedContainerColor = theme.cardBackground.copy(alpha = 0.5f),
                focusedTextColor = theme.textPrimary,
                unfocusedTextColor = theme.textPrimary
            ),
            singleLine = true
        )

        if (filteredBenefits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No milestones found matching your query.",
                    color = theme.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredBenefits) { benefit ->
                    BenefitCard(benefit = benefit, theme = theme, currentStreakDays = currentStreakDays)
                }
            }
        }
    }
}

@Composable
fun BenefitCard(
    benefit: BenefitItem,
    theme: GlowTheme,
    currentStreakDays: Int
) {
    var expanded by remember { mutableStateOf(false) }
    val isUnlocked = currentStreakDays >= benefit.progressMarkerDays
    
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

    val activeGlow = if (isUnlocked) theme.accentGlow else theme.textSecondary.copy(alpha = 0.4f)

    GlassCard(
        theme = theme,
        onClick = { expanded = !expanded },
        glowColor = activeGlow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Circle Emoji with Glowing Background
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = benefit.iconEmoji,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = benefit.title,
                        color = theme.textPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    // Streak badge
                    Text(
                        text = benefit.timeline,
                        color = if (isUnlocked) theme.accentGlow else theme.textSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = benefit.category,
                        color = theme.accentGlow.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isUnlocked) "✦ Active" else "🔒 Locked (${benefit.progressMarkerDays}d required)",
                        color = if (isUnlocked) Color(0xFF10B981) else theme.textSecondary.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = theme.textPrimary,
                modifier = Modifier.rotate(rotationState)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = benefit.description,
            color = theme.textSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "Detailed Impact:",
                    color = theme.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = benefit.details,
                    color = theme.textSecondary.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )
            }
        }
    }
}
