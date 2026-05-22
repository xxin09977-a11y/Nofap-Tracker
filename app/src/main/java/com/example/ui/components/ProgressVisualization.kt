package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyCheck
import com.example.data.StreakRecord
import com.example.ui.NoFapViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ProgressVisualizationSection(
    viewModel: NoFapViewModel,
    theme: GlowTheme,
    allChecks: List<DailyCheck>,
    allStreaks: List<StreakRecord>
) {
    var showExportDialog by remember { mutableStateOf(false) }
    var selectedTabState by remember { mutableStateOf(0) } // 0 = Calendar checkin, 1 = Streak history

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            GlowText(
                text = "Progress Center",
                theme = theme,
                fontSize = 24f
            )

            // Export button
            NeumorphicButton(
                theme = theme,
                onClick = { showExportDialog = true },
                modifier = Modifier.height(38.dp)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Export JSON",
                    tint = theme.textPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export", color = theme.textPrimary, fontSize = 12.sp)
            }
        }

        // Custom Glass Select Slider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 18.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(theme.cardBackground)
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedTabState == 0) theme.outlineBright else Color.Transparent)
                    .clickable { selectedTabState = 0 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Calendar Grid",
                    color = theme.textPrimary,
                    fontWeight = if (selectedTabState == 0) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedTabState == 1) theme.outlineBright else Color.Transparent)
                    .clickable { selectedTabState = 1 }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Streak History",
                    color = theme.textPrimary,
                    fontWeight = if (selectedTabState == 1) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp
                )
            }
        }

        if (selectedTabState == 0) {
            CalendarCheckGrid(viewModel = viewModel, theme = theme, checks = allChecks)
        } else {
            StreakHistoryTab(viewModel = viewModel, theme = theme, streaks = allStreaks)
        }

        // Export data dialog
        if (showExportDialog) {
            ExportDataDialog(
                viewModel = viewModel,
                theme = theme,
                onDismiss = { showExportDialog = false }
            )
        }
    }
}

