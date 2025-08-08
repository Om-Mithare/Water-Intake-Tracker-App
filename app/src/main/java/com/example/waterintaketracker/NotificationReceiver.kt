package com.example.waterintaketracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// NotificationReceiver.kt
class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val notificationService = WaterNotificationService(context)
        notificationService.showBasicNotification()

        // Reschedule the next alarm
        scheduleNextAlarm(context)
    }
    private fun scheduleNextAlarm(context: Context) {
        AlarmScheduler.schedulePeriodicAlarms(context)
    }
}
