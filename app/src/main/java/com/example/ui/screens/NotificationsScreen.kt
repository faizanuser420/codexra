package com.example.ui.screens

import android.media.RingtoneManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.example.data.model.CustomReminderEntity
import com.example.sound.AlarmRingtonePlayer
import com.example.ui.CodexraViewModel
import com.example.ui.theme.CodexraAmberAccent
import com.example.ui.theme.CodexraCrimsonAlert
import com.example.ui.theme.CodexraDarkSurface
import com.example.ui.theme.CodexraPrimaryCyan
import com.example.ui.theme.CodexraSuccessGreen
import com.example.ui.theme.CodexraTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(viewModel: CodexraViewModel) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    var selectedReminderForEdit by remember { mutableStateOf<CustomReminderEntity?>(null) }
    var showCreateSheet by remember { mutableStateOf(false) }
    var reminderToDelete by remember { mutableStateOf<CustomReminderEntity?>(null) }

    // Quick reminder presets
    val presets = listOf(
        Pair("💧 Water Drink", "Time to hydrate with a glass of water"),
        Pair("📚 Tuition Time", "Prepare books and notes for tuition class"),
        Pair("🕌 Namaz Reminder", "Time for prayer and reflection"),
        Pair("📖 Study Time", "Deep focus study session"),
        Pair("🏋️ Exercise Time", "Physical training and stretching"),
        Pair("🏪 Shop Opening", "Prepare and open shop for business"),
        Pair("🌙 Bedtime", "Wind down, phone away, prepare for sleep")
    )

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedReminderForEdit = null
                    showCreateSheet = true
                },
                containerColor = CodexraAmberAccent,
                contentColor = Color.Black,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("add_reminder_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
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
                text = "CUSTOM NOTIFICATIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = CodexraAmberAccent
                )
            )
            Text(
                text = "Unlimited notification reminders • Zero math challenges",
                style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Quick Preset Chips
            Text(
                text = "QUICK PRESETS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = CodexraTextSecondary
                ),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                items(presets) { preset ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = CodexraDarkSurface,
                        onClick = {
                            selectedReminderForEdit = CustomReminderEntity(
                                title = preset.first,
                                message = preset.second,
                                hour = 12,
                                minute = 0,
                                repeatType = "DAILY"
                            )
                            showCreateSheet = true
                        }
                    ) {
                        Text(
                            text = preset.first,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = CodexraTextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Reminders Scheduled",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Tap a preset above or tap + to schedule a custom notification",
                            style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onToggle = { viewModel.toggleReminder(reminder) },
                            onEdit = {
                                selectedReminderForEdit = reminder
                                showCreateSheet = true
                            },
                            onDuplicate = { viewModel.duplicateReminder(reminder) },
                            onDelete = { reminderToDelete = reminder }
                        )
                    }
                }
            }
        }
    }

    if (showCreateSheet) {
        ReminderEditSheet(
            initialReminder = selectedReminderForEdit,
            onDismiss = { showCreateSheet = false },
            onSave = { reminder ->
                viewModel.saveReminder(reminder)
                showCreateSheet = false
            }
        )
    }

    if (reminderToDelete != null) {
        AlertDialog(
            onDismissRequest = { reminderToDelete = null },
            title = { Text("Delete Reminder?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${reminderToDelete?.title}'?", color = CodexraTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        reminderToDelete?.let { viewModel.deleteReminder(it) }
                        reminderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CodexraCrimsonAlert)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { reminderToDelete = null }) {
                    Text("Cancel", color = CodexraPrimaryCyan)
                }
            },
            containerColor = CodexraDarkSurface
        )
    }
}

