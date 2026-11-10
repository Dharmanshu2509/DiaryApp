package com.example.diaryapp.ui.theme

import android.content.Context
import androidx.compose.runtime.mutableStateOf

enum class AppThemeMode(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System default");

    companion object {
        fun fromLabel(label: String): AppThemeMode {
            return entries.firstOrNull { it.label == label } ?: SYSTEM
        }
    }
}

object ThemePreferences {

    private const val PREFS_NAME = "diary_app_settings"
    private const val KEY_THEME_MODE = "theme_mode"

    val themeMode = mutableStateOf(AppThemeMode.SYSTEM)

    fun load(context: Context) {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val savedMode = preferences.getString(KEY_THEME_MODE, null)

        themeMode.value = savedMode
            ?.let { runCatching { AppThemeMode.valueOf(it) }.getOrNull() }
            ?: AppThemeMode.SYSTEM
    }

    fun setThemeMode(context: Context, mode: AppThemeMode) {
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).edit()
            .putString(KEY_THEME_MODE, mode.name)
            .apply()

        themeMode.value = mode
    }
}