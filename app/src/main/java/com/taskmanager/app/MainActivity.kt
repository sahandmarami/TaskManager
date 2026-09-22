package com.taskmanager.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.ContextCompat
import com.taskmanager.app.data.settings.AppSettings
import com.taskmanager.app.domain.model.ThemeMode
import com.taskmanager.app.ui.navigation.AppRoot
import com.taskmanager.app.ui.theme.TaskManagerTheme

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(PersianContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        val app = application as TaskManagerApp
        setContent {
            val settings by app.container.settingsRepository.settings
                .collectAsState(initial = AppSettings())
            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                TaskManagerTheme(darkTheme = darkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.background,
                    ) {
                        AppRoot(app.container)
                    }
                    OverlayPermissionDialog()
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

/**
 * One-time-per-launch prompt for the overlay permission ("نمایش روی
 * برنامه‌های دیگر"). With it granted, the alarm page opens over any app
 * and any lock screen — exactly like the built-in Clock alarms.
 * Disappears forever once granted.
 */
@Composable
private fun OverlayPermissionDialog() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity

    var show by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val granted = android.provider.Settings.canDrawOverlays(context)
        show = !granted
    }

    if (show) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { show = false },
            title = {
                androidx.compose.material3.Text(
                    "دسترسی نمایش روی برنامه‌ها",
                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                )
            },
            text = {
                androidx.compose.material3.Text(
                    "برای اینکه آلارم وظایف دقیقاً مثل ساعت خود گوشی، در هر حالتی — حتی روی قفل صفحه و داخل برنامه‌های دیگر — خودکار باز شود، دسترسی «نمایش روی برنامه‌های دیگر» را فعال کن.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    show = false
                    try {
                        activity?.startActivity(
                            android.content.Intent(
                                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:${context.packageName}"),
                            )
                        )
                    } catch (_: Exception) {
                    }
                }) {
                    androidx.compose.material3.Text(
                        "اعطای دسترسی",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { show = false }) {
                    androidx.compose.material3.Text("بعداً")
                }
            },
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        )
    }
}
