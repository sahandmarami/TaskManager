package com.taskmanager.app.ui.tasks

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.taskmanager.app.calendar.PersianCalendar
import com.taskmanager.app.calendar.toPersianDigits
import com.taskmanager.app.di.AppContainer
import com.taskmanager.app.domain.model.TaskFilter
import com.taskmanager.app.ui.components.EmptyState
import com.taskmanager.app.ui.components.TaskCard

@Composable
fun TasksScreen(
    container: AppContainer,
    onOpenTask: (Long) -> Unit,
) {
    val vm: TasksViewModel = viewModel(factory = viewModelFactory {
        initializer { TasksViewModel(container) }
    })
    val state by vm.state.collectAsState()
    val query by vm.query.collectAsState()
    val selectedFilter by vm.filter.collectAsState()
    val todayStart = remember { PersianCalendar.startOfDayMillis(PersianCalendar.todayJalali()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { vm.setQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("جستجوی وظایف…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TaskFilter.entries.forEach { f ->
                val count = state.counts[f] ?: 0
                FilterChip(
                    selected = selectedFilter == f,
                    onClick = { vm.setFilter(f) },
                    label = {
                        Text(if (count > 0) "${f.label} (${count.toPersianDigits()})" else f.label)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        if (state.tasks.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Search,
                title = "وظیفه‌ای پیدا نشد",
                subtitle = "فیلتر دیگری را امتحان کن یا وظیفه جدیدی اضافه کن",
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.tasks, key = { it.item.task.id }) { data ->
                    TaskCard(
                        data = data,
                        onClick = { onOpenTask(data.item.task.id) },
                        onToggleComplete = {
                            vm.setCompleted(data.item.task.id, !data.item.task.isCompleted)
                        },
                        todayStartMillis = todayStart,
                    )
                }
                item { Spacer(Modifier.height(90.dp)) }
            }
        }
    }
}
