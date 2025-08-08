package com.example.waterintaketracker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import kotlin.random.Random

class WaterNotificationService(
    private val context: Context
) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    fun showBasicNotification() {
        // Create an Intent to open your MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Create a PendingIntent from the Intent
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0, // Request code
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // Flags
        )

        val notification = NotificationCompat.Builder(context, "water_reminder")
            .setContentTitle("Pixo-Hydrate")
            .setContentText("Your streak awaits! \nTime to drink some water!")
            .setSmallIcon(R.drawable.your_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Dismiss notification when tapped
            .setContentIntent(pendingIntent) // Set the PendingIntent here
            .build()

        notificationManager.notify(
            Random.nextInt(),
            notification
        )
    }

    fun showExpandableNotification() {
        val image = context.bitmapFromResource(R.drawable.notification_logo)

        // Create an Intent to open your MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Create a PendingIntent from the Intent
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0, // Request code
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // Flags
        )

        val notification = NotificationCompat.Builder(context, "water_reminder")
            .setContentTitle("Pixo-Remainder")
            .setContentText("Time to drink some water!")
            .setSmallIcon(R.drawable.notification_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setLargeIcon(image)
            .setStyle(
                NotificationCompat
                    .BigPictureStyle()
                    .bigPicture(image)
                    .bigLargeIcon(null as Bitmap?)
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Set the PendingIntent here
            .build()

        notificationManager.notify(
            Random.nextInt(),
            notification
        )
    }

    fun showExpandableLongText() {
        // Create an Intent to open your MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Create a PendingIntent from the Intent
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0, // Request code
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // Flags
        )

        val notification = NotificationCompat.Builder(context, "water_reminder")
            .setContentTitle("Pixo-Hydrate")
            .setContentText("Time to drink some water!")
            .setSmallIcon(R.drawable.your_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat
                    .BigTextStyle()
                    .bigText("Very big text")
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Set the PendingIntent here
            .build()

        notificationManager.notify(
            Random.nextInt(),
            notification
        )
    }

    fun showInboxStyleNotification() {
        // Create an Intent to open your MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Create a PendingIntent from the Intent
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0, // Request code
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // Flags
        )

        val notification = NotificationCompat.Builder(context, "water_reminder")
            .setContentTitle("Pixo-Hydrate")
            .setContentText("Time to drink some water!")
            .setSmallIcon(R.drawable.notification_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat
                    .InboxStyle()
                    .addLine("Line 1")
                    .addLine("Line 2")
                    .addLine("Line 3")
                    .addLine("Line 4")
                    .addLine("Line 5")
                    .addLine("Line 6")
                    .addLine("Line 7")
            )
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // Set the PendingIntent here
            .build()

        notificationManager.notify(
            Random.nextInt(),
            notification
        )
    }

    fun showNotificationGroup() {
        val groupId = "water_group"
        val summaryId = 0

        // Create an Intent to open your MainActivity for the group summary
        val groupIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Create a PendingIntent for the group summary
        val groupPendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            0, // Request code
            groupIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // Flags
        )

        val notification1 = NotificationCompat.Builder(context, "water_reminder")
            .setContentTitle("Pixo-Hydrate")
            .setContentText("Time to drink some water!")
            .setSmallIcon(R.drawable.notification_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat
                    .InboxStyle()
                    .addLine("Line 1")
            )
            .setAutoCancel(true)
            .setGroup(groupId)
            .setContentIntent(groupPendingIntent) // Set the PendingIntent here
            .build()

        val notification2 = NotificationCompat.Builder(context, "water_reminder")
            .setContentTitle("Water Reminder")
            .setContentText("Time to drink some water!")
            .setSmallIcon(R.drawable.notification_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat
                    .InboxStyle()
                    .addLine("Line 1")
                    .addLine("Line 2")
            )
            .setAutoCancel(true)
            .setGroup(groupId)
            .setContentIntent(groupPendingIntent) // Set the PendingIntent here
            .build()

        val summaryNotification = NotificationCompat.Builder(context, "water_reminder")
            .setContentTitle("Water Reminder")
            .setContentText("Time to drink some water!")
            .setSmallIcon(R.drawable.notification_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat
                    .InboxStyle()
                    .setSummaryText("Water reminders missed")
                    .setBigContentTitle("Water Reminders")
            )
            .setAutoCancel(true)
            .setGroup(groupId)
            .setGroupSummary(true)
            .setContentIntent(groupPendingIntent) // Set the PendingIntent here
            .build()

        notificationManager.notify(
            Random.nextInt(),
            notification1
        )
        notificationManager.notify(
            Random.nextInt(),
            notification2
        )
        notificationManager.notify(
            summaryId, // Use a consistent ID for the summary notification
            summaryNotification
        )
    }

    private fun Context.bitmapFromResource(
        @DrawableRes resId: Int
    ) = BitmapFactory.decodeResource(
        resources,
        resId
    )
}
