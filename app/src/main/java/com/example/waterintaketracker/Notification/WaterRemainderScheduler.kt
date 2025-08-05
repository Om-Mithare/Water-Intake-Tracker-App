package com.example.waterintaketracker.workers

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.*
import com.example.waterintaketracker.data.NotificationScheduleTimes // Ensure this import is correct
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object WaterReminderScheduler {

    // Default interval, can be made configurable if needed
    var reminderIntervalHours = 2L // Remind every 2 hours during the active period

    /**
     * Central method to update scheduling based on new preferences.
     * This will cancel existing work and schedule new work if enabled.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateNotificationPreferences(
        context: Context,
        newPrefs: NotificationScheduleTimes,
        newIntervalHours: Long? = null // Optional: to change interval on the fly
    ) {
        newIntervalHours?.let {
            if (it > 0) reminderIntervalHours = it // Update interval if a valid one is provided
        }

        cancelWork(context) // Always cancel previous work to ensure correct rescheduling

        if (!newPrefs.notificationsEnabled) {
            println("Scheduler: Notifications are disabled. All reminder work cancelled and none will be scheduled.")
            return // Do not schedule new work if disabled
        }

        println("Scheduler: Existing work cancelled. Proceeding to schedule next reminder using Wake=${newPrefs.wakeUpTime}, Sleep=${newPrefs.sleepTime}, Interval=${reminderIntervalHours}h")
        scheduleNextReminder(context, newPrefs, isInitialSetup = true)
    }

    /**
     * Schedules the next reminder. This is also called by the worker itself to re-schedule.
     * isInitialSetup helps in deciding if the very first notification might be too soon.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    internal fun scheduleNextReminder(
        context: Context,
        prefs: NotificationScheduleTimes, // Current effective preferences
        isInitialSetup: Boolean = false
    ) {
        if (!prefs.notificationsEnabled) {
            // This check is a safeguard; updateNotificationPreferences should handle initial disabling.
            cancelWork(context)
            println("Scheduler (scheduleNextReminder): Notifications disabled. Work cancelled.")
            return
        }

        val now = LocalDateTime.now()
        var nextNotificationDateTime = calculateNextNotificationDateTime(
            now,
            prefs.wakeUpTime,
            prefs.sleepTime,
            reminderIntervalHours
        )

        if (nextNotificationDateTime == null) {
            println("Scheduler: Could not determine a valid next notification time (e.g., interval too large for schedule window). Trying for tomorrow's wakeup if valid.")
            // Attempt to schedule for the next day's wake-up time if it's a valid time to schedule for.
            val tomorrowWakeUpDateTime = LocalDateTime.of(now.toLocalDate().plusDays(1), prefs.wakeUpTime)
            if (isWithinSchedule(prefs.wakeUpTime, prefs.wakeUpTime, prefs.sleepTime)) { // Check if wakeUpTime itself is valid
                nextNotificationDateTime = tomorrowWakeUpDateTime
            } else {
                println("Scheduler: Tomorrow's wakeup time (${prefs.wakeUpTime}) is also outside a valid schedule. Cancelling any further work.")
                cancelWork(context) // Explicitly cancel if no valid time can be found
                return
            }
        }

        // If it's the initial setup and the first calculated time is too soon (or in the past),
        // advance it by one interval to avoid immediate notification spam.
        if (isInitialSetup && nextNotificationDateTime.isBefore(now.plusMinutes(1))) { // e.g. if calculated time is within 1 minute
            println("Scheduler (Initial Setup): First calculated time $nextNotificationDateTime is too soon or past. Advancing by $reminderIntervalHours hours.")
            var advancedTime = nextNotificationDateTime.plusHours(reminderIntervalHours)

            // Check if this advanced time goes past sleep time for today
            if (!isWithinSchedule(advancedTime.toLocalTime(), prefs.wakeUpTime, prefs.sleepTime) &&
                advancedTime.toLocalDate().isEqual(nextNotificationDateTime.toLocalDate())) { // Still same day
                println("Scheduler (Initial Setup): Advanced time $advancedTime goes past sleep time. Scheduling for next day's wake up (${prefs.wakeUpTime}).")
                val tomorrowWakeUpDateTime = LocalDateTime.of(now.toLocalDate().plusDays(1), prefs.wakeUpTime)
                if (isWithinSchedule(prefs.wakeUpTime, prefs.wakeUpTime, prefs.sleepTime)) {
                    nextNotificationDateTime = tomorrowWakeUpDateTime
                } else {
                    println("Scheduler (Initial Setup): Tomorrow's wakeup not valid after advancing. Cancelling.")
                    cancelWork(context)
                    return
                }
            } else {
                nextNotificationDateTime = advancedTime // Use the advanced time
            }
        }

        val delay = Duration.between(now, nextNotificationDateTime)

        if (delay.isNegative || delay.isZero) {
            println("Scheduler: Calculated delay is negative or zero (${delay.seconds}s) for target $nextNotificationDateTime. Fallback: attempting tomorrow's wakeup.")
            // If calculated delay is invalid, try to schedule for the next day's wake-up time.
            val nextDayWakeUpDateTime = LocalDateTime.of(now.toLocalDate().plusDays(1), prefs.wakeUpTime)
            if (isWithinSchedule(prefs.wakeUpTime, prefs.wakeUpTime, prefs.sleepTime)) {
                val correctedDelay = Duration.between(now, nextDayWakeUpDateTime)
                if (!correctedDelay.isNegative && correctedDelay.toMillis() > 1000) { // Ensure positive and reasonably long delay
                    scheduleWork(context, correctedDelay.toMillis(), nextDayWakeUpDateTime)
                } else {
                    println("Scheduler: Corrected delay for tomorrow's wakeup is still invalid. Cancelling work.")
                    cancelWork(context)
                }
            } else {
                println("Scheduler: Tomorrow's wakeup time not valid for schedule on negative delay. Cancelling.")
                cancelWork(context)
            }
            return
        }

        // Proceed to schedule the work if delay is valid
        scheduleWork(context, delay.toMillis(), nextNotificationDateTime)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleWork(context: Context, delayMillis: Long, scheduledTime: LocalDateTime) {
        if (delayMillis < 1000) { // Avoid scheduling with extremely short delays
            println("Scheduler: Calculated delay $delayMillis ms is too short for target $scheduledTime. Aborting scheduling.")
            return
        }

        val reminderWorkRequest = OneTimeWorkRequestBuilder<WaterReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .addTag(WaterReminderWorker.WORK_NAME) // Tag for cancellation
            .setConstraints(Constraints.Builder().build()) // Basic constraints, can be expanded
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WaterReminderWorker.WORK_NAME, // Unique work name
            ExistingWorkPolicy.REPLACE,    // Replace existing work with this name
            reminderWorkRequest
        )

        val minutes = delayMillis / 1000 / 60
        val seconds = (delayMillis / 1000) % 60
        println("Scheduler: Reminder (re)scheduled for $scheduledTime (in approx $minutes m $seconds s). Current time: ${LocalDateTime.now()}")
    }

    /**
     * Calculates the next valid notification DateTime based on current time, schedule, and interval.
     * Returns null if no valid time can be found within the logic (e.g., interval too large).
     */
    @RequiresApi(Build.VERSION_CODES.O)
    internal fun calculateNextNotificationDateTime(
        currentDateTime: LocalDateTime,
        wakeUpTime: LocalTime,
        sleepTime: LocalTime,
        intervalHours: Long
    ): LocalDateTime? {
        if (intervalHours <= 0) return null // Invalid interval

        val currentDate = currentDateTime.toLocalDate()
        val currentTime = currentDateTime.toLocalTime()

        // Case 1: Current time is before wake-up time today.
        // Schedule for today's wake-up time if wakeUpTime is within the overall schedule.
        if (currentTime.isBefore(wakeUpTime)) {
            return if (isWithinSchedule(wakeUpTime, wakeUpTime, sleepTime)) {
                LocalDateTime.of(currentDate, wakeUpTime)
            } else {
                // If wakeUpTime itself is not valid (e.g. wake=23:00, sleep=05:00, but current check made at 06:00)
                // Try next day's wake up time
                if(isWithinSchedule(wakeUpTime, wakeUpTime, sleepTime)){ // Re-check for next day logic
                    LocalDateTime.of(currentDate.plusDays(1), wakeUpTime)
                } else {
                    null // Wake up time is fundamentally outside any valid window
                }
            }
        }

        // Case 2: Current time is after sleep time today (or within an overnight schedule's sleep period).
        // Schedule for next day's wake-up time if wakeUpTime is valid.
        if (!isWithinSchedule(currentTime, wakeUpTime, sleepTime)) {
            return if (isWithinSchedule(wakeUpTime, wakeUpTime, sleepTime)) { // Check if target wakeUpTime is valid
                LocalDateTime.of(currentDate.plusDays(1), wakeUpTime)
            } else {
                null // Next day's wake-up is also not schedulable
            }
        }

        // Case 3: Current time is within the active schedule (wakeUpTime <= currentTime < sleepTime).
        // Iterate from wakeUpTime by intervalHours until a time after currentTime is found.
        var nextNotificationTimeToday = wakeUpTime
        while (true) {
            // If this slot is at or after current time AND still within today's schedule
            if ((nextNotificationTimeToday.isAfter(currentTime) || nextNotificationTimeToday == currentTime) &&
                isWithinSchedule(nextNotificationTimeToday, wakeUpTime, sleepTime)) {
                return LocalDateTime.of(currentDate, nextNotificationTimeToday)
            }

            // Advance to the next slot
            nextNotificationTimeToday = nextNotificationTimeToday.plusHours(intervalHours)

            // If the next slot goes outside today's schedule (or wraps around in an overnight schedule incorrectly)
            if (!isWithinSchedule(nextNotificationTimeToday, wakeUpTime, sleepTime)) {
                // Schedule for next day's wake-up time if wakeUpTime is valid
                return if (isWithinSchedule(wakeUpTime, wakeUpTime, sleepTime)) {
                    LocalDateTime.of(currentDate.plusDays(1), wakeUpTime)
                } else {
                    null
                }
            }
        }
    }

    /**
     * Checks if a given time (`timeToCheck`) is within the active period defined by `wakeUpTime` and `sleepTime`.
     * Handles overnight schedules correctly (e.g., wake 22:00, sleep 06:00).
     */
    @RequiresApi(Build.VERSION_CODES.O)
    internal fun isWithinSchedule(
        timeToCheck: LocalTime,
        wakeUpTime: LocalTime,
        sleepTime: LocalTime
    ): Boolean {
        return if (wakeUpTime.isBefore(sleepTime)) {
            // Standard day schedule (e.g., wake 07:00, sleep 22:00)
            !timeToCheck.isBefore(wakeUpTime) && timeToCheck.isBefore(sleepTime)
        } else {
            // Overnight schedule (e.g., wake 22:00, sleep 06:00)
            !timeToCheck.isBefore(wakeUpTime) || timeToCheck.isBefore(sleepTime)
        }
    }

    fun cancelWork(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WaterReminderWorker.WORK_NAME)
        println("Scheduler: Water reminder work with name '${WaterReminderWorker.WORK_NAME}' explicitly cancelled.")
    }
}
