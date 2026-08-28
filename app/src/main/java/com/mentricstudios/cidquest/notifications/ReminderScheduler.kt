package com.mentricstudios.cidquest.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mentricstudios.cidquest.util.NotificationPrefs
import java.util.Calendar

/**
 * Schedules the two daily re-engagement notifications (see [ReminderMessages]
 * for the copy). Uses plain [AlarmManager] rather than WorkManager so the
 * fire time can be a real clock time ("~11:00", "~19:30") instead of a
 * 15-minute-minimum periodic window — and `setAndAllowWhileIdle` rather than
 * an *exact* alarm, since a reminder landing a few minutes late is fine and
 * this way the app never needs the SCHEDULE_EXACT_ALARM permission.
 *
 * Each fired alarm re-arms itself for the same slot 24h later (see
 * [ReminderReceiver]), and [BootReceiver] re-arms both slots after a device
 * restart, since AlarmManager alarms don't survive a reboot on their own.
 */
object ReminderScheduler {

    const val REQUEST_CODE_MORNING = 1001
    const val REQUEST_CODE_EVENING = 1002

    const val EXTRA_SLOT = "reminder_slot"
    const val SLOT_MORNING = "morning"
    const val SLOT_EVENING = "evening"

    private const val MORNING_HOUR = 11
    private const val MORNING_MINUTE = 0
    private const val EVENING_HOUR = 19
    private const val EVENING_MINUTE = 30

    /** Call once on app start (and any time the player re-enables reminders
     * from Settings). Safe to call repeatedly — re-arming an already-armed
     * alarm just replaces it with the same schedule. */
    fun scheduleDailyReminders(context: Context) {
        if (!NotificationPrefs.areRemindersEnabled(context)) {
            cancelDailyReminders(context)
            return
        }
        armSlot(context, SLOT_MORNING, REQUEST_CODE_MORNING, MORNING_HOUR, MORNING_MINUTE)
        armSlot(context, SLOT_EVENING, REQUEST_CODE_EVENING, EVENING_HOUR, EVENING_MINUTE)
    }

    fun cancelDailyReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntentFor(context, SLOT_MORNING, REQUEST_CODE_MORNING))
        alarmManager.cancel(pendingIntentFor(context, SLOT_EVENING, REQUEST_CODE_EVENING))
    }

    /** Re-arms a single slot for "the next occurrence of hour:minute" — used
     * both for the initial schedule and by [ReminderReceiver] to push the
     * same slot 24h further out right after it fires. */
    fun armSlot(context: Context, slot: String, requestCode: Int, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = nextOccurrence(hour, minute)
        val pendingIntent = pendingIntentFor(context, slot, requestCode)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun nextOccurrence(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }

    private fun pendingIntentFor(context: Context, slot: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_SLOT, slot)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }
}
