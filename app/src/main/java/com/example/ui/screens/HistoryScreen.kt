package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.HistoryRecordEntity
import com.example.ui.CodexraViewModel
import com.example.ui.theme.CodexraAmberAccent
import com.example.ui.theme.CodexraCrimsonAlert
import com.example.ui.theme.CodexraDarkSurface
import com.example.ui.theme.CodexraPrimaryCyan
import com.example.ui.theme.CodexraSuccessGreen
import com.example.ui.theme.CodexraTextSecondary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(viewModel: CodexraViewModel) {
    val history by viewModel.historyRecords.collectAsStateWithLifecycle()
    val disciplineScore by viewModel.disciplineScore.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val routines by viewModel.routines.collectAsStateWithLifecycle()

    val totalCompletedTasks = remember(tasks) { tasks.count { it.status == "COMPLETED" } }
    val missionsSuccessCount = remember(history) { history.count { it.type == "MISSION_SUCCESS" } }
    val emergencyStopsCount = remember(history) { history.count { it.type == "EMERGENCY_DISMISSED" } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "DISCIPLINE & HISTORY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = CodexraPrimaryCyan
                )
            )
            Text(
                text = "Train Your Mind. Master Your Decisions.",
                style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary)
            )
        }

        // 1. Discipline Score Summary Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CodexraDarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Discipline",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "${disciplineScore.completedItems} / ${disciplineScore.totalDueItems} activities fulfilled",
                                style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .border(
                                    3.dp,
                                    if (disciplineScore.percentage >= 80) CodexraSuccessGreen else CodexraAmberAccent,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${disciplineScore.percentage}%",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Transparent Formula explanation
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F1523),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = CodexraPrimaryCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Calculation Formula",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CodexraPrimaryCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = disciplineScore.explanation,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Supportive Streak Banner
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CodexraAmberAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = CodexraAmberAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Current Streak: ${disciplineScore.currentStreak} Days (Best: ${disciplineScore.bestStreak} Days)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Streaks measure consistency, not worth. You are always in control of your next decision.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = CodexraTextSecondary,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // 2. Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricMiniCard(
                    title = "Tasks Done",
                    value = "$totalCompletedTasks",
                    color = Color(0xFFA855F7),
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    title = "Missions Passed",
                    value = "$missionsSuccessCount",
                    color = CodexraSuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    title = "Emergency Stops",
                    value = "$emergencyStopsCount",
                    color = if (emergencyStopsCount > 0) CodexraCrimsonAlert else CodexraTextSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Activity Timeline Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVITY LOG",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = CodexraTextSecondary
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        if (history.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CodexraDarkSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No recorded history yet. Alarm dismissals, Wake-Up missions, and reminder events will be logged here.",
                        color = CodexraTextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(history, key = { it.id }) { record ->
                HistoryRecordRow(record)
            }
        }
    }
}

@Composable
fun MetricMiniCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CodexraDarkSurface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = CodexraTextSecondary,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun HistoryRecordRow(record: HistoryRecordEntity) {
    val timeFormatted = remember(record.timestamp) {
        Instant.ofEpochMilli(record.timestamp)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MMM d, HH:mm"))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CodexraDarkSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when (record.type) {
                            "MISSION_SUCCESS" -> CodexraSuccessGreen.copy(alpha = 0.2f)
                            "EMERGENCY_DISMISSED" -> CodexraCrimsonAlert.copy(alpha = 0.2f)
                            "REMINDER_TRIGGERED" -> CodexraAmberAccent.copy(alpha = 0.2f)
                            "FOCUS_COMPLETED" -> Color(0xFFA855F7).copy(alpha = 0.2f)
                            else -> CodexraPrimaryCyan.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (record.type) {
                        "MISSION_SUCCESS" -> Icons.Default.Security
                        "EMERGENCY_DISMISSED" -> Icons.Default.Warning
                        "REMINDER_TRIGGERED" -> Icons.Default.Notifications
                        "FOCUS_COMPLETED" -> Icons.Default.PlayArrow
                        else -> Icons.Default.CheckCircle
                    },
                    contentDescription = null,
                    tint = when (record.type) {
                        "MISSION_SUCCESS" -> CodexraSuccessGreen
                        "EMERGENCY_DISMISSED" -> CodexraCrimsonAlert
                        "REMINDER_TRIGGERED" -> CodexraAmberAccent
                        "FOCUS_COMPLETED" -> Color(0xFFA855F7)
                        else -> CodexraPrimaryCyan
                    },
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
                if (record.details.isNotBlank()) {
                    Text(
                        text = record.details,
                        style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary)
                    )
                }
            }

            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.labelSmall.copy(color = CodexraTextSecondary)
            )
        }
    }
}
