package com.example.data.repository

import com.example.data.db.AlarmDao
import com.example.data.db.FocusDao
import com.example.data.db.HabitDao
import com.example.data.db.HistoryDao
import com.example.data.db.ReminderDao
import com.example.data.db.RoutineDao
import com.example.data.db.TaskDao
import com.example.data.model.AlarmEntity
import com.example.data.model.CustomReminderEntity
import com.example.data.model.FocusSessionEntity
import com.example.data.model.HabitEntity
import com.example.data.model.HabitLogEntity
import com.example.data.model.HistoryRecordEntity
import com.example.data.model.RoutineEntity
import com.example.data.model.RoutineLogEntity
import com.example.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class CodexraRepository(
    private val alarmDao: AlarmDao,
    private val reminderDao: ReminderDao,
    private val routineDao: RoutineDao,
    private val taskDao: TaskDao,
    private val historyDao: HistoryDao,
    private val focusDao: FocusDao,
    private val habitDao: HabitDao
) {
    // Alarms
    val allAlarmsFlow: Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()
    suspend fun getAlarmById(id: Long): AlarmEntity? = alarmDao.getAlarmById(id)
    suspend fun insertAlarm(alarm: AlarmEntity): Long = alarmDao.insertAlarm(alarm)
    suspend fun updateAlarm(alarm: AlarmEntity) = alarmDao.updateAlarm(alarm)
    suspend fun deleteAlarm(alarm: AlarmEntity) = alarmDao.deleteAlarm(alarm)
    suspend fun deleteAlarmById(id: Long) = alarmDao.deleteAlarmById(id)
    suspend fun setAlarmEnabled(id: Long, isEnabled: Boolean) = alarmDao.setAlarmEnabled(id, isEnabled)

    // Reminders
    val allRemindersFlow: Flow<List<CustomReminderEntity>> = reminderDao.getAllReminders()
    suspend fun getReminderById(id: Long): CustomReminderEntity? = reminderDao.getReminderById(id)
    suspend fun insertReminder(reminder: CustomReminderEntity): Long = reminderDao.insertReminder(reminder)
    suspend fun updateReminder(reminder: CustomReminderEntity) = reminderDao.updateReminder(reminder)
    suspend fun deleteReminder(reminder: CustomReminderEntity) = reminderDao.deleteReminder(reminder)
    suspend fun deleteReminderById(id: Long) = reminderDao.deleteReminderById(id)
    suspend fun setReminderEnabled(id: Long, isEnabled: Boolean) = reminderDao.setReminderEnabled(id, isEnabled)

    // Routines
    val allRoutinesFlow: Flow<List<RoutineEntity>> = routineDao.getAllRoutines()
    suspend fun getRoutineById(id: Long): RoutineEntity? = routineDao.getRoutineById(id)
    suspend fun insertRoutine(routine: RoutineEntity): Long = routineDao.insertRoutine(routine)
    suspend fun updateRoutine(routine: RoutineEntity) = routineDao.updateRoutine(routine)
    suspend fun deleteRoutine(routine: RoutineEntity) = routineDao.deleteRoutine(routine)
    suspend fun deleteRoutineById(id: Long) = routineDao.deleteRoutineById(id)

    fun getRoutineLogsForDay(epochDay: Long): Flow<List<RoutineLogEntity>> = routineDao.getLogsForDay(epochDay)
    suspend fun setRoutineCompletion(routineId: Long, epochDay: Long, completed: Boolean, skipped: Boolean = false) {
        if (!completed && !skipped) {
            routineDao.clearLogForDay(routineId, epochDay)
        } else {
            routineDao.insertOrUpdateLog(
                RoutineLogEntity(
                    routineId = routineId,
                    dateEpochDay = epochDay,
                    isCompleted = completed,
                    isSkipped = skipped,
                    completedAt = System.currentTimeMillis()
                )
            )
        }
    }
    val allRoutineLogsFlow: Flow<List<RoutineLogEntity>> = routineDao.getAllLogs()

    // User Habits
    val allHabitsFlow: Flow<List<HabitEntity>> = habitDao.getAllActiveHabits()
    fun getHabitLogsForDay(epochDay: Long): Flow<List<HabitLogEntity>> = habitDao.getHabitLogsForDay(epochDay)
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>> = habitDao.getLogsForHabit(habitId)
    suspend fun getHabitById(id: Long): HabitEntity? = habitDao.getHabitById(id)
    suspend fun insertHabit(habit: HabitEntity): Long = habitDao.insertHabit(habit)
    suspend fun updateHabit(habit: HabitEntity) = habitDao.updateHabit(habit)
    suspend fun deleteHabit(habit: HabitEntity) = habitDao.deleteHabit(habit)
    suspend fun deleteHabitById(id: Long) = habitDao.deleteHabitById(id)
    suspend fun setHabitCompletion(habitId: Long, epochDay: Long, isCompleted: Boolean) {
        if (!isCompleted) {
            habitDao.deleteHabitLogForDay(habitId, epochDay)
        } else {
            habitDao.insertOrUpdateHabitLog(
                HabitLogEntity(
                    habitId = habitId,
                    dateEpochDay = epochDay,
                    completedCount = 1,
                    isCompleted = true,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
    suspend fun updateHabitStreak(habitId: Long, currentStreak: Int, bestStreak: Int) {
        habitDao.updateHabitStreak(habitId, currentStreak, bestStreak)
    }

    // Tasks
    val allTasksFlow: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)
    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)
    suspend fun updateTaskStatus(id: Long, status: String) {
        val completedAt = if (status == "COMPLETED") System.currentTimeMillis() else null
        taskDao.updateTaskStatus(id, status, completedAt)
    }

    // History
    val allHistoryFlow: Flow<List<HistoryRecordEntity>> = historyDao.getAllHistory()
    suspend fun recordHistory(type: String, title: String, details: String = "", referenceId: Long? = null, isSuccess: Boolean = true) {
        historyDao.insertRecord(
            HistoryRecordEntity(
                type = type,
                referenceId = referenceId,
                title = title,
                details = details,
                timestamp = System.currentTimeMillis(),
                isSuccess = isSuccess
            )
        )
    }

    // Focus Sessions
    val allFocusSessionsFlow: Flow<List<FocusSessionEntity>> = focusDao.getAllSessions()
    suspend fun insertFocusSession(session: FocusSessionEntity): Long = focusDao.insertSession(session)

    // Clear all for restore / reset
    suspend fun clearDatabaseForRestore() {
        alarmDao.clearAll()
        reminderDao.clearAll()
        routineDao.clearAll()
        taskDao.clearAll()
        historyDao.clearAll()
        focusDao.clearAll()
        habitDao.clearAllHabits()
        habitDao.clearAllHabitLogs()
    }
}
