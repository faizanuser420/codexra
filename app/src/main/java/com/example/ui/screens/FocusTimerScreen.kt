package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sound.AlarmRingtonePlayer
import com.example.ui.CodexraViewModel
import com.example.ui.theme.CodexraAmberAccent
import com.example.ui.theme.CodexraCrimsonAlert
import com.example.ui.theme.CodexraDarkSurface
import com.example.ui.theme.CodexraPrimaryCyan
import com.example.ui.theme.CodexraSuccessGreen
import com.example.ui.theme.CodexraTextSecondary
import kotlinx.coroutines.delay

@Composable
fun FocusTimerScreen(viewModel: CodexraViewModel) {
    val context = LocalContext.current
    val focusSessions by viewModel.focusSessions.collectAsStateWithLifecycle()

    var focusMinutes by remember { mutableIntStateOf(25) }
    var breakMinutes by remember { mutableIntStateOf(5) }
    var isBreakPhase by remember { mutableStateOf(false) }

    var isRunning by remember { mutableStateOf(false) }
    var secondsRemaining by remember { mutableIntStateOf(focusMinutes * 60) }
    var completedCycles by remember { mutableIntStateOf(0) }
    var selectedTag by remember { mutableStateOf("STUDY") }

    val tags = listOf("STUDY", "WORK", "CODING", "READING")

    // Timer coroutine
    LaunchedEffect(isRunning, secondsRemaining) {
        if (isRunning && secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        } else if (isRunning && secondsRemaining == 0) {
            // Cycle finished
            AlarmRingtonePlayer.play(context, null, true, false)
            delay(1500)
            AlarmRingtonePlayer.stop()

            if (!isBreakPhase) {
                completedCycles++
                viewModel.recordFocusSession(focusMinutes, completedCycles, selectedTag)
                // Switch to break
                isBreakPhase = true
                secondsRemaining = breakMinutes * 60
            } else {
                // Switch back to focus
                isBreakPhase = false
                secondsRemaining = focusMinutes * 60
            }
        }
    }

    val totalDurationSeconds = if (isBreakPhase) breakMinutes * 60 else focusMinutes * 60
    val progress = if (totalDurationSeconds > 0) {
        (totalDurationSeconds - secondsRemaining).toFloat() / totalDurationSeconds.toFloat()
    } else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "focusProgress")

    val minutesPart = secondsRemaining / 60
    val secondsPart = secondsRemaining % 60
    val timeFormatted = String.format("%02d:%02d", minutesPart, secondsPart)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(bottom = 90.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "TACTICAL FOCUS TIMER",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
                color = Color(0xFFA855F7)
            )
        )
        Text(
            text = "Deep concentration intervals without missions",
            style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Tag selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { tag ->
                val isSelected = selectedTag == tag
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color(0xFFA855F7) else CodexraDarkSurface,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { if (!isRunning) selectedTag = tag }
                ) {
                    Text(
                        text = tag,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else CodexraTextSecondary,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Circular Timer Visual
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                // Background Track
                drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                // Progress Arc
                drawArc(
                    color = if (isBreakPhase) CodexraSuccessGreen else Color(0xFFA855F7),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isBreakPhase) "BREAK TIME" else "FOCUS BLOCK",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (isBreakPhase) CodexraSuccessGreen else Color(0xFFA855F7)
                    )
                )
                Text(
                    text = timeFormatted,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Cycles Completed: $completedCycles",
                    style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Play / Pause / Reset Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    isRunning = false
                    isBreakPhase = false
                    secondsRemaining = focusMinutes * 60
                },
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(CodexraDarkSurface)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    tint = CodexraTextSecondary
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Button(
                onClick = { isRunning = !isRunning },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) CodexraAmberAccent else Color(0xFFA855F7)
                ),
                shape = CircleShape,
                modifier = Modifier
                    .size(68.dp)
                    .testTag("focus_toggle_button")
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    tint = if (isRunning) Color.Black else Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Interval duration adjustment
        if (!isRunning) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CodexraDarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Duration Settings", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Focus Minutes", color = CodexraTextSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { if (focusMinutes > 5) { focusMinutes -= 5; secondsRemaining = focusMinutes * 60 } }) {
                                Text("-5m", color = CodexraPrimaryCyan)
                            }
                            Text("$focusMinutes m", color = Color.White, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { if (focusMinutes < 120) { focusMinutes += 5; secondsRemaining = focusMinutes * 60 } }) {
                                Text("+5m", color = CodexraPrimaryCyan)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Break Minutes", color = CodexraTextSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { if (breakMinutes > 1) breakMinutes -= 1 }) {
                                Text("-1m", color = CodexraPrimaryCyan)
                            }
                            Text("$breakMinutes m", color = Color.White, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { if (breakMinutes < 30) breakMinutes += 1 }) {
                                Text("+1m", color = CodexraPrimaryCyan)
                            }
                        }
                    }
                }
            }
        }
    }
}
