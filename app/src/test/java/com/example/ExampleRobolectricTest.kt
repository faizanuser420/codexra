package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.CodexraDatabase
import com.example.data.model.AlarmEntity
import com.example.data.model.HabitEntity
import com.example.data.model.RoutineEntity
import com.example.mission.WakeUpMissionGenerator
import com.example.scheduling.AlarmScheduler
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDateTime

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CODEXRA ALARM", appName)
    }

    @Test
    fun `wake up mission question generation produces valid non-empty prompt and answer`() {
        val question = WakeUpMissionGenerator.generateQuestion("MATHS", "EASY")
        assertTrue(question.prompt.isNotBlank())
        assertTrue(question.expectedAnswer.isNotBlank())
        assertNotNull(question.expectedAnswer.toIntOrNull())
    }

    @Test
    fun `wake up mission sequences produce valid answers`() {
        val seqQuestion = WakeUpMissionGenerator.generateQuestion("SEQUENCE", "MEDIUM")
        assertTrue(seqQuestion.prompt.contains("___"))
        assertNotNull(seqQuestion.expectedAnswer.toIntOrNull())
    }

    @Test
    fun `alarm scheduler calculates next occurrence for future time`() {
        val testNow = LocalDateTime.of(2026, 10, 15, 6, 0, 0)
        val alarm = AlarmEntity(
            title = "Morning Test",
            hour = 7,
            minute = 30,
            isEnabled = true,
            isRecurring = false
        )
        val trigger = AlarmScheduler.calculateNextTriggerMillis(alarm, testNow)
        assertNotNull(trigger)
    }

    @Test
    fun `room database stores and retrieves alarms routines and habits`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, CodexraDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        // 1. Alarm persistence
        val alarmId = db.alarmDao().insertAlarm(
            AlarmEntity(title = "Room Test Alarm", hour = 6, minute = 30)
        )
        val alarm = db.alarmDao().getAlarmById(alarmId)
        assertNotNull(alarm)
        assertEquals("Room Test Alarm", alarm?.title)

        // 2. Routine persistence
        val routineId = db.routineDao().insertRoutine(
            RoutineEntity(name = "Fajr Prayer", hour = 5, minute = 15, category = "PRAYER")
        )
        val routine = db.routineDao().getRoutineById(routineId)
        assertNotNull(routine)
        assertEquals("Fajr Prayer", routine?.name)

        // 3. User Habit persistence
        val habitId = db.habitDao().insertHabit(
            HabitEntity(title = "Morning Meditation", category = "MINDFULNESS", targetPerDay = 1)
        )
        val habit = db.habitDao().getHabitById(habitId)
        assertNotNull(habit)
        assertEquals("Morning Meditation", habit?.title)

        db.close()
    }

    @Test
    fun `CodexraAlarmDatabase provides a singleton instance via Room databaseBuilder`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val instance1 = com.example.data.db.CodexraAlarmDatabase.getInstance(context)
        val instance2 = com.example.data.db.CodexraAlarmDatabase.getInstance(context)
        assertNotNull(instance1)
        org.junit.Assert.assertSame(instance1, instance2)
    }
}
