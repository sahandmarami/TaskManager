package com.taskmanager.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.taskmanager.app.calendar.PersianCalendar
import com.taskmanager.app.di.AppContainer
import com.taskmanager.app.ui.calendar.CalendarScreen
import com.taskmanager.app.ui.focus.FocusScreen
import com.taskmanager.app.ui.goals.GoalDetailScreen
import com.taskmanager.app.ui.goals.GoalsScreen
import com.taskmanager.app.ui.home.HomeScreen
import com.taskmanager.app.ui.settings.SettingsScreen
import com.taskmanager.app.ui.taskedit.TaskEditScreen
import com.taskmanager.app.ui.tasks.TasksScreen
import com.taskmanager.app.ui.theme.extras

object Routes {
    const val HOME = "home"
    const val TASKS = "tasks"
    const val CALENDAR = "calendar"
    const val GOALS = "goals"
    const val FOCUS = "focus?taskId={taskId}"
    const val TASK_EDIT = "task_edit?taskId={taskId}&prefillDate={prefillDate}"
    const val GOAL_DETAIL = "goal_detail/{goalId}"
    const val SETTINGS = "settings"

    fun focus(taskId: Long) = "focus?taskId=$taskId"
    fun taskEdit(taskId: Long = -1L, prefillDate: Long = -1L) =
        "task_edit?taskId=$taskId&prefillDate=$prefillDate"
    fun goalDetail(goalId: Long) = "goal_detail/$goalId"
}

private data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector?,
    val iconRes: Int?,
)

private val bottomItems = listOf(
    BottomItem(Routes.HOME, "خانه", Icons.Filled.Home, null),
    BottomItem(Routes.TASKS, "وظایف", Icons.Filled.List, null),
    BottomItem(Routes.CALENDAR, "تقویم", Icons.Filled.DateRange, null),
    BottomItem(Routes.GOALS, "اهداف", null, com.taskmanager.app.R.drawable.ic_flag),
    BottomItem(Routes.FOCUS, "تمرکز", null, com.taskmanager.app.R.drawable.ic_timer),
)

private val topLevelRoutes = setOf(Routes.HOME, Routes.TASKS, Routes.CALENDAR, Routes.GOALS, Routes.FOCUS)

@Composable
fun AppRoot(container: AppContainer) {
    val navController: NavHostController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in topLevelRoutes

    // Data shared by focus screen
    val tasks by container.taskRepository.observeAll().collectAsState(initial = emptyList())
    val goals by container.goalRepository.observeAll().collectAsState(initial = emptyList())

    val focusTasks = remember(tasks) {
        tasks.filter { !it.task.isCompleted }.map { it.task.id to it.task.title }
    }
    val goalTitles = remember(goals) { goals.associate { it.goal.id to it.goal.title } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                ) {
                    bottomItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (item.icon != null) {
                                    Icon(
                                        item.icon,
                                        contentDescription = item.label,
                                        tint = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                } else if (item.iconRes != null) {
                                    Icon(
                                        painterResource(item.iconRes),
                                        contentDescription = item.label,
                                        tint = if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            },
                            label = {
                                androidx.compose.material3.Text(
                                    item.label,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomBar && currentRoute != Routes.GOALS) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Routes.taskEdit())
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "افزودن وظیفه")
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    container = container,
                    onOpenTask = { id -> navController.navigate(Routes.taskEdit(id)) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                )
            }
            composable(Routes.TASKS) {
                TasksScreen(
                    container = container,
                    onOpenTask = { id -> navController.navigate(Routes.taskEdit(id)) },
                )
            }
            composable(Routes.CALENDAR) {
                CalendarScreen(
                    container = container,
                    onOpenTask = { id -> navController.navigate(Routes.taskEdit(id)) },
                    onAddTaskForDay = { millis -> navController.navigate(Routes.taskEdit(prefillDate = millis)) },
                )
            }
            composable(Routes.GOALS) {
                GoalsScreen(
                    container = container,
                    onOpenGoal = { id -> navController.navigate(Routes.goalDetail(id)) },
                )
            }
            composable(
                Routes.FOCUS,
                arguments = listOf(
                    navArgument("taskId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                ),
            ) { entry ->
                val taskId = entry.arguments?.getLong("taskId") ?: -1L
                FocusScreen(
                    container = container,
                    initialTaskId = taskId,
                    tasks = focusTasks,
                    taskGoalLabel = { id ->
                        val goalId = tasks.firstOrNull { it.task.id == id }?.task?.goalId
                        goalId?.let { goalTitles[it] }
                    },
                )
            }
            composable(
                Routes.TASK_EDIT,
                arguments = listOf(
                    navArgument("taskId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument("prefillDate") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) { entry ->
                val taskId = entry.arguments?.getLong("taskId") ?: -1L
                val prefillDate = entry.arguments?.getLong("prefillDate") ?: -1L
                TaskEditScreen(
                    container = container,
                    taskId = taskId,
                    prefillDateMillis = prefillDate,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                Routes.GOAL_DETAIL,
                arguments = listOf(navArgument("goalId") { type = NavType.LongType }),
            ) { entry ->
                val goalId = entry.arguments?.getLong("goalId") ?: -1L
                GoalDetailScreen(
                    container = container,
                    goalId = goalId,
                    onBack = { navController.popBackStack() },
                    onOpenTask = { id -> navController.navigate(Routes.taskEdit(id)) },
                    onStartFocus = { id -> navController.navigate(Routes.focus(id)) },
                )
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    container = container,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
