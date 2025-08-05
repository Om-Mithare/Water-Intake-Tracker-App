package com.example.waterintaketracker.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.waterintaketracker.MainActivity // Your MainActivity
import com.example.waterintaketracker.R // Your R file
import com.example.waterintaketracker.data.NotificationPreferencesRepository
import com.example.waterintaketracker.data.ProfileRepository
import com.example.waterintaketracker.data.UserProfile // Import UserProfile
import com.example.waterintaketracker.data.NotificationScheduleTimes // Import for creating the combined object
// WaterReminderScheduler is in the same package
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.LocalTime

@HiltWorker
class WaterReminderWorker @AssistedInject constructor(
    @Assisted private val appContext: Context, // Use appContext from @Assisted
    @Assisted workerParams: WorkerParameters,
    private val notificationPreferencesRepository: NotificationPreferencesRepository,
    private val profileRepository: ProfileRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "waterReminderWork"
        private const val NOTIFICATION_ID = 10123
        private const val CHANNEL_ID = "water_reminder_channel_v1"
        private const val CHANNEL_NAME = "Water Reminders"
        private const val CHANNEL_DESCRIPTION = "Regular reminders to stay hydrated."
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        println("WaterReminderWorker: doWork executing at ${LocalDateTime.now()}")

        // 1. Fetch current notification enabled status
        val notificationsEnabled = notificationPreferencesRepository.notificationsEnabledFlow.first()

        if (!notificationsEnabled) {
            println("WaterReminderWorker: Notifications are currently disabled in preferences. Work is concluding.")
            // No need to explicitly cancel here; scheduler won't schedule if disabled.
            // If this worker runs, it was scheduled *before* being disabled.
            return Result.success() // Successfully handled: did nothing as per current settings.
        }

        // 2. Fetch current profile data for wake-up and sleep times
        val userProfile: UserProfile = profileRepository.userProfileFlow.first()
        val wakeUpTime = userProfile.wakeUpTime
        val sleepTime = userProfile.sleepTime

        println("WaterReminderWorker: Fetched Profile - Wake: $wakeUpTime, Sleep: $sleepTime. Notifications Enabled: $notificationsEnabled")

        val now = LocalDateTime.now()
        val currentTime = now.toLocalTime()

        // 3. Check if current time is within the user's active schedule
        if (WaterReminderScheduler.isWithinSchedule(currentTime, wakeUpTime, sleepTime)) {
            println("WaterReminderWorker: Current time $currentTime is within schedule ($wakeUpTime - $sleepTime). Showing notification.")
            showNotification(appContext) // Pass the Hilt-injected application context
        } else {
            println("WaterReminderWorker: Current time $currentTime is OUTSIDE schedule ($wakeUpTime - $sleepTime). Notification SKIPPED.")
        }

        // 4. Always attempt to reschedule for the next interval.
        // The scheduler will use the latest prefs (enabled status, wake/sleep times)
        // to decide the next valid slot.
        val currentEffectiveSchedule = NotificationScheduleTimes(
            notificationsEnabled = notificationsEnabled, // This will be true if we reached here
            wakeUpTime = wakeUpTime,
            sleepTime = sleepTime
        )

        println("WaterReminderWorker: Rescheduling next reminder via WaterReminderScheduler.")
        WaterReminderScheduler.scheduleNextReminder(appContext, currentEffectiveSchedule, isInitialSetup = false)

        return Result.success()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showNotification(context: Context) {
        createNotificationChannel(context) // Ensure channel exists

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, pendingIntentFlags)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.your_logo) // **CREATE THIS DRAWABLE**
            .setContentTitle(context.getString(R.string.notification_title)) // **ADD THIS STRING**
            .setContentText(context.getString(R.string.notification_text))   // **ADD THIS STRING**
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                System.err.println("WaterReminderWorker: POST_NOTIFICATIONS permission not granted. Cannot show notification.")
                return
            }
        }

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
                println("WaterReminderWorker: Notification successfully shown with ID $NOTIFICATION_ID at ${LocalTime.now()}")
            }
        } catch (e: SecurityException) {
            System.err.println("WaterReminderWorker: SecurityException while showing notification. ${e.message}")
            e.printStackTrace()
        } catch (e: Exception) {
            System.err.println("WaterReminderWorker: Unexpected error showing notification. ${e.message}")
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager: NotificationManager? =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

            if (notificationManager == null) {
                System.err.println("WaterReminderWorker: NotificationManager is null, cannot create channel.")
                return
            }
            notificationManager.createNotificationChannel(channel)
            println("WaterReminderWorker: Notification channel '$CHANNEL_ID' ensured.")
        }
    }
}
