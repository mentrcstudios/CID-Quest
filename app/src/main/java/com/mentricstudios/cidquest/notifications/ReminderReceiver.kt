package com.mentricstudios.cidquest.notifications

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.mentricstudios.cidquest.MainActivity
import com.mentricstudios.cidquest.R
import com.mentricstudios.cidquest.util.NotificationPrefs

/**
 * Fires when a scheduled reminder alarm goes off. Shows one notification for
 * whichever slot (morning/evening) triggered it, then immediately re-arms
 * that same slot for 24h later so the schedule keeps going indefinitely.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val slot = intent.getStringExtra(ReminderScheduler.EXTRA_SLOT) ?: ReminderScheduler.SLOT_MORNING

        // Player may have turned reminders off from Settings since this was
        // scheduled — honor that instead of showing one anyway, and don't
        // re-arm the next occurrence either.
        if (NotificationPrefs.areRemindersEnabled(context)) {
            showNotification(context, slot)
        }

        val (requestCode, hour, minute) = when (slot) {
            ReminderScheduler.SLOT_EVENING -> Triple(ReminderScheduler.REQUEST_CODE_EVENING, 19, 30)
            else -> Triple(ReminderScheduler.REQUEST_CODE_MORNING, 11, 0)
        }
        if (NotificationPrefs.areRemindersEnabled(context)) {
            ReminderScheduler.armSlot(context, slot, requestCode, hour, minute)
        }
    }

    private fun showNotification(context: Context, slot: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return
        }

        val pool = if (slot == ReminderScheduler.SLOT_EVENING) {
            ReminderMessages.EVENING
        } else {
            ReminderMessages.MORNING
        }
        val cursor = NotificationPrefs.nextMessageCursor(context)
        val message = pool[cursor % pool.size]

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val notification = NotificationCompat.Builder(context, NotificationChannels.REMINDERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.body))
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // requestCode used as notification id too, so morning/evening never
        // overwrite each other if both are somehow pending at once.
        val notificationId = if (slot == ReminderScheduler.SLOT_EVENING) {
            ReminderScheduler.REQUEST_CODE_EVENING
        } else {
            ReminderScheduler.REQUEST_CODE_MORNING
        }
        notificationManager.notify(notificationId, notification)
    }
}
