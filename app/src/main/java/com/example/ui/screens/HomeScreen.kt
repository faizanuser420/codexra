package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AlarmEntity
import com.example.scheduling.AlarmScheduler
import com.example.ui.CodexraViewModel
import com.example.ui.theme.CodexraAmberAccent
import com.example.ui.theme.CodexraDarkSurface
import com.example.ui.theme.CodexraDarkSurfaceVariant
import com.example.ui.theme.CodexraPrimaryCyan
import com.example.ui.theme.CodexraSuccessGreen
import com.example.ui.theme.CodexraTextSecondary
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: CodexraViewModel,
    onNavigateTo: (String) -> Unit
) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    val todayRoutineLogs by viewModel.todayRoutineLogs.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val disciplineScore by viewModel.disciplineScore.collectAsStateWithLifecycle()

    var showScoreInfoDialog by remember { mutableStateOf(false) }

    // Live clock
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(1000)
        }
    }

    // Next scheduled alarm calculation
    val enabledAlarms = alarms.filter { it.isEnabled }
    val nextAlarmInfo = remember(enabledAlarms, currentTime) {
        var minTrigger: Long? = null
        var nextAlarm: AlarmEntity? = null
        for (alarm in enabledAlarms) {
            val trigger = AlarmScheduler.calculateNextTriggerMillis(alarm, currentTime)
            if (trigger != null && (minTrigger == null || trigger < minTrigger)) {
                minTrigger = trigger
                nextAlarm = alarm
            }
        }
        if (minTrigger != null && nextAlarm != null) {
            val nowMillis = System.currentTimeMillis()
            val diffSec = (minTrigger - nowMillis) / 1000
            val hours = diffSec / 3600
            val minutes = (diffSec % 3600) / 60
            Pair(nextAlarm, "in ${hours}h ${minutes}m")
        } else {
            null
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Digital Clock & Date Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_clock_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CodexraDarkSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CODEXRA ALARM",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color = CodexraPrimaryCyan
                        )
                    )
                    Text(
                        text = "Train Your Mind. Master Your Decisions.",
                        style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Big digital clock
                    Text(
                        text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Text(
                        text = currentTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())),
                        style = MaterialTheme.typography.titleSmall.copy(color = CodexraAmberAccent),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // 2. Next Scheduled Alarm Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_next_alarm_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (nextAlarmInfo != null) Color(0xFF142033) else CodexraDarkSurface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (nextAlarmInfo?.first?.isWakeUpAlarm == true)
                                    CodexraAmberAccent.copy(alpha = 0.2f)
                                else
                                    CodexraPrimaryCyan.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (nextAlarmInfo?.first?.isWakeUpAlarm == true) Icons.Default.Security else Icons.Default.Alarm,
                            contentDescription = null,
                            tint = if (nextAlarmInfo?.first?.isWakeUpAlarm == true) CodexraAmberAccent else CodexraPrimaryCyan,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (nextAlarmInfo != null) "Next Alarm: ${nextAlarmInfo.second}" else "No Alarms Scheduled",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        if (nextAlarmInfo != null) {
                            val alarm = nextAlarmInfo.first
                            val formattedTime = String.format("%02d:%02d", alarm.hour, alarm.minute)
                            Text(
                                text = "$formattedTime • ${alarm.title} ${if (alarm.isWakeUpAlarm) "[Wake-Up Mission]" else ""}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (alarm.isWakeUpAlarm) CodexraAmberAccent else CodexraTextSecondary
                                )
                            )
                        } else {
                            Text(
                                text = "Tap + to set a precision alarm",
                                style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary)
                            )
                        }
                    }

                    IconButton(
                        onClick = { onNavigateTo("ALARMS") },
                        modifier = Modifier.testTag("home_view_alarms_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Alarm",
                            tint = CodexraPrimaryCyan
                        )
                    }
                }
            }
        }

        // 3. Daily Discipline Score Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showScoreInfoDialog = true }
                    .testTag("home_discipline_score_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CodexraDarkSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DAILY DISCIPLINE SCORE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = CodexraPrimaryCyan
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Formula details",
                                tint = CodexraTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${disciplineScore.completedItems} of ${disciplineScore.totalDueItems} completed today",
                            style = MaterialTheme.typography.bodyMedium.copy(color = CodexraTextSecondary)
                        )
                        Text(
                            text = "Current Streak: ${disciplineScore.currentStreak} days",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = CodexraAmberAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    // Score Circle
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .border(3.dp, if (disciplineScore.percentage >= 80) CodexraSuccessGreen else CodexraAmberAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${disciplineScore.percentage}%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }

        // 4. Quick Action Buttons
        item {
            Text(
                text = "QUICK ACTIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = CodexraTextSecondary
                ),
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionButton(
                    title = "Alarm",
                    icon = Icons.Default.Alarm,
                    color = CodexraPrimaryCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo("ALARMS") }
                )
                QuickActionButton(
                    title = "Reminder",
                    icon = Icons.Default.Notifications,
                    color = CodexraAmberAccent,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo("NOTIFICATIONS") }
                )
                QuickActionButton(
                    title = "Routine",
                    icon = Icons.Default.Repeat,
                    color = CodexraSuccessGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo("ROUTINES") }
                )
                QuickActionButton(
                    title = "Task",
                    icon = Icons.Default.TaskAlt,
                    color = Color(0xFFA855F7),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo("TASKS") }
                )
            }
        }

        // 5. Today's Routines Quick View
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TODAY'S ROUTINES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = CodexraTextSecondary
                    ),
                    modifier = Modifier.padding(start = 4.dp)
                )
                TextButton(onClick = { onNavigateTo("ROUTINES") }) {
                    Text("View All", color = CodexraPrimaryCyan, fontSize = 12.sp)
                }
            }
        }

        if (routines.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CodexraDarkSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No daily routines created yet. Add prayer, study, or exercise routines in the Routines section.",
                        color = CodexraTextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(routines.take(4)) { routine ->
                val log = todayRoutineLogs.firstOrNull { it.routineId == routine.id }
                val isCompleted = log?.isCompleted == true

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CodexraDarkSurface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.setRoutineCompletion(routine.id, !isCompleted) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Toggle completion",
                                tint = if (isCompleted) CodexraSuccessGreen else CodexraTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = routine.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isCompleted) CodexraTextSecondary else Color.White
                                )
                            )
                            Text(
                                text = "${String.format("%02d:%02d", routine.hour, routine.minute)} • ${routine.category}",
                                style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary)
                            )
                        }
                    }
                }
            }
        }

        // 6. Focus Timer Quick Access
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTo("FOCUS") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E172E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFA855F7).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Focus Timer",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Pomodoro intervals for study and work without missions",
                            style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary)
                        )
                    }
                }
            }
        }
    }

    if (showScoreInfoDialog) {
        AlertDialog(
            onDismissRequest = { showScoreInfoDialog = false },
            title = {
                Text(
                    text = "Discipline Score Calculation",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = disciplineScore.explanation,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "CODEXRA calculates your score transparently using only items actually due today. Future tasks or deleted items are never counted as failures.",
                        style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showScoreInfoDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CodexraPrimaryCyan)
                ) {
                    Text("Got It", color = Color.Black)
                }
            },
            containerColor = CodexraDarkSurface
        )
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = CodexraDarkSurface,
        modifier = modifier.height(72.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
        }
    }
}
