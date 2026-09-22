package com.taskmanager.app.alarm

import android.app.KeyguardManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.taskmanager.app.PersianContextWrapper
import com.taskmanager.app.R
import com.taskmanager.app.TaskManagerApp
import com.taskmanager.app.calendar.toPersianDigits
import com.taskmanager.app.ui.theme.TaskManagerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.sin

/**
 * Full-screen alarm experience: rings, vibrates, shows the task title and
 * offers "انجام شد" plus snooze options (۵/۱۰/۱۵/۳۰ دقیقه بعد).
 *
 * Opens automatically wherever the phone is — over the lock screen, above
 * other apps — because it is launched both by the direct start from
 * [AlarmReceiver] and by the system full-screen intent.
 */
class AlarmActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var taskId: Long = -1L

    private var titleState by mutableStateOf("")
    private var descriptionState by mutableStateOf("")

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(PersianContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readIntent(intent)

        // Show over lock screen and turn the screen on (API 26 compatible flags)
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        // Swipe-lock (non-secure keyguard) goes away automatically; secure
        // locks still show the alarm on top and the buttons stay usable.
        val keyguard = getSystemService(KeyguardManager::class.java)
        keyguard?.requestDismissKeyguard(this, null)

        // The alarm page is open → the backup notification (and its insistent
        // ringing) is no longer needed.
        if (taskId != -1L) NotificationHelper.cancelNotification(this, taskId)

        // The full-screen intent path passes only the task id — load the rest.
        loadTask()

        startRinging()

        setContent {
            TaskManagerTheme(darkTheme = true) {
                AlarmScreen(
                    title = titleState,
                    description = descriptionState,
                    onDone = { onDone() },
                    onSnooze = { minutes -> onSnooze(minutes) },
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        // Another reminder fired while the alarm screen is already open —
        // switch to the new task instead of losing it.
        setIntent(intent)
        readIntent(intent)
        if (taskId != -1L) NotificationHelper.cancelNotification(this, taskId)
        loadTask()
        if (mediaPlayer == null && vibrator == null) startRinging()
    }

    private fun readIntent(intent: android.content.Intent) {
        taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        titleState = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        descriptionState = intent.getStringExtra(EXTRA_DESCRIPTION).orEmpty()
    }

    private fun loadTask() {
        if (taskId == -1L) return
        val app = application as TaskManagerApp
        app.applicationScope.launch(Dispatchers.IO) {
            val task = app.container.taskRepository.getTask(taskId) ?: return@launch
            withContext(Dispatchers.Main) {
                titleState = task.title
                descriptionState = task.description
            }
        }
    }

    private fun startRinging() {
        val app = application as TaskManagerApp
        app.applicationScope.launch(Dispatchers.IO) {
            val settings = app.container.settingsRepository.settings.first()
            withContext(Dispatchers.Main) {
                if (settings.alarmSound) {
                    try {
                        val uri = RingtoneManager.getActualDefaultRingtoneUri(
                            this@AlarmActivity, RingtoneManager.TYPE_ALARM,
                        ) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(this@AlarmActivity, uri)
                            setAudioAttributes(
                                AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_ALARM)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                    .build()
                            )
                            isLooping = true
                            prepare()
                            start()
                        }
                    } catch (_: Exception) {
                    }
                }
                if (settings.vibration) {
                    vibrator = getSystemService(Vibrator::class.java)
                    val pattern = longArrayOf(0, 600, 400)
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
                }
            }
        }
    }

    private fun stopRinging() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
    }

    private fun onDone() {
        stopRinging()
        if (taskId != -1L) {
            val app = application as TaskManagerApp
            val tId = taskId
            app.applicationScope.launch {
                app.container.taskRepository.setCompleted(tId, true)
                NotificationHelper.cancelNotification(this@AlarmActivity, tId)
            }
        }
        finish()
    }

    private fun onSnooze(minutes: Int) {
        stopRinging()
        if (taskId != -1L) {
            val app = application as TaskManagerApp
            val tId = taskId
            val title = titleState
            val desc = descriptionState
            app.container.alarmScheduler.snooze(tId, title, desc, minutes)
            NotificationHelper.cancelNotification(this@AlarmActivity, tId)
        }
        finish()
    }

    override fun onResume() {
        super.onResume()
        if (taskId != -1L) NotificationHelper.cancelNotification(this, taskId)
    }

    override fun onStop() {
        super.onStop()
        // The user pressed Home / switched apps while the alarm rings:
        // re-post the insistent alarm notification so the ringing continues
        // and the screen is one tap away.
        if (!isFinishing && taskId != -1L) {
            NotificationHelper.showAlarmNotification(
                this, taskId, titleState, descriptionState,
            )
        }
    }

    override fun onDestroy() {
        stopRinging()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DESCRIPTION = "extra_description"
    }
}

@Composable
private fun AlarmScreen(
    title: String,
    description: String,
    onDone: () -> Unit,
    onSnooze: (Int) -> Unit,
) {
    val bg = Color(0xFF0F172A)
    val primary = Color(0xFF3B82F6)
    val success = Color(0xFF16A34A)
    val onSurface = Color(0xFFF8FAFC)
    val onSecondary = Color(0xFFCBD5E1)

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_000)
            nowMillis = System.currentTimeMillis()
        }
    }

    // Pulsing bell — makes it obvious the alarm is actually ringing.
    var phase by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(50)
            phase += 1
        }
    }
    val pulse = 1f + 0.08f * sin(phase * 0.25f).toFloat()

    // Safety: auto-silence after 3 minutes so a forgotten alarm never
    // drains the battery (the notification stays as the record).
    LaunchedEffect(Unit) {
        delay(180_000)
        onSnooze(5)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .graphicsLayer(scaleX = pulse, scaleY = pulse)
                    .background(primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_notification),
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(44.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Text("یادآوری وظیفه", color = onSecondary, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = title.ifBlank { "وظیفه" },
                color = onSurface,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            if (description.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = description,
                    color = onSecondary,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = formatClock(nowMillis),
                color = onSurface,
                fontSize = 46.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(36.dp))

            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = success, contentColor = Color.White),
            ) {
                Text("انجام شد", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(5, 10).forEach { m ->
                    OutlinedButton(
                        onClick = { onSnooze(m) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("${m.toPersianDigits()} دقیقه بعد", color = onSurface, fontSize = 15.sp)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(15, 30).forEach { m ->
                    OutlinedButton(
                        onClick = { onSnooze(m) },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("${m.toPersianDigits()} دقیقه بعد", color = onSurface, fontSize = 15.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("بعداً یادآوری کن", color = onSecondary, fontSize = 13.sp)
        }
    }
}

private fun formatClock(millis: Long): String {
    val dt = java.time.LocalDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(millis),
        java.time.ZoneId.systemDefault(),
    )
    return "%02d:%02d".format(dt.hour, dt.minute).toPersianDigits()
}
