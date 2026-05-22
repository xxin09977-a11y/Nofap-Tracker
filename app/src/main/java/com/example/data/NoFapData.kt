package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "daily_checks")
data class DailyCheck(
    @PrimaryKey val date: String, // format: "yyyy-MM-dd"
    val isClean: Boolean,
    val notes: String = "",
    val cravingsLevel: Int = 1 // 1 up to 5
)

@Entity(tableName = "streak_records")
data class StreakRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDate: Long, // timestamp
    val endDate: Long? = null, // null if this is the active current streak
    val relapseReason: String = ""
)

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1,
    val targetDays: Int = 30,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val themeMode: String = "cosmic_slate", // options: "cosmic_slate", "emerald_glow", "cyber_glow", "aurora_soft"
    val useDarkMode: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val dailyMotivationGoal: String = "Stay Focused and Mindful"
)

@Dao
interface NoFapDao {
    @Query("SELECT * FROM daily_checks ORDER BY date DESC")
    fun getAllDailyChecksFlow(): Flow<List<DailyCheck>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyCheck(check: DailyCheck)

    @Query("SELECT * FROM streak_records ORDER BY startDate DESC")
    fun getAllStreakRecordsFlow(): Flow<List<StreakRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreakRecord(record: StreakRecord)

    @Query("DELETE FROM streak_records WHERE id = :id")
    suspend fun deleteStreakRecord(id: Long)

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getUserSettingsFlow(): Flow<UserSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserSettings(settings: UserSettings)
}

@Database(entities = [DailyCheck::class, StreakRecord::class, UserSettings::class], version = 1, exportSchema = false)
abstract class NoFapDatabase : RoomDatabase() {
    abstract val noFapDao: NoFapDao

    companion object {
        @Volatile
        private var INSTANCE: NoFapDatabase? = null

        fun getDatabase(context: Context): NoFapDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoFapDatabase::class.java,
                    "nofap_tracker_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class NoFapRepository(private val dao: NoFapDao) {
    val allChecks: Flow<List<DailyCheck>> = dao.getAllDailyChecksFlow()
    val allStreaks: Flow<List<StreakRecord>> = dao.getAllStreakRecordsFlow()
    val userSettings: Flow<UserSettings> = dao.getUserSettingsFlow().map { it ?: UserSettings() }

    suspend fun saveCheck(check: DailyCheck) = dao.insertDailyCheck(check)
    suspend fun saveStreak(record: StreakRecord) = dao.insertStreakRecord(record)
    suspend fun deleteStreak(id: Long) = dao.deleteStreakRecord(id)
    suspend fun saveSettings(settings: UserSettings) = dao.saveUserSettings(settings)
}
