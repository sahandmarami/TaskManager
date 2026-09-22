package com.taskmanager.app.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taskmanager.app.data.settings.AppSettings
import com.taskmanager.app.di.AppContainer
import com.taskmanager.app.domain.model.ThemeMode
import com.taskmanager.app.ui.components.AppCard
import com.taskmanager.app.ui.components.SectionTitle
import com.taskmanager.app.ui.theme.CategoryColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    container: AppContainer,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by container.settingsRepository.settings.collectAsState(initial = AppSettings())
    val categories by container.categoryRepository.observeAll().collectAsState(initial = emptyList())

    var showAddCategory by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var pendingImportJson by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val json = container.backupManager.exportJson()
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(json.toByteArray(Charsets.UTF_8))
                    }
                    withContext(Dispatchers.Main) { message = "پشتیبان با موفقیت ذخیره شد" }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { message = "خطا در ذخیره پشتیبان: ${e.message}" }
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val text = context.contentResolver.openInputStream(uri)?.use { ins ->
                        ins.readBytes().toString(Charsets.UTF_8)
                    }
                    withContext(Dispatchers.Main) { pendingImportJson = text }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { message = "خطا در خواندن فایل: ${e.message}" }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                "تنظیمات",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        Spacer(Modifier.height(8.dp))

        // ---- ظاهر ----
        SectionTitle("ظاهر")
        Spacer(Modifier.height(8.dp))
        AppCard {
            Column(Modifier.padding(16.dp)) {
                Text("حالت برنامه", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(10.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = settings.themeMode == mode,
                            onClick = { scope.launch { container.settingsRepository.setThemeMode(mode) } },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeMode.entries.size),
                        ) {
                            Text(mode.label)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---- یادآوری‌ها ----
        SectionTitle("یادآوری‌ها")
        Spacer(Modifier.height(8.dp))
        AppCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SettingSwitch(
                    title = "یادآوری وظایف",
                    subtitle = "آلارم دقیق برای وظایف دارای ساعت",
                    checked = settings.remindersEnabled,
                    onChange = { scope.launch { container.settingsRepository.setRemindersEnabled(it) } },
                )
                SettingSwitch(
                    title = "صدای آلارم",
                    subtitle = "پخش صدا هنگام یادآوری",
                    checked = settings.alarmSound,
                    onChange = { scope.launch { container.settingsRepository.setAlarmSound(it) } },
                )
                SettingSwitch(
                    title = "ویبره",
                    subtitle = "لرزش گوشی هنگام یادآوری",
                    checked = settings.vibration,
                    onChange = { scope.launch { container.settingsRepository.setVibration(it) } },
                )
                Text(
                    "برای کنترل صدا از تنظیمات اعلان سیستم هم می‌توانی استفاده کنی (کانال: یادآوری وظایف).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---- دسته‌بندی‌ها ----
        SectionTitle("دسته‌بندی‌ها")
        Spacer(Modifier.height(8.dp))
        AppCard {
            Column(Modifier.padding(16.dp)) {
                categories.forEach { c ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    CategoryColors[c.colorIndex % CategoryColors.size],
                                    CircleShape
                                )
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            c.name,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        IconButton(onClick = {
                            scope.launch(Dispatchers.IO) {
                                container.categoryRepository.delete(c)
                            }
                        }) {
                            Icon(Icons.Filled.Delete, "حذف دسته", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                OutlinedButton(
                    onClick = { showAddCategory = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("دسته‌بندی جدید")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---- پشتیبان‌گیری ----
        SectionTitle("پشتیبان‌گیری")
        Spacer(Modifier.height(8.dp))
        AppCard {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "خروجی تمام وظایف، اهداف، دسته‌بندی‌ها و تنظیمات در یک فایل JSON — و بازگردانی آن.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { exportLauncher.launch("task-manager-backup.json") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    ) { Text("خروجی (Backup)") }
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                        modifier = Modifier.weight(1f),
                    ) { Text("بازگردانی (Restore)") }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---- درباره ----
        SectionTitle("درباره برنامه")
        Spacer(Modifier.height(8.dp))
        AppCard {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Task Manager",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "نسخه ۱.۰.۰",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "برنامه مدیریت وظایف و برنامه‌ریزی شخصی، کاملاً آفلاین، با تقویم شمسی دقیق، مناسبت‌های رسمی ایران، آلارم واقعی، اهداف و حالت تمرکز. ساخته‌شده با Kotlin و Jetpack Compose.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    message?.let {
        AlertDialog(
            onDismissRequest = { message = null },
            title = { Text("اطلاع", style = MaterialTheme.typography.titleMedium) },
            text = { Text(it, style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { message = null }) { Text("باشه") }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }

    pendingImportJson?.let { json ->
        AlertDialog(
            onDismissRequest = { pendingImportJson = null },
            title = { Text("بازگردانی پشتیبان", style = MaterialTheme.typography.titleMedium) },
            text = {
                Text(
                    "تمام اطلاعات فعلی با محتوای فایل پشتیبان جایگزین می‌شود. ادامه می‌دهی؟",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val data = json
                    pendingImportJson = null
                    scope.launch(Dispatchers.IO) {
                        try {
                            val count = container.backupManager.importJson(data)
                            withContext(Dispatchers.Main) { message = "$count وظیفه با موفقیت بازگردانی شد" }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) { message = "فایل پشتیبان معتبر نیست: ${e.message}" }
                        }
                    }
                }) {
                    Text("بازگردانی", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportJson = null }) { Text("انصراف") }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }

    if (showAddCategory) {
        AlertDialog(
            onDismissRequest = { showAddCategory = false },
            title = { Text("دسته‌بندی جدید", style = MaterialTheme.typography.titleMedium) },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    placeholder = { Text("نام دسته‌بندی") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
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
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = newCategoryName.trim()
                        if (name.isNotEmpty()) {
                            scope.launch(Dispatchers.IO) {
                                container.categoryRepository.add(name, categories.size % CategoryColors.size)
                            }
                        }
                        newCategoryName = ""
                        showAddCategory = false
                    },
                    enabled = newCategoryName.isNotBlank(),
                ) {
                    Text("افزودن", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategory = false; newCategoryName = "" }) {
                    Text("انصراف", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
        )
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary),
        )
    }
}
