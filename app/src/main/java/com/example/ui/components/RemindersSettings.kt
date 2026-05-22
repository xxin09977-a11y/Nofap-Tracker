package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserSettings
import com.example.ui.NoFapViewModel

@Composable
fun RemindersSettingsSection(
    viewModel: NoFapViewModel,
    theme: GlowTheme,
    settings: UserSettings
) {
    var targetDaysState by remember(settings) { mutableStateOf(settings.targetDays.toFloat()) }
    var reminderHourState by remember(settings) { mutableStateOf(settings.reminderHour) }
    var reminderMinuteState by remember(settings) { mutableStateOf(settings.reminderMinute) }
    var motivationGoalText by remember(settings) { mutableStateOf(settings.dailyMotivationGoal) }
    var hapticToggle by remember(settings) { mutableStateOf(settings.hapticFeedbackEnabled) }
    var darkToggle by remember(settings) { mutableStateOf(settings.useDarkMode) }

    val themeList = remember {
        listOf(
            Triple("cosmic_slate", "Frosted Glass", Color(0xFFA855F7)),
            Triple("emerald_glow", "Emerald Forest", Color(0xFF10B981)),
            Triple("cyber_glow", "Cyberpunk Neon", Color(0xFFFF007F)),
            Triple("aurora_soft", "Aurora Polaris", Color(0xFF38BDF8))
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlowText(
            text = "Settings Panel",
            theme = theme,
            fontSize = 24f
        )

        // 1. Goal Setting
        GlassCard(theme = theme, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, contentDescription = "Goal", tint = theme.accentGlow)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Set Clean Horizon",
                    color = theme.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Aim for a healthy, clean streak boundary. Target: ${targetDaysState.toInt()} Days",
                color = theme.textSecondary,
                fontSize = 13.sp
            )
            Slider(
                value = targetDaysState,
                onValueChange = { targetDaysState = it },
                valueRange = 7f..180f,
                steps = 10,
                colors = SliderDefaults.colors(
                    thumbColor = theme.accentGlow,
                    activeTrackColor = theme.accentGlow,
                    inactiveTrackColor = theme.outlineBright
                )
            )
        }

        // 2. Reminder scheduler customization
        GlassCard(theme = theme, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.NotificationsActive, contentDescription = "Reminders", tint = theme.accentGlow)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Motivation Reminders",
                    color = theme.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Customize daily alert time & check-in text to stay hyper-vigilant.",
                color = theme.textSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = motivationGoalText,
                onValueChange = { motivationGoalText = it },
                label = { Text("Motivation Text Mantra", color = theme.textSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = theme.accentGlow,
                    unfocusedBorderColor = theme.outlineBright,
                    focusedTextColor = theme.textPrimary,
                    unfocusedTextColor = theme.textPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Simplified Hour/Min Picker
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Alert Time:",
                    color = theme.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Select Hour
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.outlineBright)
                        .clickable {
                            viewModel.triggerHapticFeedback()
                            reminderHourState = (reminderHourState + 1) % 24
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = String.format("%02d Hr", reminderHourState),
                        color = theme.textPrimary,
                        fontSize = 13.sp
                    )
                }

                // Select Min
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(theme.outlineBright)
                        .clickable {
                            viewModel.triggerHapticFeedback()
                            reminderMinuteState = (reminderMinuteState + 15) % 60
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = String.format("%02d Min", reminderMinuteState),
                        color = theme.textPrimary,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            NeumorphicButton(
                theme = theme,
                onClick = { viewModel.triggerImmediateTestReminder() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Trigger Instant Alert Concept Test", color = theme.textPrimary, fontSize = 13.sp)
            }
        }

        // 3. User Appearance Theme selector
        GlassCard(theme = theme, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, contentDescription = "Themes", tint = theme.accentGlow)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Dynamic Glowing Themes",
                    color = theme.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                themeList.forEach { (id, name, color) ->
                    val isSelected = settings.themeMode == id
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) theme.outlineBright else Color.Transparent)
                            .clickable {
                                viewModel.triggerHapticFeedback()
                                viewModel.updateSettings(
                                    targetDays = targetDaysState.toInt(),
                                    reminderHour = reminderHourState,
                                    reminderMinute = reminderMinuteState,
                                    themeMode = id,
                                    useDarkMode = darkToggle,
                                    hapticFeedbackEnabled = hapticToggle,
                                    motivationGoal = motivationGoalText
                                )
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = name,
                            color = theme.textPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // 4. Auxiliary System Swithes
        GlassCard(theme = theme, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Vibration, contentDescription = "Vibe", tint = theme.accentGlow)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Interaction Adjustments",
                    color = theme.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Dark light mode toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("Deep Dark Mode", color = theme.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Saves high OLED display power.", color = theme.textSecondary, fontSize = 11.sp)
                }
                Switch(
                    checked = darkToggle,
                    onCheckedChange = {
                        darkToggle = it
                        viewModel.updateSettings(
                            targetDays = targetDaysState.toInt(),
                            reminderHour = reminderHourState,
                            reminderMinute = reminderMinuteState,
                            themeMode = settings.themeMode,
                            useDarkMode = it,
                            hapticFeedbackEnabled = hapticToggle,
                            motivationGoal = motivationGoalText
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = theme.accentGlow,
                        checkedTrackColor = theme.outlineBright
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Haptic Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("Tactile Haptic Feedback", color = theme.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Trigger subtle buzzes on click.", color = theme.textSecondary, fontSize = 11.sp)
                }
                Switch(
                    checked = hapticToggle,
                    onCheckedChange = {
                        hapticToggle = it
                        viewModel.updateSettings(
                            targetDays = targetDaysState.toInt(),
                            reminderHour = reminderHourState,
                            reminderMinute = reminderMinuteState,
                            themeMode = settings.themeMode,
                            useDarkMode = darkToggle,
                            hapticFeedbackEnabled = it,
                            motivationGoal = motivationGoalText
                        )
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = theme.accentGlow,
                        checkedTrackColor = theme.outlineBright
                    )
                )
            }
        }

        // Save Button (Explicit Action)
        NeumorphicButton(
            theme = theme,
            onClick = {
                viewModel.updateSettings(
                    targetDays = targetDaysState.toInt(),
                    reminderHour = reminderHourState,
                    reminderMinute = reminderMinuteState,
                    themeMode = settings.themeMode,
                    useDarkMode = darkToggle,
                    hapticFeedbackEnabled = hapticToggle,
                    motivationGoal = motivationGoalText
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp)
        ) {
            Text("Apply Setting Changes", fontWeight = FontWeight.Bold)
        }
    }
}
