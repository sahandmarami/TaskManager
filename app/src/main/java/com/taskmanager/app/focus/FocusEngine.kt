package com.taskmanager.app.focus

import com.taskmanager.app.data.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Pomodoro-style focus engine. Lives in the app container so the timer keeps
 * running while the user navigates between tabs.
 *
 * Default: 25 minutes focus / 5 minutes break (user adjustable).
 */
class FocusEngine(private val settingsRepository: SettingsRepository) {

    enum class Mode { FOCUS, BREAK }

    data class FocusState(
        val mode: Mode = Mode.FOCUS,
        val running: Boolean = false,
        val remainingSec: Int = 25 * 60,
        val focusMinutes: Int = 25,
        val breakMinutes: Int = 5,
        val sessionsCompleted: Int = 0,
        /** Incremented once per finished session so the UI can play a sound. */
        val sessionJustFinished: Int = 0,
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickJob: Job? = null

    private val _state = MutableStateFlow(FocusState())
    val state: StateFlow<FocusState> = _state

    init {
        scope.launch {
            val s = settingsRepository.settings.first()
            _state.value = _state.value.copy(
                focusMinutes = s.focusMinutes,
                breakMinutes = s.breakMinutes,
                remainingSec = s.focusMinutes * 60,
            )
        }
    }

    fun start() {
        if (_state.value.running) return
        _state.value = _state.value.copy(running = true)
        tickJob = scope.launch { tick() }
    }

    fun pause() {
        _state.value = _state.value.copy(running = false)
        tickJob?.cancel()
        tickJob = null
    }

    /** Resets the current mode's remaining time. */
    fun reset() {
        tickJob?.cancel()
        val s = _state.value
        val minutes = if (s.mode == Mode.FOCUS) s.focusMinutes else s.breakMinutes
        _state.value = s.copy(running = false, remainingSec = minutes * 60, sessionJustFinished = 0)
    }

    /** Ends the running session and moves to break / focus. */
    fun finishSession() {
        tickJob?.cancel()
        val s = _state.value
        if (s.mode == Mode.FOCUS) {
            _state.value = s.copy(
                mode = Mode.BREAK,
                running = false,
                remainingSec = s.breakMinutes * 60,
                sessionsCompleted = s.sessionsCompleted + 1,
                sessionJustFinished = s.sessionJustFinished + 1,
            )
        } else {
            _state.value = s.copy(
                mode = Mode.FOCUS,
                running = false,
                remainingSec = s.focusMinutes * 60,
            )
        }
    }

    fun setFocusMinutes(minutes: Int) {
        val m = minutes.coerceIn(5, 120)
        scope.launch { settingsRepository.setFocusMinutes(m) }
        _state.value = _state.value.copy(focusMinutes = m)
        if (_state.value.mode == Mode.FOCUS && !_state.value.running) {
            _state.value = _state.value.copy(remainingSec = m * 60)
        }
    }

    fun setBreakMinutes(minutes: Int) {
        val m = minutes.coerceIn(1, 60)
        scope.launch { settingsRepository.setBreakMinutes(m) }
        _state.value = _state.value.copy(breakMinutes = m)
        if (_state.value.mode == Mode.BREAK && !_state.value.running) {
            _state.value = _state.value.copy(remainingSec = m * 60)
        }
    }

    fun clearFinishedFlag() {
        if (_state.value.sessionJustFinished != 0) {
            _state.value = _state.value.copy(sessionJustFinished = 0)
        }
    }

    private suspend fun tick() {
        while (_state.value.running && _state.value.remainingSec > 0) {
            delay(1_000)
            val s = _state.value
            if (!s.running) return
            if (s.remainingSec - 1 <= 0) {
                _state.value = s.copy(remainingSec = 0, running = false)
                finishSession()
                return
            }
            _state.value = s.copy(remainingSec = s.remainingSec - 1)
        }
    }
}
