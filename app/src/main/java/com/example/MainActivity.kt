package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.NoFapViewModel
import com.example.ui.components.BenefitsExplorerSection
import com.example.ui.components.GamificationBadgesSection
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowTheme
import com.example.ui.components.GlowText
import com.example.ui.components.NeumorphicButton
import com.example.ui.components.ProgressVisualizationSection
import com.example.ui.components.RemindersSettingsSection
import com.example.ui.components.ThemeRegistry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: NoFapViewModel = viewModel()
            val settings by viewModel.userSettings.collectAsStateWithLifecycle()
            val currentTheme = remember(settings.themeMode, settings.useDarkMode) {
                ThemeRegistry.getTheme(settings.themeMode, settings.useDarkMode)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black) // fallback background
            ) {
                // Background Gradients with Floating particles
                FloatingBackground(theme = currentTheme)
                
                // Screen container
                NoFapAppContent(viewModel = viewModel, theme = currentTheme)
            }
        }
    }
}

@Composable
fun FloatingBackground(theme: GlowTheme) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")
    
    // Animate coordinates for slow drifting orbs
    val offset1Y by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb1"
    )

    val offset2X by infiniteTransition.animateFloat(
        initialValue = 100f,
        targetValue = 900f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Draw general layout background gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(theme.backgroundColors[0], theme.backgroundColors[1])
            ),
            size = size
        )

        // Floating glowing particle 1 (Primary gradient start color)
        if (w > 0f) {
            val radius1 = (w * 0.7f).coerceAtLeast(1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(theme.primaryGradient[0].copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(w * 0.2f, offset1Y),
                    radius = radius1
                ),
                radius = radius1,
                center = Offset(w * 0.2f, offset1Y)
            )
        }

        // Floating glowing particle 2 (Accent glow color)
        if (w > 0f && h > 0f) {
            val radius2 = (w * 0.6f).coerceAtLeast(1f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(theme.accentGlow.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(offset2X, h * 0.7f),
                    radius = radius2
                ),
                radius = radius2,
                center = Offset(offset2X, h * 0.7f)
            )
        }
    }
}

