package com.example

import android.app.Application
import android.util.Log
import com.example.data.db.CodexraAlarmDatabase
import com.example.data.repository.CodexraRepository
import com.example.scheduling.AlarmScheduler
import com.example.scheduling.NotificationHelper
import com.example.scheduling.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CodexraApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { CodexraAlarmDatabase.getInstance(this) }
    val repository by lazy {
        CodexraRepository(
            alarmDao = database.alarmDao(),
            reminderDao = database.reminderDao(),
            routineDao = database.routineDao(),
            taskDao = database.taskDao(),
            historyDao = database.historyDao(),
            focusDao = database.focusDao(),
            habitDao = database.habitDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        NotificationHelper.createNotificationChannels(this)

        // Ensure active alarms and reminders are scheduled
        applicationScope.launch {
            try {
                rescheduleAllActive()
            } catch (e: Exception) {
                Log.e("CodexraApp", "Initial scheduling error", e)
            }
        }
    }

    suspend fun rescheduleAllActive() {
        val alarms = repository.allAlarmsFlow.first()
        for (alarm in alarms) {
            if (alarm.isEnabled) {
                AlarmScheduler.scheduleAlarm(this, alarm)
            } else {
                AlarmScheduler.cancelAlarm(this, alarm.id)
            }
        }

        val reminders = repository.allRemindersFlow.first()
        for (reminder in reminders) {
            if (reminder.isEnabled) {
                NotificationScheduler.scheduleReminder(this, reminder)
            } else {
                NotificationScheduler.cancelReminder(this, reminder.id)
            }
        }
    }

    companion object {
        lateinit var instance: CodexraApplication
            private set
    }
}
