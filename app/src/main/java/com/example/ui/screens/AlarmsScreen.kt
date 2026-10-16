package com.example.ui.screens

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AlarmEntity
import com.example.sound.AlarmRingtonePlayer
import com.example.ui.CodexraViewModel
import com.example.ui.theme.CodexraAmberAccent
import com.example.ui.theme.CodexraCrimsonAlert
import com.example.ui.theme.CodexraDarkSurface
import com.example.ui.theme.CodexraDarkSurfaceVariant
import com.example.ui.theme.CodexraPrimaryCyan
import com.example.ui.theme.CodexraSuccessGreen
import com.example.ui.theme.CodexraTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(viewModel: CodexraViewModel) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedAlarmForEdit by remember { mutableStateOf<AlarmEntity?>(null) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var alarmToDelete by remember { mutableStateOf<AlarmEntity?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedAlarmForEdit = null
                    showCreateSheet = true
                },
                containerColor = CodexraPrimaryCyan,
                contentColor = Color.Black,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("add_alarm_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Alarm")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SMART ALARMS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = CodexraPrimaryCyan
                )
            )
            Text(
                text = "Precision schedules & Wake-Up Missions",
                style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (alarms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = CodexraTextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Alarms Created Yet",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Tap the + button below to create your first alarm",
                            style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            onToggle = { viewModel.toggleAlarm(alarm) },
                            onEdit = {
                                selectedAlarmForEdit = alarm
                                showCreateSheet = true
                            },
                            onDuplicate = { viewModel.duplicateAlarm(alarm) },
                            onDelete = { alarmToDelete = alarm }
                        )
                    }
                }
            }
        }
    }

    if (showCreateSheet) {
        AlarmEditSheet(
            initialAlarm = selectedAlarmForEdit,
            onDismiss = { showCreateSheet = false },
            onSave = { alarm ->
                viewModel.saveAlarm(alarm)
                showCreateSheet = false
            }
        )
    }

    if (alarmToDelete != null) {
        AlertDialog(
            onDismissRequest = { alarmToDelete = null },
            title = { Text("Delete Alarm?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${alarmToDelete?.title}'?", color = CodexraTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        alarmToDelete?.let { viewModel.deleteAlarm(it) }
                        alarmToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CodexraCrimsonAlert)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { alarmToDelete = null }) {
                    Text("Cancel", color = CodexraPrimaryCyan)
                }
            },
            containerColor = CodexraDarkSurface
        )
    }
}

