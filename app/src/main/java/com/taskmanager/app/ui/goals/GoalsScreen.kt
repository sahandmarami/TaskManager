package com.taskmanager.app.ui.goals

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.taskmanager.app.calendar.JalaliCalendar
import com.taskmanager.app.calendar.toPersianDigits
import com.taskmanager.app.di.AppContainer
import com.taskmanager.app.ui.components.AppCard
import com.taskmanager.app.ui.components.ConfirmDialog
import com.taskmanager.app.ui.components.JalaliDatePickerDialog
import com.taskmanager.app.ui.components.SectionTitle
import com.taskmanager.app.ui.components.TaskCard
import com.taskmanager.app.ui.components.formatMinutes
import com.taskmanager.app.ui.theme.extras

@Composable
fun GoalsScreen(
    container: AppContainer,
    onOpenGoal: (Long) -> Unit,
) {
    val vm: GoalsViewModel = viewModel(factory = viewModelFactory {
        initializer { GoalsViewModel(container) }
    })
    val state by vm.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Spacer(Modifier.height(4.dp)) }
        item {
            Text(
                "اهداف",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
            )
        }

        if (state.goals.isEmpty()) {
            item {
                AppCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            androidx.compose.ui.res.painterResource(com.taskmanager.app.R.drawable.ic_flag),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "هنوز هدفی نساخته‌ای",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "با دکمه + هدف جدیدی بساز و مراحلش را تعریف کن",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }
        } else {
            items(state.goals.size) { i ->
                val g = state.goals[i]
                GoalCard(g, onClick = { onOpenGoal(g.goal.id) }, onDelete = { vm.deleteGoal(g.goal) })
            }
        }
        item { Spacer(Modifier.height(90.dp)) }
        }

        androidx.compose.material3.FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = androidx.compose.ui.graphics.Color.White,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "هدف جدید")
        }
    }

    if (showAddDialog) {
        AddGoalDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, desc, start, end ->
                vm.addGoal(title, desc, start, end) { showAddDialog = false }
            },
        )
    }
}

@Composable
private fun GoalCard(g: GoalCardData, onClick: () -> Unit, onDelete: () -> Unit) {
    AppCard(modifier = Modifier.clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        androidx.compose.ui.res.painterResource(com.taskmanager.app.R.drawable.ic_flag),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    g.goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, "حذف هدف", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (g.goal.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    g.goal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "پیشرفت",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "${(g.progress * 100).toInt().toPersianDigits()}٪",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { g.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${g.steps.count { it.isDone }.toPersianDigits()} از ${g.steps.size.toPersianDigits()} مرحله · " +
                    "${g.tasks.count { it.item.task.isCompleted }.toPersianDigits()} از ${g.tasks.size.toPersianDigits()} وظیفه" +
                    if (g.goal.dueAtMillis != null) " · پایان: ${formatGoalDate(g.goal.dueAtMillis)}" else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Long?, Long?) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var hasStart by remember { mutableStateOf(false) }
    var startJ by remember { mutableStateOf(JalaliCalendar.JalaliDate(0, 0, 0)) }
    var hasEnd by remember { mutableStateOf(false) }
    var endJ by remember { mutableStateOf(JalaliCalendar.JalaliDate(0, 0, 0)) }
    var pickStart by remember { mutableStateOf(false) }
    var pickEnd by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("هدف جدید", style = MaterialTheme.typography.titleMedium) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("مثلاً: قبولی در امتحان") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = dialogFieldColors(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    placeholder = { Text("توضیح (اختیاری)") },
                    minLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = dialogFieldColors(),
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = { pickStart = true }) {
                        Text(
                            if (hasStart) "شروع: ${com.taskmanager.app.calendar.PersianCalendar.formatJalali(startJ)}"
                            else "تاریخ شروع",
                            fontSize = 13.sp,
                        )
                    }
                    TextButton(onClick = { pickEnd = true }) {
                        Text(
                            if (hasEnd) "پایان: ${com.taskmanager.app.calendar.PersianCalendar.formatJalali(endJ)}"
                            else "تاریخ پایان",
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        title,
                        desc,
                        if (hasStart) com.taskmanager.app.calendar.PersianCalendar.startOfDayMillis(startJ) else null,
                        if (hasEnd) com.taskmanager.app.calendar.PersianCalendar.endOfDayMillis(endJ) else null,
                    )
                },
                enabled = title.isNotBlank(),
            ) {
                Text("ذخیره", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("انصراف", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        },
        containerColor = MaterialTheme.colorScheme.surface,
    )

    if (pickStart) {
        JalaliDatePickerDialog(
            initial = if (hasStart) startJ else null,
            onDismiss = { pickStart = false },
            onConfirm = { j -> startJ = j; hasStart = true; pickStart = false },
        )
    }
    if (pickEnd) {
        JalaliDatePickerDialog(
            initial = if (hasEnd) endJ else null,
            onDismiss = { pickEnd = false },
            onConfirm = { j -> endJ = j; hasEnd = true; pickEnd = false },
        )
    }
}

@Composable
private fun dialogFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
)