@Composable
fun ReminderCard(
    reminder: CustomReminderEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedTime = String.format("%02d:%02d", reminder.hour, reminder.minute)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reminder_card_${reminder.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isEnabled) CodexraDarkSurface else Color(0xFF0D121D)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (reminder.isEnabled) CodexraAmberAccent else CodexraTextSecondary
                        )
                    )
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (reminder.isEnabled) Color.White else CodexraTextSecondary
                        )
                    )
                }

                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = CodexraAmberAccent
                    ),
                    modifier = Modifier.testTag("reminder_toggle_${reminder.id}")
                )
            }

            if (reminder.message.isNotBlank()) {
                Text(
                    text = reminder.message,
                    style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = when (reminder.repeatType) {
                            "DAILY" -> "Repeats Daily"
                            "WEEKDAYS" -> "Repeats Weekdays"
                            else -> "One-time"
                        },
                        fontSize = 11.sp,
                        color = CodexraTextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row {
                    IconButton(onClick = onDuplicate, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = CodexraTextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CodexraAmberAccent, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CodexraCrimsonAlert, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderEditSheet(
    initialReminder: CustomReminderEntity?,
    onDismiss: () -> Unit,
    onSave: (CustomReminderEntity) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialReminder?.title ?: "") }
    var message by remember { mutableStateOf(initialReminder?.message ?: "") }
    var hour by remember { mutableIntStateOf(initialReminder?.hour ?: 12) }
    var minute by remember { mutableIntStateOf(initialReminder?.minute ?: 0) }
    var repeatType by remember { mutableStateOf(initialReminder?.repeatType ?: "NONE") }
    var isVibrate by remember { mutableStateOf(initialReminder?.isVibrationEnabled ?: true) }

    var isPreviewingAudio by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            if (isPreviewingAudio) AlarmRingtonePlayer.stop()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (isPreviewingAudio) AlarmRingtonePlayer.stop()
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
                text = if (initialReminder == null) "NEW CUSTOM REMINDER" else "EDIT REMINDER",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            )

            Text(
                text = "Notifications trigger directly on schedule without math missions",
                style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary),
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            // Time Selector
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1523)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TextButton(onClick = { hour = (hour + 1) % 24 }) { Text("▲", color = CodexraAmberAccent) }
                        Text(
                            text = String.format("%02d", hour),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        TextButton(onClick = { hour = if (hour == 0) 23 else hour - 1 }) { Text("▼", color = CodexraAmberAccent) }
                    }

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.displayMedium.copy(color = Color.White),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TextButton(onClick = { minute = (minute + 1) % 60 }) { Text("▲", color = CodexraAmberAccent) }
                        Text(
                            text = String.format("%02d", minute),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        TextButton(onClick = { minute = if (minute == 0) 59 else minute - 1 }) { Text("▼", color = CodexraAmberAccent) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Reminder Title (e.g. Water Drink Time)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reminder_title_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Detailed Message (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Repeat pattern
            Text("Repeat Type", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("NONE", "One-Time"),
                    Pair("DAILY", "Daily"),
                    Pair("WEEKDAYS", "Weekdays")
                ).forEach { (type, label) ->
                    val isSelected = repeatType == type
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CodexraAmberAccent else Color(0xFF1E293B),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { repeatType = type }
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else Color.White,
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sound and Vibration
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1523)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Notification Audio", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text("Uses system alert sound", color = CodexraTextSecondary, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                if (isPreviewingAudio) {
                                    AlarmRingtonePlayer.stop()
                                    isPreviewingAudio = false
                                } else {
                                    AlarmRingtonePlayer.play(context, null, false, false)
                                    isPreviewingAudio = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPreviewingAudio) CodexraCrimsonAlert else CodexraAmberAccent
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isPreviewingAudio) "Stop" else "Preview", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vibration", color = Color.White)
                        Switch(checked = isVibrate, onCheckedChange = { isVibrate = it })
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isPreviewingAudio) AlarmRingtonePlayer.stop()
                    val toSave = (initialReminder ?: CustomReminderEntity(
                        hour = hour,
                        minute = minute,
                        title = title.ifBlank { "Reminder" }
                    )).copy(
                        title = title.ifBlank { "Reminder" },
                        message = message,
                        hour = hour,
                        minute = minute,
                        repeatType = repeatType,
                        isVibrationEnabled = isVibrate,
                        isEnabled = true
                    )
                    onSave(toSave)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_reminder_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CodexraAmberAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (initialReminder == null) "SAVE REMINDER" else "UPDATE REMINDER",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
