package com.mentricstudios.cidquest.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val REMINDERS_CHANNEL_ID = "reminders"

    /** Safe to call every app start — creating an already-existing channel
     * is a no-op. Must run before the first notification is ever posted. */
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            REMINDERS_CHANNEL_ID,
            "Play reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Occasional reminders to jump back into Cid Quest"
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
