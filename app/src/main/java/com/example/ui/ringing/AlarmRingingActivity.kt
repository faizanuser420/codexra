package com.example.ui.ringing

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CodexraApplication
import com.example.mission.MissionQuestion
import com.example.mission.WakeUpMissionGenerator
import kotlinx.coroutines.CoroutineScope
import com.example.scheduling.AlarmScheduler
import com.example.scheduling.NotificationHelper
import com.example.sound.AlarmRingtonePlayer
import com.example.sound.VoiceAnnouncer
import com.example.ui.theme.CodexraAmberAccent
import com.example.ui.theme.CodexraCrimsonAlert
import com.example.ui.theme.CodexraDarkBackground
import com.example.ui.theme.CodexraDarkSurface
import com.example.ui.theme.CodexraPrimaryCyan
import com.example.ui.theme.CodexraSuccessGreen
import com.example.ui.theme.CodexraTextSecondary
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AlarmRingingActivity : ComponentActivity() {

    private var voiceAnnouncer: VoiceAnnouncer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Turn screen on and show over lockscreen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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

        // Handle immediate dismiss from notification action
        if (intent.getBooleanExtra("EXTRA_ACTION_DISMISS", false)) {
            AlarmRingtonePlayer.stop()
            NotificationHelper.cancelNotification(this, alarmId.toInt())
            finish()
            return
        }

        // Start ringing sound & vibration
        AlarmRingtonePlayer.play(this, ringtoneUri, isVibrate, gradualVolume)

        // Initialize voice announcer if requested
        if (voiceMode != "OFF") {
            voiceAnnouncer = VoiceAnnouncer(this) { ready ->
                if (ready) {
                    val speechText = if (voiceMode == "VOICE_ONLY") {
                        AlarmRingtonePlayer.stop()
                        "Attention. Alarm: $title. $message"
                    } else {
                        "Attention. Alarm: $title."
                    }
                    voiceAnnouncer?.speak(speechText)
                }
            }
        }

        setContent {
            MyApplicationTheme(darkTheme = true) {
                AlarmRingingScreen(
                    alarmId = alarmId,
                    title = title,
                    message = message,
                    isWakeUpAlarm = isWakeUp,
                    difficulty = difficulty,
                    missionType = missionType,
                    snoozeMinutes = snoozeMinutes,
                    onDismiss = {
                        stopAllAndFinish(alarmId, isEmergency = false)
                    },
                    onEmergencyDismiss = {
                        stopAllAndFinish(alarmId, isEmergency = true)
                    },
                    onSnooze = {
                        handleSnooze(alarmId, title, message, isWakeUp, snoozeMinutes)
                    }
                )
            }
        }
    }

    private fun handleSnooze(alarmId: Long, title: String, message: String, isWakeUp: Boolean, snoozeMinutes: Int) {
        val app = application as? CodexraApplication
        app?.let {
            CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                it.repository.recordHistory(
                    type = "ALARM_SNOOZED",
                    title = title,
                    details = "Snoozed for $snoozeMinutes minutes",
                    referenceId = alarmId,
                    isSuccess = true
                )
                // Schedule snooze alarm
                val now = java.time.LocalDateTime.now().plusMinutes(snoozeMinutes.toLong())
                val snoozeAlarm = com.example.data.model.AlarmEntity(
                    id = if (alarmId > 0) alarmId else System.currentTimeMillis() % 100000,
                    title = "$title (Snoozed)",
                    message = message,
                    hour = now.hour,
                    minute = now.minute,
                    second = now.second,
                    isEnabled = true,
                    isRecurring = false,
                    isWakeUpAlarm = isWakeUp,
                    snoozeDurationMinutes = snoozeMinutes
                )
                AlarmScheduler.scheduleAlarm(this@AlarmRingingActivity, snoozeAlarm)
            }
        }
        stopAllAndFinish(alarmId, isEmergency = false)
    }

    private fun stopAllAndFinish(alarmId: Long, isEmergency: Boolean) {
        AlarmRingtonePlayer.stop()
        voiceAnnouncer?.stop()
        voiceAnnouncer?.shutdown()
        NotificationHelper.cancelNotification(this, alarmId.toInt())

        val app = application as? CodexraApplication
        app?.let {
            CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                val recordType = if (isEmergency) "EMERGENCY_DISMISSED" else "ALARM_DISMISSED"
                it.repository.recordHistory(
                    type = recordType,
                    title = intent.getStringExtra("EXTRA_ALARM_TITLE") ?: "Alarm",
                    details = if (isEmergency) "Bypassed challenge via Emergency Stop" else "Alarm dismissed normally",
                    referenceId = alarmId,
                    isSuccess = !isEmergency
                )
            }
        }
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        AlarmRingtonePlayer.stop()
        voiceAnnouncer?.shutdown()
    }
}

