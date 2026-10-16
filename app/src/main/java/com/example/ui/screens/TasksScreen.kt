package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.TaskAlt
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TaskEntity
import com.example.ui.CodexraViewModel
import com.example.ui.theme.CodexraAmberAccent
import com.example.ui.theme.CodexraCrimsonAlert
import com.example.ui.theme.CodexraDarkSurface
import com.example.ui.theme.CodexraPrimaryCyan
import com.example.ui.theme.CodexraSuccessGreen
import com.example.ui.theme.CodexraTextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: CodexraViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showCreateSheet by remember { mutableStateOf(false) }
    var selectedTaskForEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }

    val filterOptions = listOf("ALL", "PENDING", "COMPLETED", "HIGH PRIORITY")

    val filteredTasks = remember(tasks, selectedFilter) {
        when (selectedFilter) {
            "PENDING" -> tasks.filter { it.status == "PENDING" }
            "COMPLETED" -> tasks.filter { it.status == "COMPLETED" }
            "HIGH PRIORITY" -> tasks.filter { it.priority == "HIGH" }
            else -> tasks
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedTaskForEdit = null
                    showCreateSheet = true
                },
                containerColor = Color(0xFFA855F7),
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("add_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
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
                text = "TASK MANAGER",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = Color(0xFFA855F7)
                )
            )
            Text(
                text = "Track critical goals, priorities, and deadlines",
                style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                items(filterOptions) { filter ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFFA855F7) else CodexraDarkSurface,
                        onClick = { selectedFilter = filter }
                    ) {
                        Text(
                            text = filter,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else CodexraTextSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = CodexraTextSecondary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Tasks Found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Tap + to add a prioritized task or goal",
                            style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onToggleComplete = {
                                val newStatus = if (task.status == "COMPLETED") "PENDING" else "COMPLETED"
                                viewModel.setTaskStatus(task.id, newStatus)
                            },
                            onEdit = {
                                selectedTaskForEdit = task
                                showCreateSheet = true
                            },
                            onDelete = { taskToDelete = task }
                        )
                    }
                }
            }
        }
    }

    if (showCreateSheet) {
        TaskEditSheet(
            initialTask = selectedTaskForEdit,
            onDismiss = { showCreateSheet = false },
            onSave = { task ->
                viewModel.saveTask(task)
                showCreateSheet = false
            }
        )
    }

    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Delete Task?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${taskToDelete?.title}'?", color = CodexraTextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        taskToDelete?.let { viewModel.deleteTask(it) }
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CodexraCrimsonAlert)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Cancel", color = CodexraPrimaryCyan)
                }
            },
            containerColor = CodexraDarkSurface
        )
    }
}

@Composable
fun TaskCard(
    task: TaskEntity,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = task.status == "COMPLETED"
    val dateText = remember(task.dueDate) {
        val dt = Instant.ofEpochMilli(task.dueDate).atZone(ZoneId.systemDefault()).toLocalDate()
        dt.format(DateTimeFormatter.ofPattern("MMM d"))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) Color(0xFF0F241A) else CodexraDarkSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleComplete, modifier = Modifier.size(36.dp)) {
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
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (isCompleted) CodexraTextSecondary else Color.White
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Due $dateText • ${task.category}",
                                style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (task.priority) {
                        "HIGH" -> CodexraCrimsonAlert.copy(alpha = 0.2f)
                        "LOW" -> CodexraSuccessGreen.copy(alpha = 0.2f)
                        else -> CodexraAmberAccent.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = task.priority,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (task.priority) {
                            "HIGH" -> CodexraCrimsonAlert
                            "LOW" -> CodexraSuccessGreen
                            else -> CodexraAmberAccent
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = CodexraTextSecondary),
                    modifier = Modifier.padding(start = 46.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
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
fun TaskEditSheet(
    initialTask: TaskEntity?,
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var priority by remember { mutableStateOf(initialTask?.priority ?: "MEDIUM") }
    var category by remember { mutableStateOf(initialTask?.category ?: "GENERAL") }

    val priorities = listOf("LOW", "MEDIUM", "HIGH")
    val categories = listOf("WORK", "STUDY", "HEALTH", "PERSONAL", "GENERAL")

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
                text = if (initialTask == null) "NEW TASK" else "EDIT TASK",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_title_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Notes & Details (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Priority", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                priorities.forEach { p ->
                    val isSelected = priority == p
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) when (p) {
                            "HIGH" -> CodexraCrimsonAlert
                            "LOW" -> CodexraSuccessGreen
                            else -> CodexraAmberAccent
                        } else Color(0xFF1E293B),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { priority = p }
                    ) {
                        Text(
                            text = p,
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

            Text("Category", color = Color.White, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    val isSelected = category == cat
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFFA855F7) else Color(0xFF1E293B),
                        onClick = { category = cat }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else CodexraTextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val toSave = (initialTask ?: TaskEntity(
                        title = title.ifBlank { "Task" },
                        dueDate = System.currentTimeMillis()
                    )).copy(
                        title = title.ifBlank { "Task" },
                        description = description,
                        priority = priority,
                        category = category,
                        dueDate = System.currentTimeMillis()
                    )
                    onSave(toSave)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_task_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (initialTask == null) "CREATE TASK" else "UPDATE TASK",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
