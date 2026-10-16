package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_records")
data class HistoryRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // ALARM_DISMISSED, ALARM_SNOOZED, MISSION_SUCCESS, EMERGENCY_DISMISSED, REMINDER_TRIGGERED, FOCUS_COMPLETED
    val referenceId: Long? = null,
    val title: String,
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSuccess: Boolean = true
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val durationMinutes: Int,
    val breakMinutes: Int = 5,
    val completedCycles: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String = "WORK" // STUDY, WORK, CODING, READING, TACTICAL
)
