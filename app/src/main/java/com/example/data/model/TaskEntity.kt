package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDate: Long, // Epoch millis for due date
    val dueHour: Int? = null,
    val dueMinute: Int? = null,
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val category: String = "GENERAL", // WORK, STUDY, HEALTH, PERSONAL, GENERAL
    val isRecurring: Boolean = false,
    val recurrenceDays: Int = 0, // 0 = none, 1 = daily, 7 = weekly
    val status: String = "PENDING", // PENDING, COMPLETED, SKIPPED, OVERDUE
    val completedAt: Long? = null,
    val linkedAlarmId: Long? = null,
    val linkedReminderId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
