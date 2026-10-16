package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_habits",
    indices = [Index(value = ["isArchived"])]
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "DISCIPLINE", // DISCIPLINE, HEALTH, FITNESS, LEARNING, PRODUCTIVITY, MINDFULNESS
    val frequencyType: String = "DAILY", // DAILY, WEEKDAYS, WEEKLY
    val targetPerDay: Int = 1,
    val unit: String = "times", // times, glasses, pages, mins
    val iconName: String = "flag",
    val colorHex: String = "#00E5FF",
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "user_habit_logs",
    indices = [
        Index(value = ["habitId", "dateEpochDay"], unique = true),
        Index(value = ["dateEpochDay"])
    ]
)
data class HabitLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val dateEpochDay: Long,
    val completedCount: Int = 1,
    val isCompleted: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)
