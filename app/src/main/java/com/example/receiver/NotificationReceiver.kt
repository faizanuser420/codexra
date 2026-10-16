package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.CodexraApplication
import com.example.scheduling.NotificationHelper
import com.example.scheduling.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra("EXTRA_REMINDER_ID", -1L)
        val title = intent.getStringExtra("EXTRA_REMINDER_TITLE") ?: "Scheduled Reminder"
        val message = intent.getStringExtra("EXTRA_REMINDER_MESSAGE") ?: ""
        val soundUri = intent.getStringExtra("EXTRA_SOUND_URI")

        Log.d("NotificationReceiver", "Triggering reminder ID $reminderId: $title")

        // 1. Post notification (NEVER any maths or missions)
        NotificationHelper.showCustomReminderNotification(
            context = context,
            reminderId = reminderId,
            title = title,
            message = message,
            soundUri = soundUri
        )

        // 2. Update repository state & record history
        if (reminderId != -1L) {
            val app = context.applicationContext as? CodexraApplication
            app?.let { application ->
                CoroutineScope(Dispatchers.IO).launch {
                    application.repository.recordHistory(
                        type = "REMINDER_TRIGGERED",
                        title = title,
                        details = message,
                        referenceId = reminderId,
                        isSuccess = true
                    )

                    val reminder = application.repository.getReminderById(reminderId)
                    if (reminder != null) {
                        if (reminder.repeatType != "NONE") {
                            // Reschedule next occurrence
                            NotificationScheduler.scheduleReminder(context, reminder)
                        } else {
                            // One-time reminder consumed
                            application.repository.setReminderEnabled(reminderId, false)
                        }
                    }
                }
            }
        }
    }
}
