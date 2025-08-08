package com.example.waterintaketracker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.waterintaketracker.data.UserRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

@HiltWorker
class WaterReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val userRepository: UserRepository,
    private val notificationService: WaterNotificationService // Inject the notification service
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val userProfile = userRepository.userProfile.firstOrNull()

        if (userProfile == null) {
            // User not logged in or profile not found, no need to send notification
            return Result.success()
        }

        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(Calendar.MINUTE)

        val wakeUpTimeStr = userProfile.wakeupTime
        val sleepTimeStr = userProfile.sleepTime

        // Parse wake-up and sleep times
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        try {
            val wakeUpCal = Calendar.getInstance().apply {
                time = timeFormat.parse(wakeUpTimeStr) ?: throw IllegalArgumentException("Invalid wake-up time format")
            }
            val sleepCal = Calendar.getInstance().apply {
                time = timeFormat.parse(sleepTimeStr) ?: throw IllegalArgumentException("Invalid sleep time format")
            }

            // Set the year, month, day to today's date for comparison
            wakeUpCal.set(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH))
            sleepCal.set(currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH))

            // Handle cases where sleep time is on the next day (e.g., wake up 7 AM, sleep 10 PM)
            if (sleepCal.before(wakeUpCal)) {
                sleepCal.add(Calendar.DAY_OF_YEAR, 1)
            }

            // Check if current time is within the active hydration window
            if (currentTime.after(wakeUpCal) && currentTime.before(sleepCal)) {
                notificationService.showBasicNotification()
                return Result.success()
            } else {
                // Outside active hours, no notification needed
                return Result.success()
            }

        } catch (e: Exception) {
            // Log the error for debugging
            e.printStackTrace()
            return Result.failure() // Indicate failure if parsing or other issues occur
        }
    }
}
