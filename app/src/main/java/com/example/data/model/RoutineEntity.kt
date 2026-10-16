package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routines")
data class RoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val hour: Int,
    val minute: Int,
    val repeatPattern: String = "DAILY", // DAILY, WEEKDAYS, WEEKENDS, CUSTOM
    val daysOfWeek: String = "1,2,3,4,5,6,7",
    val category: String = "CUSTOM", // FAJR, PRAYER, EXERCISE, STUDY, TUITION, SHOP, WATER, BEDTIME, CUSTOM
    val isEnabled: Boolean = true,
    val hasLinkedNotification: Boolean = false,
    val linkedReminderId: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "routine_logs")
data class RoutineLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: Long,
    val dateEpochDay: Long, // e.g. LocalDate.now().toEpochDay()
    val isCompleted: Boolean = false,
    val isSkipped: Boolean = false,
    val completedAt: Long = System.currentTimeMillis()
)
