package com.mentricstudios.cidquest.util

import android.content.Context

private const val PREFS_NAME = "cid_quest_notifications"
private const val KEY_REMINDERS_ENABLED = "reminders_enabled"
private const val KEY_MESSAGE_CURSOR = "message_cursor"

/**
 * Persisted state for the daily reminder notifications (2x/day re-engagement
 * pings). Kept separate from [SettingsPrefs] since it's a different concern
 * (background scheduling vs. in-game toggles) and the player can flip it off
 * any time from Settings — no dark patterns here, it's a normal on/off switch.
 */
object NotificationPrefs {
    /** Reminders default to "on" but only ever actually show once the OS
     * notification permission (Android 13+) has been granted — this flag is
     * the player's own in-app preference layered on top of that. */
    fun areRemindersEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_REMINDERS_ENABLED, true)
    }

    fun setRemindersEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_REMINDERS_ENABLED, enabled).apply()
    }

    /** Monotonically increasing counter so each successive notification (morning
     * or evening, any day) walks forward through the message pool instead of
     * repeating the same line — cheap variety without needing a backend. */
    fun nextMessageCursor(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_MESSAGE_CURSOR, 0)
        prefs.edit().putInt(KEY_MESSAGE_CURSOR, current + 1).apply()
        return current
    }
}