@Composable
fun AlarmRingingScreen(
    alarmId: Long,
    title: String,
    message: String,
    isWakeUpAlarm: Boolean,
    difficulty: String,
    missionType: String,
    snoozeMinutes: Int,
    onDismiss: () -> Unit,
    onEmergencyDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var consecutiveCorrect by remember { mutableIntStateOf(0) }
    var currentQuestion by remember {
        mutableStateOf(WakeUpMissionGenerator.generateQuestion(missionType, difficulty))
    }
    var usedQuestionIds by remember { mutableStateOf(setOf(currentQuestion.id)) }
    var userAnswer by remember { mutableStateOf("") }
    var answerFeedback by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showEmergencyDialog by remember { mutableStateOf(false) }

    val currentTimeString = remember {
        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    fun submitAnswer() {
        if (isSubmitting || userAnswer.isBlank()) return
        isSubmitting = true

        val cleanInput = userAnswer.trim()
        val isCorrect = cleanInput.equals(currentQuestion.expectedAnswer.trim(), ignoreCase = true)

        if (isCorrect) {
            val newCount = consecutiveCorrect + 1
            consecutiveCorrect = newCount
            answerFeedback = "Correct! ($newCount / 3)"
            userAnswer = ""

            if (newCount >= 3) {
                // Mission Completed! Stop alarm automatically
                val app = CodexraApplication.instance
                coroutineScope.launch {
                    app.repository.recordHistory(
                        type = "MISSION_SUCCESS",
                        title = "Wake-Up Mission Completed",
                        details = "3 consecutive correct answers: $difficulty $missionType",
                        referenceId = alarmId,
                        isSuccess = true
                    )
                }
                onDismiss()
                return
            } else {
                // Next question
                val nextQ = WakeUpMissionGenerator.generateQuestion(
                    missionType = missionType,
                    difficulty = difficulty,
                    excludeIds = usedQuestionIds
                )
                usedQuestionIds = usedQuestionIds + nextQ.id
                currentQuestion = nextQ
            }
        } else {
            // Wrong answer -> Reset consecutive counter to 0!
            consecutiveCorrect = 0
            answerFeedback = "Incorrect! Counter reset to 0/3. Try again."
            userAnswer = ""
            // Generate a fresh question
            val nextQ = WakeUpMissionGenerator.generateQuestion(
                missionType = missionType,
                difficulty = difficulty,
                excludeIds = usedQuestionIds
            )
            usedQuestionIds = usedQuestionIds + nextQ.id
            currentQuestion = nextQ
        }
        isSubmitting = false
    }

    Scaffold(
        containerColor = CodexraDarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Alarm Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(if (isWakeUpAlarm) CodexraAmberAccent.copy(alpha = 0.2f) else CodexraPrimaryCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isWakeUpAlarm) Icons.Default.NotificationsActive else Icons.Default.Alarm,
                        contentDescription = "Ringing icon",
                        tint = if (isWakeUpAlarm) CodexraAmberAccent else CodexraPrimaryCyan,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentTimeString,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Text(
                    text = title.ifBlank { "CODEXRA ALARM" },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )

                if (message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium.copy(color = CodexraTextSecondary),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CRITICAL BRANCH: Wake-Up Mission vs Normal Alarm
            if (isWakeUpAlarm) {
                // WAKE-UP MISSION PANEL: 3 Consecutive Correct Answers Needed
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CodexraAmberAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = CodexraDarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CodexraAmberAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WAKE-UP MISSION",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = CodexraAmberAccent
                                )
                            )
                        }

                        Text(
                            text = "Solve 3 consecutive challenges to stop the alarm.",
                            style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        // 3 Consecutive Progress Indicators
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            for (step in 1..3) {
                                val isDone = consecutiveCorrect >= step
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isDone) CodexraSuccessGreen else Color(0xFF243046)
                                        )
                                        .border(
                                            1.dp,
                                            if (isDone) CodexraSuccessGreen else Color(0xFF475569),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDone) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Done",
                                            tint = Color.Black,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "$step",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Challenge Display
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F1523),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = currentQuestion.prompt,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = CodexraPrimaryCyan
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                if (currentQuestion.hint.isNotBlank()) {
                                    Text(
                                        text = currentQuestion.hint,
                                        style = MaterialTheme.typography.labelSmall.copy(color = CodexraTextSecondary),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Input field
                        OutlinedTextField(
                            value = userAnswer,
                            onValueChange = { userAnswer = it },
                            label = { Text("Enter Answer", color = CodexraTextSecondary) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { submitAnswer() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mission_answer_input")
                        )

                        if (answerFeedback != null) {
                            Text(
                                text = answerFeedback!!,
                                color = if (consecutiveCorrect > 0) CodexraSuccessGreen else CodexraCrimsonAlert,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.padding(top = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { submitAnswer() },
                            colors = ButtonDefaults.buttonColors(containerColor = CodexraAmberAccent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("mission_submit_button")
                        ) {
                            Text(
                                text = "Submit Answer",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // NORMAL ALARM PANEL: Instant Dismiss and Snooze (No math questions)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Standard Alarm Ringing",
                        style = MaterialTheme.typography.bodyMedium.copy(color = CodexraTextSecondary),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = CodexraPrimaryCyan),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("normal_alarm_dismiss_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DISMISS ALARM",
                            color = Color.Black,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Actions: Snooze and Emergency Stop
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onSnooze,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("alarm_snooze_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Snooze,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Snooze (${snoozeMinutes}m)")
                    }

                    if (isWakeUpAlarm) {
                        OutlinedButton(
                            onClick = { showEmergencyDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("emergency_stop_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CodexraCrimsonAlert)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = CodexraCrimsonAlert
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Emergency Stop", color = CodexraCrimsonAlert)
                        }
                    }
                }
            }
        }
    }

    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            title = {
                Text(
                    text = "Emergency Stop Alarm?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Emergency dismissal bypasses the 3 consecutive answers challenge. This event will be recorded in your discipline history as an emergency stop.",
                    color = CodexraTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEmergencyDialog = false
                        onEmergencyDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CodexraCrimsonAlert)
                ) {
                    Text("Confirm Emergency Stop", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyDialog = false }) {
                    Text("Cancel", color = CodexraPrimaryCyan)
                }
            },
            containerColor = CodexraDarkSurface
        )
    }
}
