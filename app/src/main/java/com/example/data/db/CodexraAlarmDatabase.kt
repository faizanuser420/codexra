package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AlarmEntity
import com.example.data.model.CustomReminderEntity
import com.example.data.model.FocusSessionEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.HistoryRecordEntity
import com.example.data.model.RoutineEntity
import com.example.data.model.RoutineLogEntity
import com.example.data.model.TaskEntity

/**
 * Room Database for CodexraAlarm application.
 * Provides access to DAOs for alarms, custom notifications, routines, tasks,
 * user habits, focus sessions, and discipline history.
 */
@Database(
    entities = [
        AlarmEntity::class,
        CustomReminderEntity::class,
        RoutineEntity::class,
        RoutineLogEntity::class,
        TaskEntity::class,
        HistoryRecordEntity::class,
        FocusSessionEntity::class,
        HabitEntity::class,
        HabitLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CodexraAlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun reminderDao(): ReminderDao
    abstract fun routineDao(): RoutineDao
    abstract fun taskDao(): TaskDao
    abstract fun historyDao(): HistoryDao
    abstract fun focusDao(): FocusDao
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: CodexraAlarmDatabase? = null

        private const val DATABASE_NAME = "codexra_alarm.db"

        /**
         * Returns the singleton instance of [CodexraAlarmDatabase], initializing it
         * with Room.databaseBuilder if not already instantiated.
         */
        fun getInstance(context: Context): CodexraAlarmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CodexraAlarmDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Typealias to maintain backward compatibility across existing references.
 */
typealias CodexraDatabase = CodexraAlarmDatabase
