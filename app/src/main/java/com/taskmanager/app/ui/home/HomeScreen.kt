package com.taskmanager.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.taskmanager.app.TaskManagerApp
import com.taskmanager.app.calendar.PersianCalendar
import com.taskmanager.app.calendar.toPersianDigits
import com.taskmanager.app.ui.components.AppCard
import com.taskmanager.app.ui.components.EmptyState
import com.taskmanager.app.ui.components.SectionTitle
import com.taskmanager.app.ui.components.TaskCard
import com.taskmanager.app.ui.components.TaskCardData
import com.taskmanager.app.ui.theme.extras

@Composable
fun HomeScreen(
    container: com.taskmanager.app.di.AppContainer,
    onOpenTask: (Long) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val app = context.applicationContext as TaskManagerApp
    val vm: HomeViewModel = viewModel(factory = viewModelFactory {
        initializer { HomeViewModel(container) }
    })
    val state by vm.state.collectAsState()
    val today = remember { PersianCalendar.todayJalali() }
    val todayStart = remember { PersianCalendar.startOfDayMillis(today) }

    // Refresh day bounds when the screen appears (handles midnight rollover)
    LaunchedEffect(Unit) { }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "امروز",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            PersianCalendar.formatJalaliWithWeekday(today),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "تنظیمات",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // Today's progress
        item {
            AppCard {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "پیشرفت امروز",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            if (state.totalToday > 0) {
                                val pct = (state.doneToday * 100 / state.totalToday)
                                "${pct.toPersianDigits()}٪"
                            } else "—",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = {
                            if (state.totalToday > 0) state.doneToday.toFloat() / state.totalToday else 0f
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${state.doneToday.toPersianDigits()} از ${state.totalToday.toPersianDigits()} وظیفه انجام شد",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Overdue
        if (state.overdueTasks.isNotEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle("وظایف عقب‌افتاده")
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "(${state.overdueTasks.size.toPersianDigits()})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extras().danger,
                    )
                }
            }
            items(state.overdueTasks, key = { "overdue_${it.item.task.id}" }) { data ->
                TaskCard(
                    data = data,
                    onClick = { onOpenTask(data.item.task.id) },
                    onToggleComplete = { vm.setCompleted(data.item.task.id, true) },
                    todayStartMillis = todayStart,
                )
            }
        }

        // Important
        if (state.importantTasks.isNotEmpty()) {
            item { SectionTitle("وظایف مهم") }
            items(state.importantTasks, key = { "important_${it.item.task.id}" }) { data ->
                TaskCard(
                    data = data,
                    onClick = { onOpenTask(data.item.task.id) },
                    onToggleComplete = { vm.setCompleted(data.item.task.id, true) },
                    todayStartMillis = todayStart,
                )
            }
        }

        // Today's tasks
        item {
            SectionTitle("وظایف امروز")
        }
        if (state.todayTasks.isEmpty()) {
            item {
                AppCard {
                    EmptyState(
                        icon = Icons.Filled.Notifications,
                        title = "برای امروز وظیفه‌ای نداری",
                        subtitle = "با دکمه + یک وظیفه جدید اضافه کن",
                    )
                }
            }
        } else {
            items(state.todayTasks, key = { "today_${it.item.task.id}" }) { data ->
                TaskCard(
                    data = data,
                    onClick = { onOpenTask(data.item.task.id) },
                    onToggleComplete = { vm.setCompleted(data.item.task.id, !data.item.task.isCompleted) },
                    todayStartMillis = todayStart,
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}
