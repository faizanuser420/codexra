package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.HabitEntity
import com.example.data.model.RoutineEntity
import com.example.ui.CodexraViewModel
import com.example.ui.theme.CodexraAmberAccent
import com.example.ui.theme.CodexraCrimsonAlert
import com.example.ui.theme.CodexraDarkSurface
import com.example.ui.theme.CodexraPrimaryCyan
import com.example.ui.theme.CodexraSuccessGreen
import com.example.ui.theme.CodexraTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(viewModel: CodexraViewModel) {
    val routines by viewModel.routines.collectAsStateWithLifecycle()
    val todayRoutineLogs by viewModel.todayRoutineLogs.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val todayHabitLogs by viewModel.todayHabitLogs.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf("ROUTINES") } // ROUTINES, HABITS

    var showCreateRoutineSheet by remember { mutableStateOf(false) }
    var selectedRoutineForEdit by remember { mutableStateOf<RoutineEntity?>(null) }
    var routineToDelete by remember { mutableStateOf<RoutineEntity?>(null) }
    var routineToSkipConfirm by remember { mutableStateOf<RoutineEntity?>(null) }

    var showCreateHabitSheet by remember { mutableStateOf(false) }
    var selectedHabitForEdit by remember { mutableStateOf<HabitEntity?>(null) }
    var habitToDelete by remember { mutableStateOf<HabitEntity?>(null) }

    // Preset categories for routines
    val routinePresets = listOf(
        Pair("Fajr Prayer", "PRAYER"),
        Pair("Morning Exercise", "EXERCISE"),
        Pair("Deep Study Block", "STUDY"),
        Pair("Tuition Session", "TUITION"),
        Pair("Shop & Inventory Check", "SHOP"),
        Pair("Water Interval", "WATER"),
        Pair("Bedtime & Phone Off", "BEDTIME")
    )

    // Preset habits
    val habitPresets = listOf(
        Pair("💧 Drink 2L Water", "HEALTH"),
        Pair("🧘 10m Mindfulness", "MINDFULNESS"),
        Pair("📖 Read 15 Pages", "LEARNING"),
        Pair("💪 30 Pushups", "FITNESS"),
        Pair("🎯 Daily Top 3 Goals", "DISCIPLINE")
    )

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (activeSubTab == "ROUTINES") {
                        selectedRoutineForEdit = null
                        showCreateRoutineSheet = true
                    } else {
                        selectedHabitForEdit = null
                        showCreateHabitSheet = true
                    }
                },
                containerColor = if (activeSubTab == "ROUTINES") CodexraSuccessGreen else CodexraPrimaryCyan,
                contentColor = Color.Black,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("add_routine_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
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
                text = "ROUTINES & USER HABITS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = CodexraSuccessGreen
                )
            )
            Text(
                text = "Persistent daily routines, prayer times, and habit streaks",
                style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Sub-tab switcher: Routines vs Habits
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (activeSubTab == "ROUTINES") CodexraSuccessGreen else CodexraDarkSurface,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeSubTab = "ROUTINES" }
                ) {
                    Text(
                        text = "Daily Routines (${routines.size})",
                        color = if (activeSubTab == "ROUTINES") Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 10.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (activeSubTab == "HABITS") CodexraPrimaryCyan else CodexraDarkSurface,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeSubTab = "HABITS" }
                ) {
                    Text(
                        text = "User Habits (${habits.size})",
                        color = if (activeSubTab == "HABITS") Color.Black else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 10.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            if (activeSubTab == "ROUTINES") {
                // Preset Row for routines
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    items(routinePresets) { (name, category) ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = CodexraDarkSurface,
                            onClick = {
                                selectedRoutineForEdit = RoutineEntity(
                                    name = name,
                                    category = category,
                                    hour = 6,
                                    minute = 0
                                )
                                showCreateRoutineSheet = true
                            }
                        ) {
                            Text(
                                text = "+ $name",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                if (routines.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = null,
                                tint = CodexraTextSecondary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Daily Routines Configured",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Add manual prayer times, study, exercise, or bedtime routines",
                                style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(routines, key = { it.id }) { routine ->
                            val log = todayRoutineLogs.firstOrNull { it.routineId == routine.id }
                            val isCompleted = log?.isCompleted == true
                            val isSkipped = log?.isSkipped == true

                            RoutineCard(
                                routine = routine,
                                isCompleted = isCompleted,
                                isSkipped = isSkipped,
                                onToggleComplete = {
                                    viewModel.setRoutineCompletion(routine.id, !isCompleted)
                                },
                                onSkip = {
                                    routineToSkipConfirm = routine
                                },
                                onEdit = {
                                    selectedRoutineForEdit = routine
                                    showCreateRoutineSheet = true
                                },
                                onDelete = {
                                    routineToDelete = routine
                                }
                            )
                        }
                    }
                }
            } else {
                // HABITS SUB-TAB
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 14.dp)
                ) {
                    items(habitPresets) { (name, category) ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = CodexraDarkSurface,
                            onClick = {
                                selectedHabitForEdit = HabitEntity(
                                    title = name,
                                    category = category
                                )
                                showCreateHabitSheet = true
                            }
                        ) {
                            Text(
                                text = "+ $name",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                if (habits.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = CodexraTextSecondary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No User Habits Added",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "Tap + to track hydration, reading, pushups, and build streaks",
                                style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(habits, key = { it.id }) { habit ->
                            val log = todayHabitLogs.firstOrNull { it.habitId == habit.id }
                            val isCompleted = log?.isCompleted == true

                            HabitCard(
                                habit = habit,
                                isCompleted = isCompleted,
                                onToggleComplete = {
                                    viewModel.setHabitCompletion(habit.id, !isCompleted)
                                },
                                onEdit = {
                                    selectedHabitForEdit = habit
                                    showCreateHabitSheet = true
                                },
                                onDelete = {
                                    habitToDelete = habit
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateRoutineSheet) {
        RoutineEditSheet(
            initialRoutine = selectedRoutineForEdit,
            onDismiss = { showCreateRoutineSheet = false },
            onSave = { routine ->
                viewModel.saveRoutine(routine)
                showCreateRoutineSheet = false
            }
        )
    }

    if (showCreateHabitSheet) {
        HabitEditSheet(
            initialHabit = selectedHabitForEdit,
            onDismiss = { showCreateHabitSheet = false },
            onSave = { habit ->
                viewModel.saveHabit(habit)
                showCreateHabitSheet = false
            }
        )
    }

    if (routineToSkipConfirm != null) {
        AlertDialog(
            onDismissRequest = { routineToSkipConfirm = null },
            title = { Text("Skip Routine for Today?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Marking '${routineToSkipConfirm?.name}' as skipped for today will not count as a failure.", color = CodexraTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        routineToSkipConfirm?.let {
                            viewModel.setRoutineCompletion(it.id, isCompleted = false, isSkipped = true)
                        }
                        routineToSkipConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CodexraAmberAccent)
                ) {
                    Text("Confirm Skip", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { routineToSkipConfirm = null }) {
                    Text("Cancel", color = CodexraPrimaryCyan)
                }
            },
            containerColor = CodexraDarkSurface
        )
    }

    if (routineToDelete != null) {
        AlertDialog(
            onDismissRequest = { routineToDelete = null },
            title = { Text("Delete Routine?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Delete '${routineToDelete?.name}' from your daily planner?", color = CodexraTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        routineToDelete?.let { viewModel.deleteRoutine(it) }
                        routineToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CodexraCrimsonAlert)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { routineToDelete = null }) {
                    Text("Cancel", color = CodexraPrimaryCyan)
                }
            },
            containerColor = CodexraDarkSurface
        )
    }

    if (habitToDelete != null) {
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("Delete Habit?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Delete habit '${habitToDelete?.title}' from persistent storage?", color = CodexraTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        habitToDelete?.let { viewModel.deleteHabit(it) }
                        habitToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CodexraCrimsonAlert)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToDelete = null }) {
                    Text("Cancel", color = CodexraPrimaryCyan)
                }
            },
            containerColor = CodexraDarkSurface
        )
    }
}

@Composable
fun HabitCard(
    habit: HabitEntity,
    isCompleted: Boolean,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("habit_card_${habit.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFF0D2520) else CodexraDarkSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleComplete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Toggle Complete",
                            tint = if (isCompleted) CodexraSuccessGreen else CodexraTextSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = habit.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCompleted) CodexraSuccessGreen else Color.White
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${habit.category} • ${habit.frequencyType}",
                                style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(CodexraAmberAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = CodexraAmberAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${habit.currentStreak}d",
                        fontWeight = FontWeight.Bold,
                        color = CodexraAmberAccent,
                        fontSize = 13.sp
                    )
                }
            }

            if (habit.description.isNotBlank()) {
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary),
                    modifier = Modifier.padding(start = 46.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CodexraPrimaryCyan, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CodexraCrimsonAlert, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitEditSheet(
    initialHabit: HabitEntity?,
    onDismiss: () -> Unit,
    onSave: (HabitEntity) -> Unit
) {
    var title by remember { mutableStateOf(initialHabit?.title ?: "") }
    var description by remember { mutableStateOf(initialHabit?.description ?: "") }
    var category by remember { mutableStateOf(initialHabit?.category ?: "DISCIPLINE") }
    var frequencyType by remember { mutableStateOf(initialHabit?.frequencyType ?: "DAILY") }

    val categories = listOf("DISCIPLINE", "HEALTH", "FITNESS", "LEARNING", "PRODUCTIVITY", "MINDFULNESS")
    val frequencies = listOf("DAILY", "WEEKDAYS", "WEEKLY")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                text = if (initialHabit == null) "NEW USER HABIT" else "EDIT HABIT",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = "Persisted locally in Room database for continuous streak tracking",
                style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary),
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Habit Title (e.g. 10m Meditation, Drink 2L Water)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Target or Details (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Category", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = category == cat
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CodexraPrimaryCyan else Color(0xFF1E293B),
                        onClick = { category = cat }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Frequency", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                frequencies.forEach { freq ->
                    val isSelected = frequencyType == freq
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CodexraAmberAccent else Color(0xFF1E293B),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { frequencyType = freq }
                    ) {
                        Text(
                            text = freq,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else Color.White,
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val toSave = (initialHabit ?: HabitEntity(
                        title = title.ifBlank { "Habit" }
                    )).copy(
                        title = title.ifBlank { "Habit" },
                        description = description,
                        category = category,
                        frequencyType = frequencyType
                    )
                    onSave(toSave)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_habit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CodexraPrimaryCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (initialHabit == null) "SAVE HABIT" else "UPDATE HABIT",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun RoutineCard(
    routine: RoutineEntity,
    isCompleted: Boolean,
    isSkipped: Boolean,
    onToggleComplete: () -> Unit,
    onSkip: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val formattedTime = String.format("%02d:%02d", routine.hour, routine.minute)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("routine_card_${routine.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> Color(0xFF0F241A)
                isSkipped -> Color(0xFF1F1A12)
                else -> CodexraDarkSurface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleComplete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Toggle Complete",
                            tint = if (isCompleted) CodexraSuccessGreen else CodexraTextSecondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = routine.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCompleted) CodexraSuccessGreen else Color.White
                            )
                        )
                        Text(
                            text = "$formattedTime • ${routine.category}",
                            style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isCompleted -> CodexraSuccessGreen.copy(alpha = 0.2f)
                        isSkipped -> CodexraAmberAccent.copy(alpha = 0.2f)
                        else -> Color(0xFF1E293B)
                    }
                ) {
                    Text(
                        text = when {
                            isCompleted -> "COMPLETED"
                            isSkipped -> "SKIPPED"
                            else -> "DUE TODAY"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isCompleted -> CodexraSuccessGreen
                            isSkipped -> CodexraAmberAccent
                            else -> CodexraTextSecondary
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (routine.description.isNotBlank()) {
                Text(
                    text = routine.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary),
                    modifier = Modifier.padding(start = 46.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isCompleted && !isSkipped) {
                    TextButton(onClick = onSkip) {
                        Text("Skip Today", color = CodexraAmberAccent, fontSize = 12.sp)
                    }
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CodexraPrimaryCyan, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CodexraCrimsonAlert, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineEditSheet(
    initialRoutine: RoutineEntity?,
    onDismiss: () -> Unit,
    onSave: (RoutineEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialRoutine?.name ?: "") }
    var description by remember { mutableStateOf(initialRoutine?.description ?: "") }
    var category by remember { mutableStateOf(initialRoutine?.category ?: "CUSTOM") }
    var hour by remember { mutableIntStateOf(initialRoutine?.hour ?: 8) }
    var minute by remember { mutableIntStateOf(initialRoutine?.minute ?: 0) }

    val categories = listOf("PRAYER", "EXERCISE", "STUDY", "TUITION", "SHOP", "WATER", "BEDTIME", "CUSTOM")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                text = if (initialRoutine == null) "NEW DAILY ROUTINE" else "EDIT ROUTINE",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            )
            Text(
                text = "Manual schedules without requiring GPS or automatic calculations",
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
                        TextButton(onClick = { hour = (hour + 1) % 24 }) { Text("▲", color = CodexraSuccessGreen) }
                        Text(
                            text = String.format("%02d", hour),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        TextButton(onClick = { hour = if (hour == 0) 23 else hour - 1 }) { Text("▼", color = CodexraSuccessGreen) }
                    }

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.displayMedium.copy(color = Color.White),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        TextButton(onClick = { minute = (minute + 1) % 60 }) { Text("▲", color = CodexraSuccessGreen) }
                        Text(
                            text = String.format("%02d", minute),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        TextButton(onClick = { minute = if (minute == 0) 59 else minute - 1 }) { Text("▼", color = CodexraSuccessGreen) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Routine Name (e.g. Fajr, Study Session)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description or Checklist (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Category", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = category == cat
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CodexraSuccessGreen else Color(0xFF1E293B),
                        onClick = { category = cat }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val toSave = (initialRoutine ?: RoutineEntity(
                        name = name.ifBlank { "Routine" },
                        hour = hour,
                        minute = minute
                    )).copy(
                        name = name.ifBlank { "Routine" },
                        description = description,
                        category = category,
                        hour = hour,
                        minute = minute,
                        isEnabled = true
                    )
                    onSave(toSave)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_routine_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CodexraSuccessGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (initialRoutine == null) "SAVE ROUTINE" else "UPDATE ROUTINE",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