@Composable
fun CalendarCheckGrid(
    viewModel: NoFapViewModel,
    theme: GlowTheme,
    checks: List<DailyCheck>
) {
    val context = LocalContext.current
    var calendarInstance by remember { mutableStateOf(Calendar.getInstance()) }
    val currentMonthYearString = remember(calendarInstance) {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        sdf.format(calendarInstance.time)
    }

    // Modal dialog state for active check-in configuration
    var selectedDayToLog by remember { mutableStateOf<String?>(null) }
    var logNoteState by remember { mutableStateOf("") }
    var cravingBarState by remember { mutableStateOf(2f) }

    // Calendar generation
    val daysInMonth = remember(calendarInstance) {
        calendarInstance.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
    val firstDayOfWeek = remember(calendarInstance) {
        val temp = calendarInstance.clone() as Calendar
        temp.set(Calendar.DAY_OF_MONTH, 1)
        temp.get(Calendar.DAY_OF_WEEK) - 1 // 0-based for offset (0=Sunday, 1=Monday, etc)
    }

    GlassCard(
        theme = theme,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Month Selector
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = {
                viewModel.triggerHapticFeedback()
                val temp = calendarInstance.clone() as Calendar
                temp.add(Calendar.MONTH, -1)
                calendarInstance = temp
            }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = theme.textPrimary)
            }

            Text(
                text = currentMonthYearString,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            IconButton(onClick = {
                viewModel.triggerHapticFeedback()
                val temp = calendarInstance.clone() as Calendar
                temp.add(Calendar.MONTH, 1)
                calendarInstance = temp
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = theme.textPrimary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Week Headers
        val weekHeaders = listOf("S", "M", "T", "W", "T", "F", "S")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekHeaders.forEach {
                Text(
                    text = it,
                    color = theme.textSecondary.copy(alpha = 0.6f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Month Grid Layout
        val checkMap = remember(checks) {
            checks.associateBy { it.date }
        }

        val totalCells = firstDayOfWeek + daysInMonth
        var rowDays = mutableListOf<@Composable () -> Unit>()

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (cellIndex in 0 until totalCells step 7) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (dayInRow in 0..6) {
                        val absoluteCell = cellIndex + dayInRow
                        if (absoluteCell < firstDayOfWeek || absoluteCell >= totalCells) {
                            Box(modifier = Modifier.weight(1f)) // empty placeholder
                        } else {
                            val dayNumber = absoluteCell - firstDayOfWeek + 1
                            val calDate = calendarInstance.clone() as Calendar
                            calDate.set(Calendar.DAY_OF_MONTH, dayNumber)
                            val dateStringKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calDate.time)
                            
                            val checkRecord = checkMap[dateStringKey]
                            val isClean = checkRecord?.isClean

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isClean == true -> Color(0x3D10B981) // Soft Translucent Green
                                            isClean == false -> Color(0x3DEF4444) // Soft Translucent Red
                                            else -> theme.cardBackground.copy(alpha = 0.3f)
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = when {
                                            isClean == true -> Color(0xFF10B981)
                                            isClean == false -> Color(0xFFEF4444)
                                            else -> theme.outlineBright.copy(alpha = 0.4f)
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.triggerHapticFeedback()
                                        selectedDayToLog = dateStringKey
                                        logNoteState = checkRecord?.notes ?: ""
                                        cravingBarState = (checkRecord?.cravingsLevel ?: 2).toFloat()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNumber.toString(),
                                    color = when {
                                        isClean == true -> Color(0xFF10B981)
                                        isClean == false -> Color(0xFFEF4444)
                                        else -> theme.textPrimary
                                    },
                                    fontWeight = if (isClean != null) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clean 🌱", color = theme.textSecondary, fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Slip/Relapse ⚡", color = theme.textSecondary, fontSize = 11.sp)
            }
        }
    }

    // Modal Sheet for Daily Settings Logging
    if (selectedDayToLog != null) {
        AlertDialog(
            onDismissRequest = { selectedDayToLog = null },
            containerColor = theme.backgroundColors.first(),
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Log check-in: $selectedDayToLog",
                    color = theme.textPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "How was your self-control today? Mark clean to build XP or report relapse to heal.",
                        color = theme.textSecondary,
                        fontSize = 12.sp
                    )
                    
                    Text("Craving Level (1-5): ${cravingBarState.toInt()}", color = theme.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = cravingBarState,
                        onValueChange = { cravingBarState = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = theme.accentGlow,
                            activeTrackColor = theme.accentGlow,
                            inactiveTrackColor = theme.outlineBright
                        )
                    )

                    OutlinedTextField(
                        value = logNoteState,
                        onValueChange = { logNoteState = it },
                        placeholder = { Text("How do you feel? Notes (optional)...", color = theme.textSecondary) },
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.logCheckIn(selectedDayToLog!!, true, logNoteState, cravingBarState.toInt())
                            selectedDayToLog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clean 🌱")
                    }
                    Button(
                        onClick = {
                            viewModel.logCheckIn(selectedDayToLog!!, false, logNoteState, cravingBarState.toInt())
                            selectedDayToLog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Relapse ⚡")
                    }
                }
            }
        )
    }
}

@Composable
fun StreakHistoryTab(
    viewModel: NoFapViewModel,
    theme: GlowTheme,
    streaks: List<StreakRecord>
) {
    // Show a high fidelity past streaks canvas-based chart
    val historyOnly = remember(streaks) {
        streaks.filter { it.endDate != null }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // High fidelity custom Canvas chart of past streak records
        GlassCard(theme = theme, modifier = Modifier.fillMaxWidth()) {
            Text(
                "Streak Performance Analytics",
                color = theme.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (historyOnly.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Achieve and end streaks to view progress comparisons here.",
                        color = theme.textSecondary,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp
                    )
                }
            } else {
                val maxStreakDays = remember(historyOnly) {
                    historyOnly.maxOfOrNull {
                        val duration = it.endDate!! - it.startDate
                        (duration / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
                    } ?: 1
                }

                // Custom high performance chart
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .padding(top = 10.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    
                    val itemsToDraw = historyOnly.take(6).reversed() // Show up to last 6 ended streaks
                    val count = itemsToDraw.size
                    val spacing = canvasWidth / (count + 1)
                    val barWidth = 32.dp.toPx()

                    itemsToDraw.forEachIndexed { index, streak ->
                        val durationDays = ((streak.endDate!! - streak.startDate) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(1)
                        val ratio = durationDays.toFloat() / maxStreakDays.toFloat()
                        val barHeight = canvasHeight * ratio * 0.82f
                        
                        val x = spacing * (index + 1) - (barWidth / 2)
                        val y = canvasHeight - barHeight

                        // Draw background glow rect
                        drawRect(
                            color = theme.accentGlow.copy(alpha = 0.15f),
                            topLeft = Offset(x - 2, y - 2),
                            size = Size(barWidth + 4, barHeight + 4)
                        )

                        // Draw main gradient bar
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(theme.primaryGradient[0], theme.primaryGradient[1])
                            ),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight)
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Oldest", color = theme.textSecondary, fontSize = 9.sp)
                    Text("Latest Past Streaks (Days)", color = theme.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                    Text("Newest", color = theme.textSecondary, fontSize = 9.sp)
                }
            }
        }

        // List of all completed streaks
        Text(
            "Historic Relapse Logs",
            color = theme.textPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(top = 6.dp)
        )

        if (historyOnly.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(30.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No history records stored yet.",
                    color = theme.textSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(historyOnly) { streak ->
                    val durationVal = ((streak.endDate!! - streak.startDate) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                    val startStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(streak.startDate))
                    val endStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(streak.endDate))

                    GlassCard(
                        theme = theme,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp),
                        glowColor = theme.outlineBright.copy(alpha = 0.3f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    "Streak: $durationVal Days Clean",
                                    color = theme.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "$startStr - $endStr",
                                    color = theme.textSecondary,
                                    fontSize = 11.sp
                                )
                                if (streak.relapseReason.isNotEmpty()) {
                                    Text(
                                        "Trigger: \"${streak.relapseReason}\"",
                                        color = theme.accentGlow,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(theme.outlineBright),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🛡️", fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExportDataDialog(
    viewModel: NoFapViewModel,
    theme: GlowTheme,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val jsonString = remember { viewModel.getExportJsonString() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = theme.backgroundColors.first(),
        title = {
            Text(
                "Export Clean Habit Backups",
                color = theme.textPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Copy your personal habit history encoded as lightweight JSON offline data. Save it in your text files safely.",
                    color = theme.textSecondary,
                    fontSize = 12.sp
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = jsonString,
                        color = Color(0xFF10B981),
                        fontSize = 10.sp,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        },
        confirmButton = {
            NeumorphicButton(
                theme = theme,
                onClick = {
                    try {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        if (clipboard != null) {
                            val clip = ClipData.newPlainText("NoFap Export", jsonString)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied backup to clipboard!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Clipboard service not available", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to write to clipboard", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                    onDismiss()
                }
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = theme.textPrimary, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Data", color = theme.textPrimary, fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = theme.textSecondary)
            }
        }
    )
}
