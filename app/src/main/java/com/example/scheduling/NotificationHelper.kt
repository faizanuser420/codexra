package com.example.scheduling

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.ui.ringing.AlarmRingingActivity

object NotificationHelper {

    const val CHANNEL_ALARM_ID = "codexra_alarm_channel_v1"
    const val CHANNEL_REMINDER_ID = "codexra_reminder_channel_v1"
    private const val CHANNEL_CUSTOM_PREFIX = "codexra_sound_chan_"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            // High-priority Alarm channel
            val alarmChannel = NotificationChannel(
                CHANNEL_ALARM_ID,
                "CODEXRA Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alarm alerts with full-screen wake capabilities"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 1000)
                enableLights(true)
                lightColor = Color.CYAN
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            notificationManager.createNotificationChannel(alarmChannel)

            // Reminder channel
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDER_ID,
                "CODEXRA Custom Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Scheduled custom notification reminders"
                enableVibration(true)
                enableLights(true)
                lightColor = Color.YELLOW
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }

    /**
     * Resolves or dynamically creates a dedicated channel if a custom sound is specified
     */
    fun getOrCreateSoundChannel(context: Context, soundUriString: String?): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || soundUriString.isNullOrBlank()) {
            return CHANNEL_REMINDER_ID
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return CHANNEL_REMINDER_ID
        val cleanHash = Math.abs(soundUriString.hashCode()).toString()
        val customChannelId = "$CHANNEL_CUSTOM_PREFIX$cleanHash"

        val existing = notificationManager.getNotificationChannel(customChannelId)
        if (existing == null) {
            try {
                val soundUri = Uri.parse(soundUriString)
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()

                val channel = NotificationChannel(
                    customChannelId,
                    "Custom Reminder Sound ($cleanHash)",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    setSound(soundUri, audioAttributes)
                    enableVibration(true)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
            } catch (e: Exception) {
                return CHANNEL_REMINDER_ID
            }
        }
        return customChannelId
    }

    fun showAlarmNotification(
        context: Context,
        alarmId: Long,
        title: String,
        message: String,
        isWakeUpAlarm: Boolean
    ) {
        val ringingIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            putExtra("EXTRA_ALARM_ID", alarmId)
            putExtra("EXTRA_ALARM_TITLE", title)
            putExtra("EXTRA_ALARM_MESSAGE", message)
            putExtra("EXTRA_IS_WAKEUP", isWakeUpAlarm)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            ringingIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = Intent(context, AlarmRingingActivity::class.java).apply {
            putExtra("EXTRA_ALARM_ID", alarmId)
            putExtra("EXTRA_ACTION_DISMISS", true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val dismissPendingIntent = PendingIntent.getActivity(
            context,
            (alarmId + 200000).toInt(),
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ALARM_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title.ifBlank { "CODEXRA Alarm" })
            .setContentText(if (isWakeUpAlarm) "Wake-Up Mission Alarm Ringing! Solve to dismiss." else message.ifBlank { "Alarm is ringing" })
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)

        if (!isWakeUpAlarm) {
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
        }

        try {
            NotificationManagerCompat.from(context).notify(alarmId.toInt(), builder.build())
        } catch (e: SecurityException) {
            // Missing notification permission
        }
    }

    fun showCustomReminderNotification(
        context: Context,
        reminderId: Long,
        title: String,
        message: String,
        soundUri: String? = null
    ) {
        val channelId = getOrCreateSoundChannel(context, soundUri)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            (reminderId + 300000).toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title.ifBlank { "Reminder" })
            .setContentText(message.ifBlank { "Scheduled reminder" })
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.ifBlank { "Scheduled reminder" }))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify((reminderId + 500000).toInt(), builder.build())
        } catch (e: SecurityException) {
            // Missing notification permission
        }
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(notificationId)
    }
}
