package com.taskmanager.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.taskmanager.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "task_manager_settings")

/** Simple app settings persisted with DataStore (theme, alarm sound, vibration, focus durations). */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val alarmSound: Boolean = true,
    val vibration: Boolean = true,
    val remindersEnabled: Boolean = true,
    val focusMinutes: Int = 25,
    val breakMinutes: Int = 5,
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val THEME = intPreferencesKey("theme_mode")
        val ALARM_SOUND = booleanPreferencesKey("alarm_sound")
        val VIBRATION = booleanPreferencesKey("vibration")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val FOCUS_MINUTES = intPreferencesKey("focus_minutes")
        val BREAK_MINUTES = intPreferencesKey("break_minutes")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            themeMode = ThemeMode.fromOrdinal(p[Keys.THEME] ?: ThemeMode.SYSTEM.ordinal),
            alarmSound = p[Keys.ALARM_SOUND] ?: true,
            vibration = p[Keys.VIBRATION] ?: true,
            remindersEnabled = p[Keys.REMINDERS_ENABLED] ?: true,
            focusMinutes = p[Keys.FOCUS_MINUTES] ?: 25,
            breakMinutes = p[Keys.BREAK_MINUTES] ?: 5,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME] = mode.ordinal }
    }

    suspend fun setAlarmSound(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ALARM_SOUND] = enabled }
    }

    suspend fun setVibration(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VIBRATION] = enabled }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REMINDERS_ENABLED] = enabled }
    }

    suspend fun setFocusMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.FOCUS_MINUTES] = minutes.coerceIn(5, 120) }
    }

    suspend fun setBreakMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.BREAK_MINUTES] = minutes.coerceIn(1, 60) }
    }

    /** Raw map for backup export. */
    suspend fun snapshot(): Map<String, Any> {
        val p = context.dataStore.data.first()
        return mapOf(
            "themeMode" to (p[Keys.THEME] ?: 0),
            "alarmSound" to (p[Keys.ALARM_SOUND] ?: true),
            "vibration" to (p[Keys.VIBRATION] ?: true),
            "remindersEnabled" to (p[Keys.REMINDERS_ENABLED] ?: true),
            "focusMinutes" to (p[Keys.FOCUS_MINUTES] ?: 25),
            "breakMinutes" to (p[Keys.BREAK_MINUTES] ?: 5),
        )
    }

    suspend fun restore(map: Map<String, Any>) {
        context.dataStore.edit { p ->
            (map["themeMode"] as? Number)?.let { p[Keys.THEME] = it.toInt() }
            (map["alarmSound"] as? Boolean)?.let { p[Keys.ALARM_SOUND] = it }
            (map["vibration"] as? Boolean)?.let { p[Keys.VIBRATION] = it }
            (map["remindersEnabled"] as? Boolean)?.let { p[Keys.REMINDERS_ENABLED] = it }
            (map["focusMinutes"] as? Number)?.let { p[Keys.FOCUS_MINUTES] = it.toInt().coerceIn(5, 120) }
            (map["breakMinutes"] as? Number)?.let { p[Keys.BREAK_MINUTES] = it.toInt().coerceIn(1, 60) }
        }
    }
}
