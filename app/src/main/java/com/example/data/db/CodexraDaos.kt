package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmById(id: Long): AlarmEntity?

    @Query("SELECT * FROM alarms WHERE isEnabled = 1 ORDER BY hour ASC, minute ASC")
    suspend fun getEnabledAlarms(): List<AlarmEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity): Long

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Delete
    suspend fun deleteAlarm(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarmById(id: Long)

    @Query("UPDATE alarms SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setAlarmEnabled(id: Long, isEnabled: Boolean)

    @Query("DELETE FROM alarms")
    suspend fun clearAll()
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM custom_reminders ORDER BY hour ASC, minute ASC")
    fun getAllReminders(): Flow<List<CustomReminderEntity>>

    @Query("SELECT * FROM custom_reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): CustomReminderEntity?

    @Query("SELECT * FROM custom_reminders WHERE isEnabled = 1")
    suspend fun getEnabledReminders(): List<CustomReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: CustomReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: CustomReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: CustomReminderEntity)

    @Query("DELETE FROM custom_reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("UPDATE custom_reminders SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setReminderEnabled(id: Long, isEnabled: Boolean)

    @Query("DELETE FROM custom_reminders")
    suspend fun clearAll()
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY hour ASC, minute ASC")
    fun getAllRoutines(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE id = :id")
    suspend fun getRoutineById(id: Long): RoutineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEntity): Long

    @Update
    suspend fun updateRoutine(routine: RoutineEntity)

    @Delete
    suspend fun deleteRoutine(routine: RoutineEntity)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutineById(id: Long)

    @Query("SELECT * FROM routine_logs WHERE dateEpochDay = :dateEpochDay")
    fun getLogsForDay(dateEpochDay: Long): Flow<List<RoutineLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: RoutineLogEntity)

    @Query("DELETE FROM routine_logs WHERE routineId = :routineId AND dateEpochDay = :dateEpochDay")
    suspend fun clearLogForDay(routineId: Long, dateEpochDay: Long)

    @Query("SELECT * FROM routine_logs ORDER BY dateEpochDay DESC")
    fun getAllLogs(): Flow<List<RoutineLogEntity>>

    @Query("DELETE FROM routines")
    suspend fun clearAll()
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC, priority DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE tasks SET status = :status, completedAt = :completedAt WHERE id = :id")
    suspend fun updateTaskStatus(id: Long, status: String, completedAt: Long?)

    @Query("DELETE FROM tasks")
    suspend fun clearAll()
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_records ORDER BY timestamp DESC LIMIT 200")
    fun getAllHistory(): Flow<List<HistoryRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: HistoryRecordEntity): Long

    @Query("DELETE FROM history_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM history_records")
    suspend fun clearAll()
}

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity): Long

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAll()
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM user_habits WHERE isArchived = 0 ORDER BY createdAt ASC")
    fun getAllActiveHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM user_habits ORDER BY createdAt ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM user_habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("DELETE FROM user_habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long)

    @Query("UPDATE user_habits SET currentStreak = :currentStreak, bestStreak = :bestStreak WHERE id = :id")
    suspend fun updateHabitStreak(id: Long, currentStreak: Int, bestStreak: Int)

    @Query("SELECT * FROM user_habit_logs WHERE dateEpochDay = :dateEpochDay")
    fun getHabitLogsForDay(dateEpochDay: Long): Flow<List<HabitLogEntity>>

    @Query("SELECT * FROM user_habit_logs WHERE habitId = :habitId ORDER BY dateEpochDay DESC")
    fun getLogsForHabit(habitId: Long): Flow<List<HabitLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHabitLog(log: HabitLogEntity)

    @Query("DELETE FROM user_habit_logs WHERE habitId = :habitId AND dateEpochDay = :dateEpochDay")
    suspend fun deleteHabitLogForDay(habitId: Long, dateEpochDay: Long)

    @Query("DELETE FROM user_habits")
    suspend fun clearAllHabits()

    @Query("DELETE FROM user_habit_logs")
    suspend fun clearAllHabitLogs()
}

