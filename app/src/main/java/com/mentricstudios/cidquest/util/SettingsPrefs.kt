package com.mentricstudios.cidquest.util

import android.content.Context

private const val PREFS_NAME = "cid_quest_settings"
private const val KEY_ON_SCREEN_CONTROLS = "on_screen_controls_enabled"
private const val KEY_VIBRATION = "vibration_enabled"
private const val KEY_SOUND = "sound_enabled"

/**
 * Small persisted toggles for the Settings screen. On-screen controls default
 * to "off" (player can enable them from Settings if they want them). Vibration
 * and sound both default to "on" so a fresh install behaves like it did before.
 */
object SettingsPrefs {
    fun isOnScreenControlsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ON_SCREEN_CONTROLS, false)
    }

    fun setOnScreenControlsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ON_SCREEN_CONTROLS, enabled).apply()
    }

    fun isVibrationEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_VIBRATION, true)
    }

    fun setVibrationEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_VIBRATION, enabled).apply()
    }

    /** Master switch for SFX + background music, toggled from the speaker
     * icon on the Home screen (and mirrored in Settings). */
    fun isSoundEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SOUND, true)
    }

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
    }
}