@Composable
fun NoFapAppContent(viewModel: NoFapViewModel, theme: GlowTheme) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(0) } // 0 = Active, 1 = Calendar/Progress, 2 = Aura, 3 = Benefits, 4 = Settings
    
    // Back gesture handles returning to Tab 0 if on secondary tab
    BackHandler(enabled = currentTab != 0) {
        viewModel.triggerHapticFeedback()
        currentTab = 0
    }

    // Modal dialogs states on the dashboard
    var showRelapseDialog by remember { mutableStateOf(false) }
    var showPreseedDialog by remember { mutableStateOf(false) }
    var showCelebrationOverlay by remember { mutableStateOf(false) }
    var celebrationMilestoneText by remember { mutableStateOf("7 Days") }

    // Core states
    val allChecks by viewModel.allChecks.collectAsStateWithLifecycle()
    val allStreaks by viewModel.allStreaks.collectAsStateWithLifecycle()
    val currentStreakInfo by viewModel.currentStreakState.collectAsStateWithLifecycle()
    val gamificationData by viewModel.gamificationState.collectAsStateWithLifecycle()
    val settings by viewModel.userSettings.collectAsStateWithLifecycle()
    val toastMsg by viewModel.notificationMessage.collectAsStateWithLifecycle()

    LaunchedEffect(toastMsg) {
        toastMsg?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearNotification()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = Color.Transparent,
        bottomBar = {
            GlassBottomNavigation(currentTab = currentTab, theme = theme, onTabSelect = {
                viewModel.triggerHapticFeedback()
                currentTab = it
            })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Animate content switches smoothly
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "nav_content"
            ) { targetTab ->
                when (targetTab) {
                    0 -> ActiveTrackerDashboard(
                        viewModel = viewModel,
                        theme = theme,
                        days = currentStreakInfo.days,
                        targetDays = settings.targetDays,
                        motivationText = settings.dailyMotivationGoal,
                        gamificationData = gamificationData,
                        onRelapsePressed = { showRelapseDialog = true },
                        onPreseedPressed = { showPreseedDialog = true },
                        onSharePressed = {
                            try {
                                val activeText = "Reclaiming my life! Currently building my mind. Current NoFap Streak: ${currentStreakInfo.days} days clean! Join me in reclaiming your mental aura! 🌱⚡ #NoFapRestrive"
                                val intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, activeText)
                                    type = "text/plain"
                                }
                                val chooser = Intent.createChooser(intent, "Share Streak Progression")
                                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(chooser)
                            } catch (e: Exception) {
                                Toast.makeText(context, "No sharing apps available on this device", Toast.LENGTH_SHORT).show()
                                e.printStackTrace()
                            }
                        },
                        onManualCelebrateTrigger = {
                            celebrationMilestoneText = "${currentStreakInfo.days} Days Clean"
                            showCelebrationOverlay = true
                        }
                    )
                    1 -> ProgressVisualizationSection(
                        viewModel = viewModel,
                        theme = theme,
                        allChecks = allChecks,
                        allStreaks = allStreaks
                    )
                    2 -> GamificationBadgesSection(
                        viewModel = viewModel,
                        theme = theme,
                        gamificationData = gamificationData
                    )
                    3 -> BenefitsExplorerSection(
                        theme = theme,
                        currentStreakDays = currentStreakInfo.days
                    )
                    4 -> RemindersSettingsSection(
                        viewModel = viewModel,
                        theme = theme,
                        settings = settings
                    )
                }
            }

            // Relapse input dialog modal
            if (showRelapseDialog) {
                var relapseReason by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showRelapseDialog = false },
                    containerColor = theme.backgroundColors.first(),
                    title = {
                        Text(
                            "Record Slip & Reset Streak",
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "It's alright. A slip is a lesson, not a death sentence. Write down what triggered you so you can bypass it next time.",
                                color = theme.textSecondary,
                                fontSize = 13.sp
                            )
                            OutlinedTextField(
                                value = relapseReason,
                                onValueChange = { relapseReason = it },
                                placeholder = { Text("E.g. Sleepiness, late social media scrolling...", color = theme.textSecondary) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = theme.accentGlow,
                                    unfocusedBorderColor = theme.outlineBright,
                                    focusedTextColor = theme.textPrimary,
                                    unfocusedTextColor = theme.textPrimary
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.resetStreakOnRelapse(relapseReason.ifEmpty { "Urge trigger" })
                                showRelapseDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("Reset Counter ⚡")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRelapseDialog = false }) {
                            Text("Cancel", color = theme.textSecondary)
                        }
                    }
                )
            }

            // Preseed manual starter day selector
            if (showPreseedDialog) {
                var daysEntered by remember { mutableStateOf("") }
                AlertDialog(
                    onDismissRequest = { showPreseedDialog = false },
                    containerColor = theme.backgroundColors.first(),
                    title = {
                        Text(
                            "Import Previous Clean Days",
                            color = theme.textPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Enter the total finished clean days you successfully achieved before downloading the tracking app, to resume your streak count immediately.",
                                color = theme.textSecondary,
                                fontSize = 13.sp
                            )
                            OutlinedTextField(
                                value = daysEntered,
                                onValueChange = { daysEntered = it },
                                placeholder = { Text("E.g. 5, 10, 30 days...", color = theme.textSecondary) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = theme.accentGlow,
                                    unfocusedBorderColor = theme.outlineBright,
                                    focusedTextColor = theme.textPrimary,
                                    unfocusedTextColor = theme.textPrimary
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val d = daysEntered.toIntOrNull() ?: 0
                                if (d >= 0) {
                                    viewModel.startNewStreakManually(d)
                                    showPreseedDialog = false
                                } else {
                                    Toast.makeText(context, "Please enter a valid day number.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accentGlow)
                        ) {
                            Text("Apply Days", color = theme.backgroundColors.first())
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPreseedDialog = false }) {
                            Text("Cancel", color = theme.textSecondary)
                        }
                    }
                )
            }

            // Milestone Achieved celebration overlay overlay
            if (showCelebrationOverlay) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.82f))
                        .clickable { showCelebrationOverlay = false },
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(
                        theme = theme,
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(16.dp),
                        glowColor = theme.accentGlow
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("✨ EXTREME FOCUS UNLOCKED ✨", color = theme.accentGlow, fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 2.sp)
                            
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(theme.outlineBright.copy(alpha = 0.4f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🦅", fontSize = 54.sp)
                            }

                            Text(
                                text = "Milestone: $celebrationMilestoneText!",
                                color = theme.textPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "You successfully reset another cycle of androgen pathways. Your willpower is unmatched, your mind is returning to healthy defaults. Pure focus awaits!",
                                color = theme.textSecondary,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                textAlign = TextAlign.Center
                            )

                            NeumorphicButton(
                                theme = theme,
                                onClick = { showCelebrationOverlay = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Continue Aura Transmutation ⚔️", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveTrackerDashboard(
    viewModel: NoFapViewModel,
    theme: GlowTheme,
    days: Int,
    targetDays: Int,
    motivationText: String,
    gamificationData: com.example.ui.GamificationData,
    onRelapsePressed: () -> Unit,
    onPreseedPressed: () -> Unit,
    onManualCelebrateTrigger: () -> Unit,
    onSharePressed: () -> Unit
) {
    val progressRatio = remember(days, targetDays) {
        (days.toFloat() / targetDays.toFloat()).coerceIn(0f..1f)
    }

    // Dynamic scale for pulse effect on day count inside the circle!
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Animated rotation angle for dynamic spinning outer dashed orbital ring
    val angleRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // App title with minimalist neon alignment
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(theme.accentGlow)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "NoFap Tracker",
                color = theme.textPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Mantra Card
        GlassCard(
            theme = theme,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🕊️", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Daily Mantra & Target Focus", color = theme.accentGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "\"$motivationText\"",
                        color = theme.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Crown Jewel: Massive Glowing Progress Ring
        Box(
            modifier = Modifier
                .size(240.dp)
                .drawBehind {
                    // Draw outer ambient glowing aura
                    val radiusVal = size.maxDimension * 0.72f
                    if (radiusVal > 0f) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(theme.accentGlow.copy(alpha = 0.16f * scalePulse), Color.Transparent),
                                center = center,
                                radius = radiusVal
                            ),
                            radius = radiusVal
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // 1. Spinning dashed outline (as requested by Frosted Glass HTML template)
            Canvas(
                modifier = Modifier
                    .size(216.dp)
                    .rotate(angleRotation)
            ) {
                drawCircle(
                    color = theme.outlineBright.copy(alpha = 0.15f),
                    radius = size.minDimension / 2,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 4.dp.toPx(),
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(15f, 15f),
                            0f
                        )
                    )
                )
            }

            // Outer ring frame
            Box(
                modifier = Modifier
                    .size(208.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.5.dp,
                        color = theme.outlineBright.copy(alpha = 0.18f),
                        shape = CircleShape
                    )
            )

            // 2. Translucent Glass Track
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(theme.cardBackground)
                    .border(
                        width = 1.dp,
                        color = theme.outlineBright.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
            )

            // 3. Dynamic progress border (purple-blue gradient)
            Canvas(modifier = Modifier.size(198.dp)) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(theme.primaryGradient[0], theme.primaryGradient[1], theme.primaryGradient[0])
                    ),
                    startAngle = -90f,
                    sweepAngle = progressRatio * 360f,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 8.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )
            }

            // 4. Center Glass Orb Box
            Box(
                modifier = Modifier
                    .size(174.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.14f),
                                Color.White.copy(alpha = 0.04f)
                            )
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        color = theme.outlineBright,
                        shape = CircleShape
                    )
                    .clickable {
                        onManualCelebrateTrigger()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "CLEAN STREAK",
                        color = theme.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = days.toString(),
                        color = theme.textPrimary,
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Black,
                        style = TextStyle(
                            shadow = Shadow(
                                color = theme.accentGlow.copy(alpha = 0.85f),
                                blurRadius = 16f
                            )
                        )
                    )
                    Text(
                        text = "Days Total",
                        color = theme.accentGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // 5. Floating gold-orange badge as shown in the design template
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = (-6).dp, y = (-6).dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFBBF24), Color(0xFFF97316))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = "Premium Streak Badge",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Share & preseed auxiliary selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Share
            NeumorphicButton(
                theme = theme,
                onClick = onSharePressed,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Share, "Share", tint = theme.textPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share Aura", color = theme.textPrimary, fontSize = 12.sp)
            }

            // Import Pre-seed
            NeumorphicButton(
                theme = theme,
                onClick = onPreseedPressed,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.TrendingUp, "Preseed", tint = theme.textPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Preseed Days", color = theme.textPrimary, fontSize = 12.sp)
            }
        }

        // Quick Stats Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassCard(theme = theme, modifier = Modifier.weight(1f)) {
                Text("Target Goal", color = theme.textSecondary, fontSize = 11.sp)
                Text("$targetDays Days", color = theme.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
            GlassCard(theme = theme, modifier = Modifier.weight(1f)) {
                Text("Aura Level", color = theme.textSecondary, fontSize = 11.sp)
                Text("LVL ${gamificationData.level}", color = theme.accentGlow, fontSize = 16.sp, fontWeight = FontWeight.Black)
            }
        }

        // Slip Relapse Emergency Core Button
        Button(
            onClick = onRelapsePressed,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0x3DEF4444)),
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = Color(0xFFEF4444), shape = RoundedCornerShape(20.dp))
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text(
                text = "Report Slip / Relapse ⚡",
                color = Color(0xFFEF4444),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
    }
}

// Low latency, highly optimized Glass Bottom Nav Bar matching edge-to-edge guidelines
@Composable
fun GlassBottomNavigation(
    currentTab: Int,
    theme: GlowTheme,
    onTabSelect: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars) // respects system gesture/pill navigation safely
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(theme.cardBackground)
            .border(
                width = 1.2.dp,
                color = theme.outlineBright.copy(alpha = 0.20f),
                shape = RoundedCornerShape(32.dp)
            )
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navItems = listOf(
                Triple(0, "Tracker", Icons.Default.OfflineBolt),
                Triple(1, "Calendar", Icons.Default.DateRange),
                Triple(2, "Awards", Icons.Default.WorkspacePremium),
                Triple(3, "Benefits", Icons.Default.MenuBook),
                Triple(4, "Settings", Icons.Default.Settings)
            )

            navItems.forEach { (index, label, icon) ->
                val isSelected = currentTab == index
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "scale"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTabSelect(index) }
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) theme.accentGlow else theme.textSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(if (isSelected) 24.dp else 21.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        color = if (isSelected) theme.textPrimary else theme.textSecondary.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