@Composable
fun AlarmCard(
    alarm: AlarmEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedTime = String.format("%02d:%02d", alarm.hour, alarm.minute)
    val amPm = if (alarm.hour < 12) "AM" else "PM"
    val hour12 = when {
        alarm.hour == 0 -> 12
        alarm.hour > 12 -> alarm.hour - 12
        else -> alarm.hour
    }
    val formattedTime12 = String.format("%02d:%02d %s", hour12, alarm.minute, amPm)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alarm_card_${alarm.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) CodexraDarkSurface else Color(0xFF0D121D)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (alarm.isEnabled) Color.White else CodexraTextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "($formattedTime12)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = CodexraTextSecondary
                            ),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Text(
                        text = alarm.title.ifBlank { "Alarm" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (alarm.isEnabled) Color.White else CodexraTextSecondary
                        )
                    )
                }

                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = if (alarm.isWakeUpAlarm) CodexraAmberAccent else CodexraPrimaryCyan
                    ),
                    modifier = Modifier.testTag("alarm_toggle_${alarm.id}")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle badges: Wake-Up Mission vs Normal Alarm
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (alarm.isWakeUpAlarm) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CodexraAmberAccent.copy(alpha = 0.15f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = CodexraAmberAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "WAKE-UP MISSION (${alarm.wakeUpDifficulty})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CodexraAmberAccent
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CodexraPrimaryCyan.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "STANDARD ALARM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CodexraPrimaryCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                val recurrenceText = if (alarm.isRecurring) {
                    val days = alarm.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
                    val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
                    if (days.size == 7) "Every day" else days.mapNotNull { if (it in 1..7) dayNames[it - 1] else null }.joinToString(" ")
                } else {
                    "One-time"
                }

                Text(
                    text = "• $recurrenceText",
                    style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary)
                )
            }

            if (alarm.message.isNotBlank()) {
                Text(
                    text = alarm.message,
                    style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Card Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDuplicate, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate",
                        tint = CodexraTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = CodexraPrimaryCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = CodexraCrimsonAlert,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditSheet(
    initialAlarm: AlarmEntity?,
    onDismiss: () -> Unit,
    onSave: (AlarmEntity) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialAlarm?.title ?: "") }
    var message by remember { mutableStateOf(initialAlarm?.message ?: "") }
    var hour by remember { mutableIntStateOf(initialAlarm?.hour ?: 7) }
    var minute by remember { mutableIntStateOf(initialAlarm?.minute ?: 0) }
    var is24Hour by remember { mutableStateOf(false) }

    var isRecurring by remember { mutableStateOf(initialAlarm?.isRecurring ?: false) }
    var selectedDays by remember {
        mutableStateOf(
            if (initialAlarm != null && initialAlarm.daysOfWeek.isNotBlank()) {
                initialAlarm.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
            } else {
                setOf(1, 2, 3, 4, 5) // Mon-Fri default
            }
        )
    }

    var isVibrate by remember { mutableStateOf(initialAlarm?.isVibrationEnabled ?: true) }
    var isGradualVolume by remember { mutableStateOf(initialAlarm?.isGradualVolume ?: false) }
    var snoozeMinutes by remember { mutableIntStateOf(initialAlarm?.snoozeDurationMinutes ?: 5) }

    // CRITICAL: Wake-Up Mission switch
    var isWakeUpAlarm by remember { mutableStateOf(initialAlarm?.isWakeUpAlarm ?: false) }
    var wakeUpDifficulty by remember { mutableStateOf(initialAlarm?.wakeUpDifficulty ?: "MEDIUM") }
    var wakeUpMissionType by remember { mutableStateOf(initialAlarm?.wakeUpMissionType ?: "MATHS") }
    var voiceMode by remember { mutableStateOf(initialAlarm?.voiceAnnouncementMode ?: "OFF") }

    // Audio preview state
    var isAudioPlaying by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            if (isAudioPlaying) {
                AlarmRingtonePlayer.stop()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (isAudioPlaying) AlarmRingtonePlayer.stop()
            onDismiss()
        },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CodexraDarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = if (initialAlarm == null) "CREATE ALARM" else "EDIT ALARM",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Time Selector (Hour & Minute)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1523)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Time Format", color = CodexraTextSecondary, fontSize = 13.sp)
                        Row {
                            TextButton(onClick = { is24Hour = false }) {
                                Text(
                                    "12H",
                                    color = if (!is24Hour) CodexraPrimaryCyan else CodexraTextSecondary,
                                    fontWeight = if (!is24Hour) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            TextButton(onClick = { is24Hour = true }) {
                                Text(
                                    "24H",
                                    color = if (is24Hour) CodexraPrimaryCyan else CodexraTextSecondary,
                                    fontWeight = if (is24Hour) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Number Steppers for Hour & Minute
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Hour
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            TextButton(onClick = { hour = (hour + 1) % 24 }) { Text("▲", color = CodexraPrimaryCyan) }
                            Text(
                                text = String.format("%02d", hour),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            TextButton(onClick = { hour = if (hour == 0) 23 else hour - 1 }) { Text("▼", color = CodexraPrimaryCyan) }
                        }

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.displayMedium.copy(color = Color.White),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        // Minute
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            TextButton(onClick = { minute = (minute + 1) % 60 }) { Text("▲", color = CodexraPrimaryCyan) }
                            Text(
                                text = String.format("%02d", minute),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            TextButton(onClick = { minute = if (minute == 0) 59 else minute - 1 }) { Text("▼", color = CodexraPrimaryCyan) }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title & Message fields
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Alarm Title (e.g. Wake-Up, Workout, Morning Brief)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("alarm_title_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Detailed Alarm Message (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Recurring vs One-time toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Repeat Schedule", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("Select individual days of the week", color = CodexraTextSecondary, fontSize = 12.sp)
                }
                Switch(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it }
                )
            }

            AnimatedVisibility(visible = isRecurring) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    val daysLabels = listOf(
                        Pair(1, "M"),
                        Pair(2, "T"),
                        Pair(3, "W"),
                        Pair(4, "T"),
                        Pair(5, "F"),
                        Pair(6, "S"),
                        Pair(7, "S")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for ((dayNum, label) in daysLabels) {
                            val isSelected = dayNum in selectedDays
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) CodexraPrimaryCyan else Color(0xFF1E293B))
                                    .clickable {
                                        selectedDays = if (isSelected) {
                                            if (selectedDays.size > 1) selectedDays - dayNum else selectedDays
                                        } else {
                                            selectedDays + dayNum
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CRITICAL SECTION: WAKE-UP MISSION CONFIGURATION
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isWakeUpAlarm) Color(0xFF291C08) else Color(0xFF131A29)
                ),
                border = if (isWakeUpAlarm) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CodexraAmberAccent)) else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (isWakeUpAlarm) CodexraAmberAccent else CodexraTextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Wake-Up Mission",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Requires 3 consecutive correct answers",
                                    color = if (isWakeUpAlarm) CodexraAmberAccent else CodexraTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Switch(
                            checked = isWakeUpAlarm,
                            onCheckedChange = { isWakeUpAlarm = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = CodexraAmberAccent
                            ),
                            modifier = Modifier.testTag("wake_up_mission_switch")
                        )
                    }

                    AnimatedVisibility(visible = isWakeUpAlarm) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Text(
                                text = "Mission Type",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CodexraAmberAccent
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("MATHS", "SEQUENCE", "MEMORY").forEach { type ->
                                    val isSelected = wakeUpMissionType == type
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) CodexraAmberAccent else Color(0xFF1F293D),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { wakeUpMissionType = type }
                                    ) {
                                        Text(
                                            text = type,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else Color.White,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Mission Difficulty",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CodexraAmberAccent
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("EASY", "MEDIUM", "HARD").forEach { diff ->
                                    val isSelected = wakeUpDifficulty == diff
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) CodexraAmberAccent else Color(0xFF1F293D),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { wakeUpDifficulty = diff }
                                    ) {
                                        Text(
                                            text = diff,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else Color.White,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sound Preview & Volume Controls
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1523)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Alarm Tone", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text("System default alarm ringtone", color = CodexraTextSecondary, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (isAudioPlaying) {
                                    AlarmRingtonePlayer.stop()
                                    isAudioPlaying = false
                                } else {
                                    AlarmRingtonePlayer.play(context, null, isVibrate, false)
                                    isAudioPlaying = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAudioPlaying) CodexraCrimsonAlert else CodexraPrimaryCyan
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isAudioPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAudioPlaying) "Stop" else "Preview",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vibration", color = Color.White)
                        Switch(checked = isVibrate, onCheckedChange = { isVibrate = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Gradual Volume Fade-In", color = Color.White)
                            Text("Softly increases volume over 10 seconds", color = CodexraTextSecondary, fontSize = 11.sp)
                        }
                        Switch(checked = isGradualVolume, onCheckedChange = { isGradualVolume = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    if (isAudioPlaying) AlarmRingtonePlayer.stop()
                    val alarmToSave = (initialAlarm ?: AlarmEntity(
                        hour = hour,
                        minute = minute,
                        title = title.ifBlank { "Alarm" }
                    )).copy(
                        title = title.ifBlank { "Alarm" },
                        message = message,
                        hour = hour,
                        minute = minute,
                        isRecurring = isRecurring,
                        daysOfWeek = if (isRecurring) selectedDays.sorted().joinToString(",") else "",
                        isVibrationEnabled = isVibrate,
                        isGradualVolume = isGradualVolume,
                        snoozeDurationMinutes = snoozeMinutes,
                        isWakeUpAlarm = isWakeUpAlarm,
                        wakeUpDifficulty = wakeUpDifficulty,
                        wakeUpMissionType = wakeUpMissionType,
                        voiceAnnouncementMode = voiceMode,
                        isEnabled = true
                    )
                    onSave(alarmToSave)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_alarm_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWakeUpAlarm) CodexraAmberAccent else CodexraPrimaryCyan
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (initialAlarm == null) "SAVE ALARM" else "UPDATE ALARM",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
