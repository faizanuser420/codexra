package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_reminders")
data class CustomReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String = "",
    val targetTimestamp: Long = 0L, // For one-time reminder
    val hour: Int,
    val minute: Int,
    val repeatType: String = "NONE", // NONE, DAILY, WEEKDAYS
    val daysOfWeek: String = "", // Comma-separated: 1..7
    val startDate: Long? = null,
    val endDate: Long? = null,
    val soundUri: String = "",
    val soundTitle: String = "Default Notification Sound",
    val isVibrationEnabled: Boolean = true,
    val isEnabled: Boolean = true,
    val categoryTag: String = "CUSTOM", // WATER, TUITION, PRAYER, STUDY, EXERCISE, SHOP, BEDTIME, CUSTOM
    val createdAt: Long = System.currentTimeMillis()
)
