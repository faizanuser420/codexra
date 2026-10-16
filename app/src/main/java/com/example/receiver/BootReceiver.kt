package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.CodexraApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("BootReceiver", "Received broadcast action: $action")
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val app = context.applicationContext as? CodexraApplication
            app?.let { application ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        application.rescheduleAllActive()
                        Log.d("BootReceiver", "Successfully rescheduled all active alarms and reminders")
                    } catch (e: Exception) {
                        Log.e("BootReceiver", "Failed to reschedule items", e)
                    }
                }
            }
        }
    }
}
