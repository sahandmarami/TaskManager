package com.taskmanager.app.ui.focus

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taskmanager.app.calendar.toPersianDigits
import com.taskmanager.app.di.AppContainer
import com.taskmanager.app.focus.FocusEngine
import com.taskmanager.app.ui.components.AppCard
import com.taskmanager.app.ui.components.DotChip
import com.taskmanager.app.ui.components.StepperRow
import com.taskmanager.app.ui.components.formatMinutes
import com.taskmanager.app.ui.theme.extras

@OptIn(ExperimentalTextApi::class)
@Composable
fun FocusScreen(
    container: AppContainer,
    initialTaskId: Long,
    tasks: List<Pair<Long, String>>, // (id, title) incomplete tasks
    taskGoalLabel: (Long) -> String?,
) {
    val engine = container.focusEngine
    val state by engine.state.collectAsState()
    val theme = extras()
    val context = LocalContext.current

    var selectedTaskId by remember { mutableStateOf<Long?>(if (initialTaskId > 0) initialTaskId else null) }
    var dropdownOpen by remember { mutableStateOf(false) }

    // Apply navigation-provided task once
    LaunchedEffect(initialTaskId) {
        if (initialTaskId > 0) selectedTaskId = initialTaskId
    }

    // Beep + vibrate when a session finishes
    LaunchedEffect(state.sessionJustFinished) {
        if (state.sessionJustFinished > 0) {
            try {
                val tone = ToneGenerator(AudioManager.STREAM_ALARM, 90)
                tone.startTone(ToneGenerator.TONE_PROP_BEEP2, 1200)
            } catch (_: Exception) {
            }
            try {
                val vibrator = context.getSystemService(android.os.Vibrator::class.java)
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1))
            } catch (_: Exception) {
            }
            engine.clearFinishedFlag()
        }
    }

    val totalSec = (if (state.mode == FocusEngine.Mode.FOCUS) state.focusMinutes else state.breakMinutes) * 60
    val progress = if (totalSec > 0) 1f - state.remainingSec.toFloat() / totalSec else 0f
    val minutes = state.remainingSec / 60
    val seconds = state.remainingSec % 60
    val modeLabel = if (state.mode == FocusEngine.Mode.FOCUS) "زمان تمرکز" else "زمان استراحت"
    val modeColor = if (state.mode == FocusEngine.Mode.FOCUS) MaterialTheme.colorScheme.primary else theme.success
    val selectedTaskTitle = selectedTaskId?.let { id -> tasks.firstOrNull { it.first == id }?.second }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(4.dp))
        Text(
            "تمرکز",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            textAlign = TextAlign.Start,
        )

        // Task selector
        AppCard {
            Column(Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        selectedTaskTitle ?: "بدون وظیفه مشخص",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selectedTaskTitle != null) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    Box {
                        OutlinedButton(onClick = { dropdownOpen = !dropdownOpen }, enabled = !state.running) {
                            Text("انتخاب")
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = dropdownOpen,
                            onDismissRequest = { dropdownOpen = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("بدون وظیفه") },
                                onClick = { selectedTaskId = null; dropdownOpen = false },
                            )
                            tasks.forEach { (id, title) ->
                                DropdownMenuItem(
                                    text = { Text(title, maxLines = 1) },
                                    onClick = { selectedTaskId = id; dropdownOpen = false },
                                )
                            }
                        }
                    }
                }
                selectedTaskId?.let { id ->
                    taskGoalLabel(id)?.let { goalTitle ->
                        Spacer(Modifier.height(6.dp))
                        DotChip(
                            text = "هدف: $goalTitle",
                            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            modeLabel,
            style = MaterialTheme.typography.titleMedium,
            color = modeColor,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))

        // Big timer
        Text(
            text = "%02d:%02d".format(minutes, seconds),
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            style = TextStyle(fontFeatureSettings = "tnum"),
        )

        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            color = modeColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        // Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                !state.running && state.remainingSec == totalSec -> {
                    Button(
                        onClick = { engine.start() },
                        modifier = Modifier.size(width = 160.dp, height = 52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    ) { Text("شروع", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                }
                state.running -> {
                    OutlinedButton(
                        onClick = { engine.pause() },
                        modifier = Modifier.size(width = 120.dp, height = 52.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) { Text("توقف", fontSize = 15.sp) }
                    Button(
                        onClick = { engine.finishSession() },
                        modifier = Modifier.size(width = 120.dp, height = 52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = Color.White,
                        ),
                    ) { Text("پایان", fontSize = 15.sp) }
                }
                else -> { // paused
                    Button(
                        onClick = { engine.start() },
                        modifier = Modifier.size(width = 120.dp, height = 52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    ) { Text("ادامه", fontSize = 15.sp) }
                    OutlinedButton(
                        onClick = { engine.finishSession() },
                        modifier = Modifier.size(width = 120.dp, height = 52.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) { Text("پایان", fontSize = 15.sp) }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        DotChip(
            text = "جلسات کامل‌شده: ${state.sessionsCompleted.toPersianDigits()}",
            textColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        // Durations (editable only when idle)
        AppCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("مدت زمان‌ها", style = MaterialTheme.typography.titleMedium)
                StepperRow(
                    label = "تمرکز",
                    valueText = formatMinutes(state.focusMinutes),
                    onDecrease = { engine.setFocusMinutes(state.focusMinutes - 5) },
                    onIncrease = { engine.setFocusMinutes(state.focusMinutes + 5) },
                )
                StepperRow(
                    label = "استراحت",
                    valueText = formatMinutes(state.breakMinutes),
                    onDecrease = { engine.setBreakMinutes(state.breakMinutes - 1) },
                    onIncrease = { engine.setBreakMinutes(state.breakMinutes + 1) },
                )
            }
        }

        Spacer(Modifier.height(90.dp))
    }
}
