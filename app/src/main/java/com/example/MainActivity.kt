package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CodexraViewModel
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.FocusTimerScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.RoutinesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.CodexraAmberAccent
import com.example.ui.theme.CodexraDarkBackground
import com.example.ui.theme.CodexraDarkSurface
import com.example.ui.theme.CodexraPrimaryCyan
import com.example.ui.theme.CodexraTextSecondary
import com.example.ui.theme.MyApplicationTheme

enum class CodexraTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    ALARMS("Alarms", Icons.Default.Alarm),
    NOTIFICATIONS("Alerts", Icons.Default.Notifications),
    ROUTINES("Routines", Icons.Default.Repeat),
    TASKS("Tasks", Icons.Default.TaskAlt),
    FOCUS("Focus", Icons.Default.PlayArrow),
    HISTORY("History", Icons.Default.History),
    SETTINGS("Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: CodexraViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                val context = LocalContext.current
                val currentTabStr by viewModel.currentTab.collectAsStateWithLifecycle()

                // Runtime permission request for Android 13+
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // Notification permission handled
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPerm = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!hasPerm) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // BackHandler: Return to Home if on subscreen
                BackHandler(enabled = currentTabStr != CodexraTab.HOME.name) {
                    viewModel.navigateTo(CodexraTab.HOME.name)
                }

                Scaffold(
                    contentWindowInsets = WindowInsets.systemBars,
                    containerColor = CodexraDarkBackground,
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
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
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = CodexraAmberAccent,
                                            fontSize = 10.sp
                                        )
                                    )
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = { viewModel.navigateTo(CodexraTab.FOCUS.name) },
                                    modifier = Modifier.testTag("topbar_focus_timer_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Focus Timer",
                                        tint = if (currentTabStr == CodexraTab.FOCUS.name) Color(0xFFA855F7) else CodexraTextSecondary
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.navigateTo(CodexraTab.SETTINGS.name) },
                                    modifier = Modifier.testTag("topbar_settings_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = if (currentTabStr == CodexraTab.SETTINGS.name) CodexraPrimaryCyan else CodexraTextSecondary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = CodexraDarkBackground
                            )
                        )
                    },
                    bottomBar = {
                        CodexraBottomNav(
                            currentTab = currentTabStr,
                            onTabSelected = { viewModel.navigateTo(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTabStr) {
                            CodexraTab.HOME.name -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateTo = { viewModel.navigateTo(it) }
                            )
                            CodexraTab.ALARMS.name -> AlarmsScreen(viewModel = viewModel)
                            CodexraTab.NOTIFICATIONS.name -> NotificationsScreen(viewModel = viewModel)
                            CodexraTab.ROUTINES.name -> RoutinesScreen(viewModel = viewModel)
                            CodexraTab.TASKS.name -> TasksScreen(viewModel = viewModel)
                            CodexraTab.FOCUS.name -> FocusTimerScreen(viewModel = viewModel)
                            CodexraTab.HISTORY.name -> HistoryScreen(viewModel = viewModel)
                            CodexraTab.SETTINGS.name -> SettingsScreen(viewModel = viewModel)
                            else -> HomeScreen(
                                viewModel = viewModel,
                                onNavigateTo = { viewModel.navigateTo(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CodexraBottomNav(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    // Primary 5 tabs in bottom navigation
    val primaryTabs = listOf(
        CodexraTab.HOME,
        CodexraTab.ALARMS,
        CodexraTab.NOTIFICATIONS,
        CodexraTab.ROUTINES,
        CodexraTab.TASKS
    )

    NavigationBar(
        containerColor = CodexraDarkSurface,
        tonalElevation = 8.dp,
        modifier = Modifier.height(64.dp)
    ) {
        primaryTabs.forEach { tab ->
            val isSelected = currentTab == tab.name
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab.name) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = CodexraPrimaryCyan,
                    indicatorColor = CodexraPrimaryCyan,
                    unselectedIconColor = CodexraTextSecondary,
                    unselectedTextColor = CodexraTextSecondary
                ),
                modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
            )
        }
    }
}
