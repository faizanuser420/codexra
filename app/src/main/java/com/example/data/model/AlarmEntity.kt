package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String = "",
    val hour: Int,
    val minute: Int,
    val second: Int = 0,
    val isEnabled: Boolean = true,
    val isRecurring: Boolean = false,
    val daysOfWeek: String = "", // Comma-separated: 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
    val startDate: Long? = null,
    val endDate: Long? = null,
    val ringtoneUri: String = "",
    val ringtoneTitle: String = "Default Alarm Tone",
    val isVibrationEnabled: Boolean = true,
    val isGradualVolume: Boolean = false,
    val snoozeDurationMinutes: Int = 5,
    // CRITICAL: Wake-Up Mission is ONLY enabled for Wake-Up alarms
    val isWakeUpAlarm: Boolean = false,
    val wakeUpDifficulty: String = "MEDIUM", // EASY, MEDIUM, HARD
    val wakeUpMissionType: String = "MATHS", // MATHS, MEMORY, SEQUENCE
    val voiceAnnouncementMode: String = "OFF", // OFF, VOICE_ONLY, SOUND_AND_VOICE
    val createdAt: Long = System.currentTimeMillis()
)
