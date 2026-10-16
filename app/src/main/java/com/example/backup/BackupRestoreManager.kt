package com.example.backup

import android.content.Context
import android.net.Uri
import com.example.CodexraApplication
import com.example.data.model.AlarmEntity
import com.example.data.model.CustomReminderEntity
import com.example.data.model.FocusSessionEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HistoryRecordEntity
import com.example.data.model.RoutineEntity
import com.example.data.model.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class BackupPreview(
    val version: Int,
    val timestamp: Long,
    val alarmsCount: Int,
    val remindersCount: Int,
    val routinesCount: Int,
    val tasksCount: Int,
    val historyCount: Int,
    val focusCount: Int,
    val habitsCount: Int = 0
)

object BackupRestoreManager {

    private const val BACKUP_VERSION = 1
    private const val BACKUP_MAGIC = "CODEXRA_BACKUP_V1"

    suspend fun exportBackup(context: Context, destinationUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val app = context.applicationContext as CodexraApplication
            val repo = app.repository

            val alarms = repo.allAlarmsFlow.first()
            val reminders = repo.allRemindersFlow.first()
            val routines = repo.allRoutinesFlow.first()
            val tasks = repo.allTasksFlow.first()
            val history = repo.allHistoryFlow.first()
            val focusSessions = repo.allFocusSessionsFlow.first()

            val habits = repo.allHabitsFlow.first()

            val rootJson = JSONObject().apply {
                put("magic", BACKUP_MAGIC)
                put("version", BACKUP_VERSION)
                put("exportTimestamp", System.currentTimeMillis())

                // Habits
                val habitsArray = JSONArray()
                for (h in habits) {
                    val obj = JSONObject().apply {
                        put("title", h.title)
                        put("description", h.description)
                        put("category", h.category)
                        put("frequencyType", h.frequencyType)
                        put("targetPerDay", h.targetPerDay)
                        put("unit", h.unit)
                        put("currentStreak", h.currentStreak)
                        put("bestStreak", h.bestStreak)
                    }
                    habitsArray.put(obj)
                }
                put("habits", habitsArray)

                // Alarms
                val alarmsArray = JSONArray()
                for (a in alarms) {
                    val obj = JSONObject().apply {
                        put("title", a.title)
                        put("message", a.message)
                        put("hour", a.hour)
                        put("minute", a.minute)
                        put("second", a.second)
                        put("isEnabled", a.isEnabled)
                        put("isRecurring", a.isRecurring)
                        put("daysOfWeek", a.daysOfWeek)
                        put("startDate", a.startDate ?: JSONObject.NULL)
                        put("endDate", a.endDate ?: JSONObject.NULL)
                        put("ringtoneUri", a.ringtoneUri)
                        put("ringtoneTitle", a.ringtoneTitle)
                        put("isVibrationEnabled", a.isVibrationEnabled)
                        put("isGradualVolume", a.isGradualVolume)
                        put("snoozeDurationMinutes", a.snoozeDurationMinutes)
                        put("isWakeUpAlarm", a.isWakeUpAlarm)
                        put("wakeUpDifficulty", a.wakeUpDifficulty)
                        put("wakeUpMissionType", a.wakeUpMissionType)
                        put("voiceAnnouncementMode", a.voiceAnnouncementMode)
                    }
                    alarmsArray.put(obj)
                }
                put("alarms", alarmsArray)

                // Reminders
                val remindersArray = JSONArray()
                for (r in reminders) {
                    val obj = JSONObject().apply {
                        put("title", r.title)
                        put("message", r.message)
                        put("targetTimestamp", r.targetTimestamp)
                        put("hour", r.hour)
                        put("minute", r.minute)
                        put("repeatType", r.repeatType)
                        put("daysOfWeek", r.daysOfWeek)
                        put("startDate", r.startDate ?: JSONObject.NULL)
                        put("endDate", r.endDate ?: JSONObject.NULL)
                        put("soundUri", r.soundUri)
                        put("soundTitle", r.soundTitle)
                        put("isVibrationEnabled", r.isVibrationEnabled)
                        put("isEnabled", r.isEnabled)
                        put("categoryTag", r.categoryTag)
                    }
                    remindersArray.put(obj)
                }
                put("reminders", remindersArray)

                // Routines
                val routinesArray = JSONArray()
                for (rt in routines) {
                    val obj = JSONObject().apply {
                        put("name", rt.name)
                        put("description", rt.description)
                        put("hour", rt.hour)
                        put("minute", rt.minute)
                        put("repeatPattern", rt.repeatPattern)
                        put("daysOfWeek", rt.daysOfWeek)
                        put("category", rt.category)
                        put("isEnabled", rt.isEnabled)
                        put("hasLinkedNotification", rt.hasLinkedNotification)
                    }
                    routinesArray.put(obj)
                }
                put("routines", routinesArray)

                // Tasks
                val tasksArray = JSONArray()
                for (t in tasks) {
                    val obj = JSONObject().apply {
                        put("title", t.title)
                        put("description", t.description)
                        put("dueDate", t.dueDate)
                        put("dueHour", t.dueHour ?: JSONObject.NULL)
                        put("dueMinute", t.dueMinute ?: JSONObject.NULL)
                        put("priority", t.priority)
                        put("category", t.category)
                        put("isRecurring", t.isRecurring)
                        put("recurrenceDays", t.recurrenceDays)
                        put("status", t.status)
                    }
                    tasksArray.put(obj)
                }
                put("tasks", tasksArray)

                // History
                val historyArray = JSONArray()
                for (h in history) {
                    val obj = JSONObject().apply {
                        put("type", h.type)
                        put("title", h.title)
                        put("details", h.details)
                        put("timestamp", h.timestamp)
                        put("isSuccess", h.isSuccess)
                    }
                    historyArray.put(obj)
                }
                put("history", historyArray)

                // Focus Sessions
                val focusArray = JSONArray()
                for (f in focusSessions) {
                    val obj = JSONObject().apply {
                        put("durationMinutes", f.durationMinutes)
                        put("breakMinutes", f.breakMinutes)
                        put("completedCycles", f.completedCycles)
                        put("timestamp", f.timestamp)
                        put("tag", f.tag)
                    }
                    focusArray.put(obj)
                }
                put("focusSessions", focusArray)
            }

            context.contentResolver.openOutputStream(destinationUri)?.use { outStream ->
                OutputStreamWriter(outStream).use { writer ->
                    writer.write(rootJson.toString(2))
                }
            }
            Result.success(alarms.size + reminders.size + routines.size + tasks.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun parseAndValidateBackup(context: Context, sourceUri: Uri): Result<Pair<JSONObject, BackupPreview>> = withContext(Dispatchers.IO) {
        try {
            val content = StringBuilder()
            context.contentResolver.openInputStream(sourceUri)?.use { inStream ->
                BufferedReader(InputStreamReader(inStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        content.append(line)
                    }
                }
            }
            val json = JSONObject(content.toString())
            if (!json.has("magic") || json.getString("magic") != BACKUP_MAGIC) {
                return@withContext Result.failure(IllegalArgumentException("Invalid or unrecognized CODEXRA backup file"))
            }

            val version = json.optInt("version", 1)
            val timestamp = json.optLong("exportTimestamp", System.currentTimeMillis())
            val alarmsArr = json.optJSONArray("alarms") ?: JSONArray()
            val remindersArr = json.optJSONArray("reminders") ?: JSONArray()
            val routinesArr = json.optJSONArray("routines") ?: JSONArray()
            val tasksArr = json.optJSONArray("tasks") ?: JSONArray()
            val historyArr = json.optJSONArray("history") ?: JSONArray()
            val focusArr = json.optJSONArray("focusSessions") ?: JSONArray()
            val habitsArr = json.optJSONArray("habits") ?: JSONArray()

            val preview = BackupPreview(
                version = version,
                timestamp = timestamp,
                alarmsCount = alarmsArr.length(),
                remindersCount = remindersArr.length(),
                routinesCount = routinesArr.length(),
                tasksCount = tasksArr.length(),
                historyCount = historyArr.length(),
                focusCount = focusArr.length(),
                habitsCount = habitsArr.length()
            )
            Result.success(Pair(json, preview))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreBackup(context: Context, json: JSONObject, isReplaceMode: Boolean): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val app = context.applicationContext as CodexraApplication
            val repo = app.repository

            if (isReplaceMode) {
                repo.clearDatabaseForRestore()
            }

            var restoredCount = 0

            // Habits
            val habitsArr = json.optJSONArray("habits")
            if (habitsArr != null) {
                for (i in 0 until habitsArr.length()) {
                    val item = habitsArr.getJSONObject(i)
                    val habit = HabitEntity(
                        title = item.optString("title", "Habit"),
                        description = item.optString("description", ""),
                        category = item.optString("category", "DISCIPLINE"),
                        frequencyType = item.optString("frequencyType", "DAILY"),
                        targetPerDay = item.optInt("targetPerDay", 1),
                        unit = item.optString("unit", "times"),
                        currentStreak = item.optInt("currentStreak", 0),
                        bestStreak = item.optInt("bestStreak", 0)
                    )
                    repo.insertHabit(habit)
                    restoredCount++
                }
            }

            // Alarms
            val alarmsArr = json.optJSONArray("alarms")
            if (alarmsArr != null) {
                for (i in 0 until alarmsArr.length()) {
                    val item = alarmsArr.getJSONObject(i)
                    val alarm = AlarmEntity(
                        title = item.optString("title", "Alarm"),
                        message = item.optString("message", ""),
                        hour = item.optInt("hour", 7),
                        minute = item.optInt("minute", 0),
                        second = item.optInt("second", 0),
                        isEnabled = item.optBoolean("isEnabled", true),
                        isRecurring = item.optBoolean("isRecurring", false),
                        daysOfWeek = item.optString("daysOfWeek", ""),
                        startDate = if (item.isNull("startDate")) null else item.optLong("startDate"),
                        endDate = if (item.isNull("endDate")) null else item.optLong("endDate"),
                        ringtoneUri = item.optString("ringtoneUri", ""),
                        ringtoneTitle = item.optString("ringtoneTitle", "Default Alarm Tone"),
                        isVibrationEnabled = item.optBoolean("isVibrationEnabled", true),
                        isGradualVolume = item.optBoolean("isGradualVolume", false),
                        snoozeDurationMinutes = item.optInt("snoozeDurationMinutes", 5),
                        isWakeUpAlarm = item.optBoolean("isWakeUpAlarm", false),
                        wakeUpDifficulty = item.optString("wakeUpDifficulty", "MEDIUM"),
                        wakeUpMissionType = item.optString("wakeUpMissionType", "MATHS"),
                        voiceAnnouncementMode = item.optString("voiceAnnouncementMode", "OFF")
                    )
                    repo.insertAlarm(alarm)
                    restoredCount++
                }
            }

            // Reminders
            val remindersArr = json.optJSONArray("reminders")
            if (remindersArr != null) {
                for (i in 0 until remindersArr.length()) {
                    val item = remindersArr.getJSONObject(i)
                    val reminder = CustomReminderEntity(
                        title = item.optString("title", "Reminder"),
                        message = item.optString("message", ""),
                        targetTimestamp = item.optLong("targetTimestamp", 0L),
                        hour = item.optInt("hour", 8),
                        minute = item.optInt("minute", 0),
                        repeatType = item.optString("repeatType", "NONE"),
                        daysOfWeek = item.optString("daysOfWeek", ""),
                        startDate = if (item.isNull("startDate")) null else item.optLong("startDate"),
                        endDate = if (item.isNull("endDate")) null else item.optLong("endDate"),
                        soundUri = item.optString("soundUri", ""),
                        soundTitle = item.optString("soundTitle", "Default Sound"),
                        isVibrationEnabled = item.optBoolean("isVibrationEnabled", true),
                        isEnabled = item.optBoolean("isEnabled", true),
                        categoryTag = item.optString("categoryTag", "CUSTOM")
                    )
                    repo.insertReminder(reminder)
                    restoredCount++
                }
            }

            // Routines
            val routinesArr = json.optJSONArray("routines")
            if (routinesArr != null) {
                for (i in 0 until routinesArr.length()) {
                    val item = routinesArr.getJSONObject(i)
                    val routine = RoutineEntity(
                        name = item.optString("name", "Routine"),
                        description = item.optString("description", ""),
                        hour = item.optInt("hour", 9),
                        minute = item.optInt("minute", 0),
                        repeatPattern = item.optString("repeatPattern", "DAILY"),
                        daysOfWeek = item.optString("daysOfWeek", "1,2,3,4,5,6,7"),
                        category = item.optString("category", "CUSTOM"),
                        isEnabled = item.optBoolean("isEnabled", true),
                        hasLinkedNotification = item.optBoolean("hasLinkedNotification", false)
                    )
                    repo.insertRoutine(routine)
                    restoredCount++
                }
            }

            // Tasks
            val tasksArr = json.optJSONArray("tasks")
            if (tasksArr != null) {
                for (i in 0 until tasksArr.length()) {
                    val item = tasksArr.getJSONObject(i)
                    val task = TaskEntity(
                        title = item.optString("title", "Task"),
                        description = item.optString("description", ""),
                        dueDate = item.optLong("dueDate", System.currentTimeMillis()),
                        dueHour = if (item.isNull("dueHour")) null else item.optInt("dueHour"),
                        dueMinute = if (item.isNull("dueMinute")) null else item.optInt("dueMinute"),
                        priority = item.optString("priority", "MEDIUM"),
                        category = item.optString("category", "GENERAL"),
                        isRecurring = item.optBoolean("isRecurring", false),
                        recurrenceDays = item.optInt("recurrenceDays", 0),
                        status = item.optString("status", "PENDING")
                    )
                    repo.insertTask(task)
                    restoredCount++
                }
            }

            // Reschedule active alarms and reminders
            app.rescheduleAllActive()
            Result.success(restoredCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
