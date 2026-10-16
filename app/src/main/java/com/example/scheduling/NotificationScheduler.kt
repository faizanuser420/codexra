package com.example.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.CustomReminderEntity
import com.example.receiver.NotificationReceiver
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object NotificationScheduler {

    private const val TAG = "NotificationScheduler"

    fun scheduleReminder(context: Context, reminder: CustomReminderEntity) {
        if (!reminder.isEnabled) {
            cancelReminder(context, reminder.id)
            return
        }

        val triggerEpochMillis = calculateNextTriggerMillis(reminder)
        if (triggerEpochMillis == null) {
            Log.w(TAG, "No valid next trigger for reminder ${reminder.id}")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.codexra.ACTION_REMINDER_TRIGGER"
            putExtra("EXTRA_REMINDER_ID", reminder.id)
            putExtra("EXTRA_REMINDER_TITLE", reminder.title)
            putExtra("EXTRA_REMINDER_MESSAGE", reminder.message)
            putExtra("EXTRA_SOUND_URI", reminder.soundUri)
            putExtra("EXTRA_IS_VIBRATE", reminder.isVibrationEnabled)
            putExtra("EXTRA_CATEGORY", reminder.categoryTag)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (reminder.id + 500000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Reminder ${reminder.id} scheduled for $triggerEpochMillis")
        } catch (e: Exception) {
            Log.e(TAG, "Failed scheduling reminder ${reminder.id}", e)
        }
    }

    fun cancelReminder(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            action = "com.example.codexra.ACTION_REMINDER_TRIGGER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId + 500000).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Reminder $reminderId cancelled")
    }

    fun calculateNextTriggerMillis(reminder: CustomReminderEntity, fromTime: LocalDateTime = LocalDateTime.now()): Long? {
        val now = fromTime
        val targetTime = LocalTime.of(reminder.hour, reminder.minute)

        // Check end date
        if (reminder.endDate != null) {
            val endDateTime = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(reminder.endDate),
                ZoneId.systemDefault()
            )
            if (now.isAfter(endDateTime)) return null
        }

        var baseDate = now.toLocalDate()
        if (reminder.startDate != null) {
            val startDate = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(reminder.startDate),
                ZoneId.systemDefault()
            ).toLocalDate()
            if (startDate.isAfter(baseDate)) {
                baseDate = startDate
            }
        }

        when (reminder.repeatType) {
            "NONE" -> {
                // If targetTimestamp is set and in future, use it
                if (reminder.targetTimestamp > now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) {
                    return reminder.targetTimestamp
                }
                var candidate = LocalDateTime.of(baseDate, targetTime)
                if (candidate.isBefore(now) || candidate.isEqual(now)) {
                    candidate = candidate.plusDays(1)
                }
                return candidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
            "DAILY" -> {
                var candidate = LocalDateTime.of(baseDate, targetTime)
                if (candidate.isBefore(now) || candidate.isEqual(now)) {
                    candidate = candidate.plusDays(1)
                }
                return candidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
            "WEEKDAYS" -> {
                val dayTokens = reminder.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
                val activeDays: Set<Int> = if (dayTokens.isEmpty()) {
                    setOf(1, 2, 3, 4, 5) // Mon-Fri default for weekdays
                } else {
                    dayTokens.toSet()
                }

                for (offset in 0..14) {
                    val candidateDate = baseDate.plusDays(offset.toLong())
                    val candidateDay = candidateDate.dayOfWeek.value
                    if (candidateDay in activeDays) {
                        val candidate = LocalDateTime.of(candidateDate, targetTime)
                        if (candidate.isAfter(now)) {
                            return candidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        }
                    }
                }
                return null
            }
            else -> {
                var candidate = LocalDateTime.of(baseDate, targetTime)
                if (candidate.isBefore(now) || candidate.isEqual(now)) {
                    candidate = candidate.plusDays(1)
                }
                return candidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
        }
    }
}
