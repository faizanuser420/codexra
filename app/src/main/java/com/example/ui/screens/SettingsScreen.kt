package com.example.ui.screens

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.example.backup.BackupPreview
import com.example.backup.BackupRestoreManager
import com.example.sound.VoiceAnnouncer
import com.example.ui.CodexraViewModel
import com.example.ui.theme.CodexraAmberAccent
import com.example.ui.theme.CodexraCrimsonAlert
import com.example.ui.theme.CodexraDarkSurface
import com.example.ui.theme.CodexraPrimaryCyan
import com.example.ui.theme.CodexraSuccessGreen
import com.example.ui.theme.CodexraTextSecondary
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun SettingsScreen(viewModel: CodexraViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Permissions check
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    val hasExactAlarmPermission = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager?.canScheduleExactAlarms() == true
        } else {
            true
        }
    }
    val hasNotificationPermission = remember(context) {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val isBatteryOptimized = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            powerManager?.isIgnoringBatteryOptimizations(context.packageName) == false
        } else {
            false
        }
    }

    // TTS Tester
    var voiceAnnouncer by remember { mutableStateOf<VoiceAnnouncer?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        voiceAnnouncer = VoiceAnnouncer(context)
        onDispose {
            voiceAnnouncer?.shutdown()
        }
    }

    // Backup & Restore states
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var restoreDialogData by remember { mutableStateOf<Pair<JSONObject, BackupPreview>?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            isLoading = true
            coroutineScope.launch {
                val res = BackupRestoreManager.exportBackup(context, uri)
                isLoading = false
                statusMessage = if (res.isSuccess) {
                    "Backup exported successfully! (${res.getOrNull()} items)"
                } else {
                    "Export failed: ${res.exceptionOrNull()?.localizedMessage}"
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isLoading = true
            coroutineScope.launch {
                val parseRes = BackupRestoreManager.parseAndValidateBackup(context, uri)
                isLoading = false
                if (parseRes.isSuccess) {
                    restoreDialogData = parseRes.getOrNull()
                } else {
                    statusMessage = "Invalid backup: ${parseRes.exceptionOrNull()?.localizedMessage}"
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "SETTINGS & SYSTEM RELIABILITY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = CodexraPrimaryCyan
                )
            )
            Text(
                text = "Permissions, voice announcements, and offline backups",
                style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary)
            )
        }

        // Status Toast Message
        if (statusMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CodexraPrimaryCyan.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statusMessage!!,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { statusMessage = null }) {
                            Text("✕", color = Color.White)
                        }
                    }
                }
            }
        }

        // 1. Android Reliability & Permissions
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CodexraDarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Permissions & Reliability",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Android requires explicit access for exact timing and lock-screen rings.",
                        style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary),
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                    )

                    // Exact Alarm Item
                    PermissionStatusRow(
                        title = "Exact Alarm Timing",
                        description = "Enables alarms to fire at precise scheduled seconds.",
                        isGranted = hasExactAlarmPermission,
                        actionLabel = "Open Setting",
                        onAction = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                try { context.startActivity(intent) } catch (e: Exception) {}
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notifications Item
                    PermissionStatusRow(
                        title = "Notifications & Full-Screen Alerts",
                        description = "Displays ringing wake-up screen and reminders.",
                        isGranted = hasNotificationPermission,
                        actionLabel = "App Notifications",
                        onAction = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            try { context.startActivity(intent) } catch (e: Exception) {}
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Battery Optimizations
                    PermissionStatusRow(
                        title = "Unrestricted Background Power",
                        description = "Prevents device battery saver from killing alarm receivers.",
                        isGranted = !isBatteryOptimized,
                        actionLabel = "Battery Settings",
                        onAction = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                try { context.startActivity(intent) } catch (e: Exception) {}
                            }
                        }
                    )
                }
            }
        }

        // 2. Voice Announcements (TextToSpeech)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CodexraDarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = CodexraPrimaryCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Voice Announcements (TTS)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Text(
                        text = "Uses Android Text-To-Speech engine to announce alarm titles aloud.",
                        style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary),
                        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                    )

                    Button(
                        onClick = {
                            isSpeaking = true
                            voiceAnnouncer?.speak("Attention. CODEXRA Alarm test announcement. Train your mind. Master your decisions.") {
                                isSpeaking = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CodexraPrimaryCyan),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("test_voice_announcement_button")
                    ) {
                        Text(
                            text = if (isSpeaking) "Speaking..." else "Test Speech Announcement",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. Offline Data, Backup & Restore
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CodexraDarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Backup & Restore",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "100% offline JSON data backup. Exports alarms, reminders, routines, and tasks.",
                        style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary),
                        modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                exportLauncher.launch("codexra_backup_${System.currentTimeMillis()}.json")
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_backup_button")
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Backup")
                        }

                        OutlinedButton(
                            onClick = {
                                importLauncher.launch(arrayOf("application/json", "*/*"))
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_backup_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Backup")
                        }
                    }

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = CodexraPrimaryCyan)
                        }
                    }
                }
            }
        }

        // 4. About CODEXRA ALARM
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C111C)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CODEXRA ALARM",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp,
                            color = CodexraPrimaryCyan
                        )
                    )
                    Text(
                        text = "Train Your Mind. Master Your Decisions.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = CodexraAmberAccent
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "Version 1.0 • Built with Kotlin & Jetpack Compose\nZero external trackers • Zero ads • 100% Offline Core",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CodexraTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }

    // Restore Confirmation Dialog
    if (restoreDialogData != null) {
        val (json, preview) = restoreDialogData!!
        var isReplaceMode by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { restoreDialogData = null },
            title = {
                Text(
                    text = "Confirm Backup Restore",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Backup contains: ${preview.alarmsCount} alarms, ${preview.remindersCount} reminders, ${preview.routinesCount} routines, ${preview.tasksCount} tasks.",
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = isReplaceMode,
                            onCheckedChange = { isReplaceMode = it }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isReplaceMode) "Replace existing data" else "Merge with existing data",
                            color = if (isReplaceMode) CodexraAmberAccent else CodexraPrimaryCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentData = restoreDialogData
                        restoreDialogData = null
                        if (currentData != null) {
                            isLoading = true
                            coroutineScope.launch {
                                val res = BackupRestoreManager.restoreBackup(context, currentData.first, isReplaceMode)
                                isLoading = false
                                statusMessage = if (res.isSuccess) {
                                    "Restored ${res.getOrNull()} items successfully!"
                                } else {
                                    "Restore failed: ${res.exceptionOrNull()?.localizedMessage}"
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CodexraPrimaryCyan)
                ) {
                    Text("Proceed Restore", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { restoreDialogData = null }) {
                    Text("Cancel", color = CodexraTextSecondary)
                }
            },
            containerColor = CodexraDarkSurface
        )
    }
}

@Composable
fun PermissionStatusRow(
    title: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isGranted) CodexraSuccessGreen else CodexraCrimsonAlert)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = CodexraTextSecondary,
                    fontSize = 11.sp
                ),
                modifier = Modifier.padding(start = 18.dp, top = 2.dp)
            )
        }

        OutlinedButton(
            onClick = onAction,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = if (isGranted) "Granted" else actionLabel,
                fontSize = 11.sp,
                color = if (isGranted) CodexraSuccessGreen else CodexraAmberAccent
            )
        }
    }
}
