package com.taskmanager.app.ui.taskedit

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.taskmanager.app.calendar.JalaliCalendar
import com.taskmanager.app.calendar.PersianCalendar
import com.taskmanager.app.di.AppContainer
import com.taskmanager.app.domain.model.Priority
import com.taskmanager.app.ui.components.AppCard
import com.taskmanager.app.ui.components.ConfirmDialog
import com.taskmanager.app.ui.components.JalaliDatePickerDialog
import com.taskmanager.app.ui.components.SectionTitle
import com.taskmanager.app.ui.components.priorityColor
import com.taskmanager.app.ui.theme.CategoryColors

@Composable
fun TaskEditScreen(
    container: AppContainer,
    taskId: Long,
    prefillDateMillis: Long,
    onBack: () -> Unit,
) {
    val vm: TaskEditViewModel = viewModel(factory = viewModelFactory {
        initializer {
            TaskEditViewModel(
                container = container,
                savedStateHandle = SavedStateHandle(mapOf("taskId" to taskId)),
                prefillDateMillis = prefillDateMillis,
            )
        }
    })
    val state by vm.state.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                if (state.isEdit) "ویرایش وظیفه" else "افزودن وظیفه",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(Modifier.height(16.dp))

        // ----- عنوان وظیفه -----
        Text("عنوان وظیفه", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.title,
            onValueChange = { vm.update { s -> s.copy(title = it) } },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("مثلاً: خرید نان", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = fieldColors(),
        )

        Spacer(Modifier.height(16.dp))

        // ----- توضیحات -----
        Text("توضیحات", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = state.description,
            onValueChange = { vm.update { s -> s.copy(description = it) } },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("توضیحات اختیاری", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            minLines = 2,
            shape = MaterialTheme.shapes.medium,
            colors = fieldColors(),
        )

        Spacer(Modifier.height(16.dp))

        // ----- کارت تاریخ و زمان -----
        AppCard {
            Column(Modifier.padding(16.dp)) {
                Text("تاریخ و زمان", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (state.hasDate) PersianCalendar.formatJalali(
                            JalaliCalendar.JalaliDate(state.jYear, state.jMonth, state.jDay)
                        ) else "بدون تاریخ",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (state.hasDate) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (state.hasDate) {
                        IconButton(onClick = { vm.update { s -> s.copy(hasDate = false, hasTime = false) } }) {
                            Icon(Icons.Filled.Close, "حذف تاریخ", tint = MaterialTheme.colorScheme.error)
                        }
                        OutlinedButton(onClick = { showDatePicker = true }) { Text("تغییر تاریخ") }
                    } else {
                        OutlinedButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Filled.DateRange, contentDescription = null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("انتخاب تاریخ")
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (state.hasTime) PersianCalendar.formatTime(state.hour, state.minute) else "بدون ساعت",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (state.hasTime) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (state.hasTime) {
                        IconButton(onClick = { vm.update { s -> s.copy(hasTime = false) } }) {
                            Icon(Icons.Filled.Close, "حذف ساعت", tint = MaterialTheme.colorScheme.error)
                        }
                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            enabled = state.hasDate,
                        ) { Text("تغییر ساعت") }
                    } else {
                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            enabled = state.hasDate,
                        ) { Text("انتخاب ساعت") }
                    }
                }
                if (!state.hasDate) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "برای انتخاب ساعت، ابتدا تاریخ را تعیین کنید",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ----- اولویت -----
        Text("اولویت", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Priority.entries.forEach { p ->
                val selected = state.priority == p
                FilterChip(
                    selected = selected,
                    onClick = { vm.update { s -> s.copy(priority = p) } },
                    label = { Text(p.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = priorityColor(p),
                        selectedLabelColor = Color.White,
                    ),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ----- دسته‌بندی -----
        Text("دسته‌بندی", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.categories.forEach { c ->
                val selected = state.categoryId == c.id
                FilterChip(
                    selected = selected,
                    onClick = {
                        vm.update { s -> s.copy(categoryId = if (selected) null else c.id) }
                    },
                    label = { Text(c.name) },
                    leadingIcon = {
                        Box(
                            Modifier
                                .size(10.dp)
                                .background(
                                    CategoryColors[c.colorIndex % CategoryColors.size],
                                    CircleShape,
                                )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ----- اتصال به هدف -----
        if (state.goals.isNotEmpty()) {
            Text("اتصال به هدف", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.goals.forEach { g ->
                    val selected = state.goalId == g.id
                    FilterChip(
                        selected = selected,
                        onClick = { vm.update { s -> s.copy(goalId = if (selected) null else g.id) } },
                        label = { Text(g.title, maxLines = 1) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ----- یادآوری -----
        if (state.hasDate && state.hasTime) {
            AppCard {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("یادآوری", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "آلارم دقیق گوشی در زمان تعیین‌شده",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = state.reminderEnabled,
                            onCheckedChange = {
                                vm.update { s -> s.copy(reminderEnabled = it) }
                                if (it) vm.checkExactAlarm()
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
                        )
                    }
                    if (state.reminderEnabled) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(
                                0 to "سر وقت",
                                5 to "۵ دقیقه قبل",
                                15 to "۱۵ دقیقه قبل",
                                30 to "۳۰ دقیقه قبل",
                            ).forEach { (m, label) ->
                                FilterChip(
                                    selected = state.reminderMinutesBefore == m,
                                    onClick = { vm.update { s -> s.copy(reminderMinutesBefore = m) } },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurface,
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ----- زیرکارها -----
        SectionTitle("زیرکارها")
        Spacer(Modifier.height(8.dp))
        state.subtasks.forEachIndexed { index, st ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    st.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IconButton(onClick = {
                    vm.setSubtasks(state.subtasks.filterIndexed { i, _ -> i != index })
                }) {
                    Icon(Icons.Filled.Close, "حذف زیرکار", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        var newSubtask by remember { mutableStateOf("") }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newSubtask,
                onValueChange = { newSubtask = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("زیرکار جدید…", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = fieldColors(),
            )
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (newSubtask.isNotBlank()) {
                        vm.setSubtasks(state.subtasks + SubTaskDraft(title = newSubtask.trim()))
                        newSubtask = ""
                    }
                }
            ) {
                Icon(Icons.Filled.Add, "افزودن زیرکار", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(24.dp))

        // ----- ذخیره وظیفه -----
        Button(
            onClick = { vm.save() },
            enabled = state.title.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            ),
        ) {
            Text("ذخیره وظیفه", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        if (state.isEdit) {
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { vm.requestDeleteConfirm() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text("حذف وظیفه", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    if (showDatePicker) {
        JalaliDatePickerDialog(
            initial = if (state.hasDate) JalaliCalendar.JalaliDate(state.jYear, state.jMonth, state.jDay) else null,
            onDismiss = { showDatePicker = false },
            onConfirm = { j ->
                vm.update { s -> s.copy(hasDate = true, jYear = j.year, jMonth = j.month, jDay = j.day) }
                showDatePicker = false
            },
        )
    }

    if (showTimePicker) {
        TimePickerDialogCompose(
            initialHour = state.hour,
            initialMinute = state.minute,
            onDismiss = { showTimePicker = false },
            onConfirm = { h, m ->
                vm.update { s -> s.copy(hasTime = true, hour = h, minute = m) }
                showTimePicker = false
            },
        )
    }

    if (state.showDeleteConfirm) {
        ConfirmDialog(
            title = "حذف وظیفه",
            message = "این وظیفه و زیرکارهای آن برای همیشه حذف می‌شوند. مطمئنی؟",
            onConfirm = { vm.deleteTask() },
            onDismiss = { vm.dismissDeleteConfirm() },
        )
    }

    if (state.showExactAlarmDialog) {
        AlertDialog(
            onDismissRequest = { vm.dismissExactAlarmDialog() },
            title = { Text("مجوز آلارم دقیق", style = MaterialTheme.typography.titleMedium) },
            text = {
                Text(
                    "برای اینکه یادآوری‌ها دقیقاً در زمان مقرر پخش شوند، باید مجوز «آلارم و یادآوری» را در تنظیمات اندروید فعال کنی.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(onClick = { vm.openExactAlarmSettings(context) }) {
                    Text("فعال‌سازی", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.dismissExactAlarmDialog() }) {
                    Text("بعداً", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogCompose(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val timeState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true,
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("انتخاب ساعت", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(12.dp))
            TimePicker(state = timeState)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = onDismiss) {
                    Text("انصراف", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = { onConfirm(timeState.hour, timeState.minute) }) {
                    Text("تأیید", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
)
