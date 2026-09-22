package com.taskmanager.app

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import java.util.Locale

/**
 * Forces the Persian (fa) locale and RTL layout for all app activities,
 * regardless of the device language.
 */
object PersianContextWrapper {

    fun wrap(base: Context): ContextWrapper {
        val config = Configuration(base.resources.configuration)
        val locale = Locale("fa")
        Locale.setDefault(locale)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return ContextWrapper(base.createConfigurationContext(config))
    }
}
