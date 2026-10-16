package com.example.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.model.AlarmEntity
import com.example.receiver.AlarmReceiver
import com.example.ui.ringing.AlarmRingingActivity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"

    fun scheduleAlarm(context: Context, alarm: AlarmEntity) {
        if (!alarm.isEnabled) {
            cancelAlarm(context, alarm.id)
            return
        }

        val triggerEpochMillis = calculateNextTriggerMillis(alarm)
        if (triggerEpochMillis == null) {
            Log.w(TAG, "No valid next trigger for alarm ${alarm.id}")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.codexra.ACTION_ALARM_TRIGGER"
            putExtra("EXTRA_ALARM_ID", alarm.id)
            putExtra("EXTRA_ALARM_TITLE", alarm.title)
            putExtra("EXTRA_ALARM_MESSAGE", alarm.message)
            putExtra("EXTRA_IS_WAKEUP", alarm.isWakeUpAlarm)
            putExtra("EXTRA_WAKEUP_DIFFICULTY", alarm.wakeUpDifficulty)
            putExtra("EXTRA_WAKEUP_TYPE", alarm.wakeUpMissionType)
            putExtra("EXTRA_RINGTONE_URI", alarm.ringtoneUri)
            putExtra("EXTRA_IS_VIBRATE", alarm.isVibrationEnabled)
            putExtra("EXTRA_GRADUAL_VOLUME", alarm.isGradualVolume)
            putExtra("EXTRA_SNOOZE_DURATION", alarm.snoozeDurationMinutes)
            putExtra("EXTRA_VOICE_MODE", alarm.voiceAnnouncementMode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create show intent for AlarmClockInfo
        val showIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            putExtra("EXTRA_ALARM_ID", alarm.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val showPendingIntent = PendingIntent.getActivity(
            context,
            (alarm.id + 100000).toInt(),
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(triggerEpochMillis, showPendingIntent),
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
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerEpochMillis, showPendingIntent),
                    pendingIntent
                )
            }
            Log.d(TAG, "Alarm ${alarm.id} successfully scheduled for $triggerEpochMillis")
        } catch (e: SecurityException) {
            Log.e(TAG, "Exact alarm permission missing", e)
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMillis,
                    pendingIntent
                )
            } catch (ex: Exception) {
                Log.e(TAG, "Failed fallback alarm scheduling", ex)
            }
        }
    }

    fun cancelAlarm(context: Context, alarmId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.example.codexra.ACTION_ALARM_TRIGGER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d(TAG, "Alarm $alarmId cancelled")
    }

    fun calculateNextTriggerMillis(alarm: AlarmEntity, fromTime: LocalDateTime = LocalDateTime.now()): Long? {
        val now = fromTime
        val targetTime = LocalTime.of(alarm.hour, alarm.minute, alarm.second.coerceIn(0, 59))

        // Check end date
        if (alarm.endDate != null) {
            val endDateTime = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(alarm.endDate),
                ZoneId.systemDefault()
            )
            if (now.isAfter(endDateTime)) return null
        }

        // Start date filter
        var baseDate = now.toLocalDate()
        if (alarm.startDate != null) {
            val startDate = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(alarm.startDate),
                ZoneId.systemDefault()
            ).toLocalDate()
            if (startDate.isAfter(baseDate)) {
                baseDate = startDate
            }
        }

        if (!alarm.isRecurring) {
            var candidate = LocalDateTime.of(baseDate, targetTime)
            if (candidate.isBefore(now) || candidate.isEqual(now)) {
                candidate = candidate.plusDays(1)
            }
            return candidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        // Recurring alarm
        val dayTokens = alarm.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
        val activeDays: Set<Int> = if (dayTokens.isEmpty()) {
            (1..7).toSet() // Default daily if no days selected
        } else {
            dayTokens.toSet()
        }

        for (offset in 0..14) {
            val candidateDate = baseDate.plusDays(offset.toLong())
            val candidateDay = candidateDate.dayOfWeek.value // 1 (Mon) .. 7 (Sun)
            if (candidateDay in activeDays) {
                val candidate = LocalDateTime.of(candidateDate, targetTime)
                if (candidate.isAfter(now)) {
                    // Check end date
                    if (alarm.endDate != null) {
                        val endMillis = alarm.endDate
                        val candMillis = candidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        if (candMillis > endMillis) return null
                    }
                    return candidate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                }
            }
        }
        return null
    }
}
