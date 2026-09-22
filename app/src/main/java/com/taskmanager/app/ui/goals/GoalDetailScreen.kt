package com.taskmanager.app.ui.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.taskmanager.app.R
import com.taskmanager.app.calendar.toPersianDigits
import com.taskmanager.app.di.AppContainer
import com.taskmanager.app.ui.components.AppCard
import com.taskmanager.app.ui.components.ConfirmDialog
import com.taskmanager.app.ui.components.SectionTitle
import com.taskmanager.app.ui.theme.extras

@Composable
fun GoalDetailScreen(
    container: AppContainer,
    goalId: Long,
    onBack: () -> Unit,
    onOpenTask: (Long) -> Unit,
    onStartFocus: (Long) -> Unit,
) {
    val vm: GoalDetailViewModel = viewModel(factory = viewModelFactory {
        initializer {
            GoalDetailViewModel(container, SavedStateHandle(mapOf("goalId" to goalId)))
        }
    })
    val state by vm.state.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var newStep by remember { mutableStateOf("") }
    val theme = extras()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                "جزئیات هدف",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        val data = state.data
        if (state.loading || data == null) {
            Spacer(Modifier.height(24.dp))
            Text(
                "در حال بارگذاری…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Spacer(Modifier.height(8.dp))

            // Header card
            AppCard {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_flag),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            data.goal.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (data.goal.description.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            data.goal.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "تاریخ شروع: ${formatGoalDate(data.goal.startAtMillis)} · پایان: ${formatGoalDate(data.goal.dueAtMillis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
                            "${(data.progress * 100).toInt().toPersianDigits()}٪",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { data.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Steps
            SectionTitle("مراحل هدف")
            Spacer(Modifier.height(8.dp))
            if (data.steps.isEmpty()) {
                Text(
                    "برای این هدف مرحله‌ای تعریف نشده است",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                data.steps.forEach { step ->
                    AppCard(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(
                                        if (step.isDone) theme.success else Color.Transparent,
                                        CircleShape
                                    )
                                    .border(
                                        2.dp,
                                        if (step.isDone) theme.success else MaterialTheme.colorScheme.outline,
                                        CircleShape
                                    )
                                    .clickable { vm.toggleStep(step) },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (step.isDone) {
                                    Icon(
                                        Icons.Filled.Done,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                step.title,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (step.isDone) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface,
                                textDecoration = if (step.isDone) TextDecoration.LineThrough else null,
                            )
                            IconButton(onClick = { vm.deleteStep(step) }) {
                                Icon(
                                    Icons.Filled.Close, "حذف مرحله",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newStep,
                    onValueChange = { newStep = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("مرحله جدید…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = detailFieldColors(),
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        vm.addStep(newStep)
                        newStep = ""
                    }
                ) {
                    Icon(Icons.Filled.Add, "افزودن مرحله", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Linked tasks
            SectionTitle("وظایف متصل")
            Spacer(Modifier.height(8.dp))
            if (data.tasks.isEmpty()) {
                Text(
                    "هیچ وظیفه‌ای به این هدف متصل نیست. از فرم وظیفه، بخش «اتصال به هدف» استفاده کن.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                data.tasks.forEach { taskData ->
                    val t = taskData.item.task
                    AppCard(modifier = Modifier.padding(vertical = 4.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .background(
                                            if (t.isCompleted) theme.success else Color.Transparent,
                                            CircleShape
                                        )
                                        .border(
                                            2.dp,
                                            if (t.isCompleted) theme.success else MaterialTheme.colorScheme.outline,
                                            CircleShape
                                        )
                                        .clickable { vm.toggleTask(t.id, !t.isCompleted) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (t.isCompleted) {
                                        Icon(
                                            Icons.Filled.Done,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    t.title,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onOpenTask(t.id) },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (t.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (t.isCompleted) TextDecoration.LineThrough else null,
                                )
                            }
                            if (!t.isCompleted) {
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { onStartFocus(t.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White,
                                    ),
                                ) {
                                    Icon(
                                    androidx.compose.ui.res.painterResource(R.drawable.ic_timer),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                                    Spacer(Modifier.width(6.dp))
                                    Text("شروع تمرکز", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { showDeleteConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("حذف هدف", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "حذف هدف",
            message = "هدف و مراحل آن حذف می‌شوند و اتصال وظایف به این هدف برداشته می‌شود. وظایف خودشان حذف نمی‌شوند. مطمئنی؟",
            onConfirm = { vm.deleteGoal { onBack() } },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

@Composable
private fun detailFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
)
