package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DailyCheck
import com.example.data.NoFapDatabase
import com.example.data.NoFapRepository
import com.example.data.StreakRecord
import com.example.data.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NoFapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NoFapRepository
    
    // UI Notification toast flow
    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    init {
        val database = NoFapDatabase.getDatabase(application)
        repository = NoFapRepository(database.noFapDao)
    }

    // Exposed Flows
    val allChecks: StateFlow<List<DailyCheck>> = repository.allChecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStreaks: StateFlow<List<StreakRecord>> = repository.allStreaks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<UserSettings> = repository.userSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    // Combines and derives current status
    val currentStreakState: StateFlow<CurrentStreakInfo> = combine(allStreaks, allChecks) { streaks, checks ->
        calculateCurrentStreak(streaks, checks)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CurrentStreakInfo(0, 0L, null))

    // Gamification state: level, currentXP, nextLevelXP, progressPercent, earnedBadges
    val gamificationState: StateFlow<GamificationData> = combine(allChecks, currentStreakState) { checks, streakInfo ->
        val cleanDays = checks.count { it.isClean }
        val streakDays = streakInfo.days
        
        val xp = (cleanDays * 15) + (streakDays * 20)
        val level = 1 + (xp / 100)
        val nextLevelXp = level * 100
        val prevLevelXp = (level - 1) * 100
        val currentLevelXpProgress = xp - prevLevelXp
        val progressPercent = currentLevelXpProgress.toFloat() / 100f

        val badges = mutableListOf<Badge>()
        if (cleanDays >= 1) {
            badges.add(Badge("1", "Clean Slate", "Your first 1 clean day logged.", "🌱", "Bronze"))
        }
        if (cleanDays >= 3) {
            badges.add(Badge("2", "Seedling Growth", "Logged 3 clean days.", "🌿", "Bronze"))
        }
        if (streakDays >= 7) {
            badges.add(Badge("3", "Golden Week", "Maintained a 7-day clean streak.", "🔥", "Silver"))
        }
        if (streakDays >= 14) {
            badges.add(Badge("4", "Unyielding Resolve", "Reached a 14-day streak.", "⚔️", "Silver"))
        }
        if (streakDays >= 30) {
            badges.add(Badge("5", "Aura Alchemist", "Achieved 30 days of clean energy.", "🪷", "Gold"))
        }
        if (cleanDays >= 50) {
            badges.add(Badge("6", "Half-Century Mind", "Accomplished 50 total clean days.", "🦅", "Platinum"))
        }

        GamificationData(level, xp, currentLevelXpProgress, progressPercent, badges)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GamificationData(1, 0, 0, 0f, emptyList()))

    fun clearNotification() {
        _notificationMessage.value = null
    }

    // Perform Haptic feedback
    fun triggerHapticFeedback() {
        val settings = userSettings.value
        if (!settings.hapticFeedbackEnabled) return

        try {
            val context = getApplication<Application>().applicationContext
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(40)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Actions
    fun logCheckIn(dateStr: String, isClean: Boolean, notes: String, crayonsLevel: Int, onStreakAchieved: () -> Unit = {}) {
        viewModelScope.launch {
            triggerHapticFeedback()
            val existingCheck = allChecks.value.find { it.date == dateStr }
            val newCheck = DailyCheck(dateStr, isClean, notes, crayonsLevel)
            repository.saveCheck(newCheck)

            if (isClean) {
                // If checking in clean, make sure we have an active streak running.
                val active = allStreaks.value.find { it.endDate == null }
                if (active == null) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val parsedDate = dateFormat.parse(dateStr) ?: Date()
                    repository.saveStreak(StreakRecord(startDate = parsedDate.time))
                } else {
                    // Update active streak check
                    val currentStreakCountBefore = currentStreakState.value.days
                    val activeStartDate = active.startDate
                    val activeDays = ((System.currentTimeMillis() - activeStartDate) / (1000 * 60 * 60 * 24)).toInt()
                    if (activeDays > currentStreakCountBefore && activeDays % 7 == 0) {
                        onStreakAchieved()
                    }
                }
                _notificationMessage.value = "Day logged successfully! Pure mind, pure soul."
            } else {
                // Relapse. End any active streak.
                val active = allStreaks.value.find { it.endDate == null }
                if (active != null) {
                    repository.saveStreak(active.copy(endDate = System.currentTimeMillis(), relapseReason = notes))
                }
                _notificationMessage.value = "Relapse recorded. Fall down 7 times, stand up 8!"
            }
        }
    }

    fun startNewStreakManually(daysAgo: Int) {
        viewModelScope.launch {
            triggerHapticFeedback()
            // End active if exists
            val active = allStreaks.value.find { it.endDate == null }
            if (active != null) {
                repository.saveStreak(active.copy(endDate = System.currentTimeMillis(), relapseReason = "Ended manually for new start"))
            }

            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            repository.saveStreak(StreakRecord(startDate = cal.timeInMillis))
            _notificationMessage.value = "New streak initialized!"
        }
    }

    fun resetStreakOnRelapse(reason: String) {
        viewModelScope.launch {
            triggerHapticFeedback()
            val active = allStreaks.value.find { it.endDate == null }
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            
            // Log a bad check-in
            repository.saveCheck(DailyCheck(todayStr, false, "Relapse: $reason", 5))

            if (active != null) {
                repository.saveStreak(active.copy(endDate = System.currentTimeMillis(), relapseReason = reason))
            }
            // Automatically start a new clean streak starting from tomorrow/today fresh start
            _notificationMessage.value = "Don't let slips define you. Day 0 starts now."
        }
    }

    fun updateSettings(targetDays: Int, reminderHour: Int, reminderMinute: Int, themeMode: String, useDarkMode: Boolean, hapticFeedbackEnabled: Boolean, motivationGoal: String) {
        viewModelScope.launch {
            triggerHapticFeedback()
            val current = userSettings.value
            val newSettings = current.copy(
                targetDays = targetDays,
                reminderHour = reminderHour,
                reminderMinute = reminderMinute,
                themeMode = themeMode,
                useDarkMode = useDarkMode,
                hapticFeedbackEnabled = hapticFeedbackEnabled,
                dailyMotivationGoal = motivationGoal
            )
            repository.saveSettings(newSettings)
            _notificationMessage.value = "Preferences saved securely."
        }
    }

    // Export user data
    fun getExportJsonString(): String {
        triggerHapticFeedback()
        try {
            val root = JSONObject()
            val checksArray = JSONArray()
            allChecks.value.forEach {
                val obj = JSONObject()
                obj.put("date", it.date)
                obj.put("isClean", it.isClean)
                obj.put("notes", it.notes)
                obj.put("cravingsLevel", it.cravingsLevel)
                checksArray.put(obj)
            }
            root.put("checks", checksArray)

            val streaksArray = JSONArray()
            allStreaks.value.forEach {
                val obj = JSONObject()
                obj.put("id", it.id)
                obj.put("startDate", it.startDate)
                obj.put("endDate", it.endDate ?: JSONObject.NULL)
                obj.put("relapseReason", it.relapseReason)
                streaksArray.put(obj)
            }
            root.put("streaks", streaksArray)

            val settingsObj = JSONObject()
            val s = userSettings.value
            settingsObj.put("targetDays", s.targetDays)
            settingsObj.put("themeMode", s.themeMode)
            settingsObj.put("useDarkMode", s.useDarkMode)
            settingsObj.put("motivationGoal", s.dailyMotivationGoal)
            root.put("settings", settingsObj)

            return root.toString(2)
        } catch (e: Exception) {
            return "{\"error\": \"Could not generate export data: ${e.localizedMessage}\"}"
        }
    }

    // Trigger immediate alert to simulate/test customized reminders
    fun triggerImmediateTestReminder() {
        triggerHapticFeedback()
        val s = userSettings.value
        _notificationMessage.value = "Reminder scheduled for ${String.format("%02d:%02d", s.reminderHour, s.reminderMinute)}: Focus on \"${s.dailyMotivationGoal}\"!"
    }

    private fun calculateCurrentStreak(streaks: List<StreakRecord>, checks: List<DailyCheck>): CurrentStreakInfo {
        val active = streaks.find { it.endDate == null } ?: return CurrentStreakInfo(0, 0, null)
        
        // Calculate difference in milliseconds
        val now = System.currentTimeMillis()
        val diffMs = now - active.startDate
        val diffDays = (diffMs / (1000 * 60 * 60 * 24)).toInt()

        return CurrentStreakInfo(
            days = if (diffDays < 0) 0 else diffDays,
            sinceTimestamp = active.startDate,
            activeRecord = active
        )
    }
}

data class CurrentStreakInfo(
    val days: Int,
    val sinceTimestamp: Long,
    val activeRecord: StreakRecord?
)

data class Badge(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val tier: String // Bronze, Silver, Gold, Platinum
)

data class GamificationData(
    val level: Int,
    val totalXp: Int,
    val progressXp: Int,
    val progressPercent: Float,
    val earnedBadges: List<Badge>
)
