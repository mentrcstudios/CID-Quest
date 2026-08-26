package com.mentricstudios.cidquest.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * AlarmManager alarms are wiped on device restart, so re-arm both daily
 * reminder slots once the system finishes booting (only if the player still
 * has reminders enabled).
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ReminderScheduler.scheduleDailyReminders(context)
        }
    }
}
