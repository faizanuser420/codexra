package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.CodexraApplication
import com.example.scheduling.AlarmScheduler
import com.example.scheduling.NotificationHelper
import com.example.ui.ringing.AlarmRingingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra("EXTRA_ALARM_ID", -1L)
        val title = intent.getStringExtra("EXTRA_ALARM_TITLE") ?: "CODEXRA Alarm"
        val message = intent.getStringExtra("EXTRA_ALARM_MESSAGE") ?: ""
        val isWakeUp = intent.getBooleanExtra("EXTRA_IS_WAKEUP", false)
        val difficulty = intent.getStringExtra("EXTRA_WAKEUP_DIFFICULTY") ?: "MEDIUM"
        val missionType = intent.getStringExtra("EXTRA_WAKEUP_TYPE") ?: "MATHS"
        val ringtoneUri = intent.getStringExtra("EXTRA_RINGTONE_URI")
        val isVibrate = intent.getBooleanExtra("EXTRA_IS_VIBRATE", true)
        val gradualVolume = intent.getBooleanExtra("EXTRA_GRADUAL_VOLUME", false)
        val snoozeMinutes = intent.getIntExtra("EXTRA_SNOOZE_DURATION", 5)
        val voiceMode = intent.getStringExtra("EXTRA_VOICE_MODE") ?: "OFF"

        Log.d("AlarmReceiver", "Received alarm trigger for ID $alarmId (isWakeUp: $isWakeUp)")

        // 1. Post Alarm Notification with full-screen intent
        NotificationHelper.showAlarmNotification(
            context = context,
            alarmId = alarmId,
            title = title,
            message = message,
            isWakeUpAlarm = isWakeUp
        )

        // 2. Launch full-screen ringing activity
        val ringIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            putExtra("EXTRA_ALARM_ID", alarmId)
            putExtra("EXTRA_ALARM_TITLE", title)
            putExtra("EXTRA_ALARM_MESSAGE", message)
            putExtra("EXTRA_IS_WAKEUP", isWakeUp)
            putExtra("EXTRA_WAKEUP_DIFFICULTY", difficulty)
            putExtra("EXTRA_WAKEUP_TYPE", missionType)
            putExtra("EXTRA_RINGTONE_URI", ringtoneUri)
            putExtra("EXTRA_IS_VIBRATE", isVibrate)
            putExtra("EXTRA_GRADUAL_VOLUME", gradualVolume)
            putExtra("EXTRA_SNOOZE_DURATION", snoozeMinutes)
            putExtra("EXTRA_VOICE_MODE", voiceMode)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        try {
            context.startActivity(ringIntent)
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Could not launch AlarmRingingActivity directly", e)
        }

        // 3. Update or reschedule in background database
        if (alarmId != -1L) {
            val app = context.applicationContext as? CodexraApplication
            app?.let { application ->
                CoroutineScope(Dispatchers.IO).launch {
                    val alarm = application.repository.getAlarmById(alarmId)
                    if (alarm != null) {
                        if (alarm.isRecurring) {
                            // Reschedule next occurrence
                            AlarmScheduler.scheduleAlarm(context, alarm)
                        } else {
                            // One-time alarm is consumed
                            application.repository.setAlarmEnabled(alarmId, false)
                        }
                    }
                }
            }
        }
    }
}
