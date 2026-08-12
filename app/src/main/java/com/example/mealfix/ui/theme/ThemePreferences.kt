package com.example.mealfix.ui.theme

import android.content.Context

/**
 * Persists the user's explicit light/dark choice across app restarts, using plain
 * SharedPreferences — there's exactly one boolean to remember, so Room/DataStore would be
 * overkill. Until the user has toggled it once, [isDarkMode] falls back to whatever
 * [systemDefault] the caller passes in (typically the system's current dark-mode setting).
 */
object ThemePreferences {
    private const val PREFS_NAME = "mealfix_theme_prefs"
    private const val KEY_IS_DARK = "is_dark_mode"

    fun isDarkMode(context: Context, systemDefault: Boolean): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_DARK, systemDefault)
    }

    fun setDarkMode(context: Context, isDark: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_DARK, isDark)
            .apply()
    }
}
