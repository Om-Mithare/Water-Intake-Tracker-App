package com.example.waterintaketracker.ui // Or your ViewModel package

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.waterintaketracker.data.NotificationPreferencesRepository
import com.example.waterintaketracker.data.NotificationScheduleTimes
import com.example.waterintaketracker.data.ProfileRepository
import com.example.waterintaketracker.workers.WaterReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val notificationPreferencesRepository: NotificationPreferencesRepository,
    private val profileRepository: ProfileRepository,
    application: Application // For context if needed by scheduler directly, or pass specific context
) : AndroidViewModel(application) {

    // For providing a sensible initial value to the combined flow immediately.
    // This fetches the current profile synchronously once.
    @RequiresApi(Build.VERSION_CODES.O)
    private val _initialUserProfileForSchedule = profileRepository.getCurrentUserProfile()

    @RequiresApi(Build.VERSION_CODES.O)
    val notificationSchedule: StateFlow<NotificationScheduleTimes?> =
        combine(
            notificationPreferencesRepository.notificationsEnabledFlow,
            profileRepository.userProfileFlow // Use the flow for ongoing updates from profile changes
        ) { isEnabled, userProfile ->
            // Create the combined data structure
            NotificationScheduleTimes(
                notificationsEnabled = isEnabled,
                wakeUpTime = userProfile.wakeUpTime,
                sleepTime = userProfile.sleepTime
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L), // Keep active 5s after last subscriber
            initialValue = NotificationScheduleTimes( // Provide an immediate initial value
                notificationsEnabled = true, // Consider fetching synchronously or using a default
                wakeUpTime = _initialUserProfileForSchedule.wakeUpTime,
                sleepTime = _initialUserProfileForSchedule.sleepTime
            )
        )

    // Call this when the screen is first displayed or when permission is granted
    @RequiresApi(Build.VERSION_CODES.O)
    fun ensureRemindersAreScheduledIfNeeded() {
        viewModelScope.launch {
            // Get the most current combined state for scheduling decision
            // Use .first() if you need to ensure it's fully resolved, or .value if already subscribed
            val currentSchedule = notificationSchedule.value ?: notificationSchedule.filterNotNull().first()


            if (currentSchedule.notificationsEnabled) {
                println("NotificationSettingsViewModel: Notifications ENABLED. Scheduling with Profile Times: Wake=${currentSchedule.wakeUpTime}, Sleep=${currentSchedule.sleepTime}")
                WaterReminderScheduler.updateNotificationPreferences(
                    getApplication<Application>().applicationContext,
                    currentSchedule,
                    WaterReminderScheduler.reminderIntervalHours // Use the scheduler's current interval
                )
            } else {
                println("NotificationSettingsViewModel: Notifications DISABLED. Cancelling any existing work.")
                WaterReminderScheduler.cancelWork(getApplication<Application>().applicationContext)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            notificationPreferencesRepository.updateNotificationsEnabled(enabled)
            // The `notificationSchedule` flow will automatically emit a new state.
            // Now, explicitly re-evaluate scheduling based on the new combined state.
            val latestSchedule = notificationSchedule.value ?: notificationSchedule.filterNotNull().first()

            if (latestSchedule.notificationsEnabled) { // Check the NEW enabled state from the combined flow
                println("NotificationSettingsViewModel: Toggled notifications to ENABLED. Re-scheduling.")
                WaterReminderScheduler.updateNotificationPreferences(
                    getApplication<Application>().applicationContext,
                    latestSchedule,
                    WaterReminderScheduler.reminderIntervalHours
                )
            } else {
                println("NotificationSettingsViewModel: Toggled notifications to DISABLED. Cancelling work.")
                WaterReminderScheduler.cancelWork(getApplication<Application>().applicationContext)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateUserWakeUpTime(time: LocalTime) {
        viewModelScope.launch {
            profileRepository.updateWakeUpTime(time)
            // `notificationSchedule` flow updates automatically due to `profileRepository.userProfileFlow`.
            // If notifications are enabled, re-schedule with the new time.
            val currentSchedule = notificationSchedule.value // Use current value for immediate reaction
            if (currentSchedule != null && currentSchedule.notificationsEnabled) {
                println("NotificationSettingsViewModel: Profile wakeUpTime changed to $time. Re-scheduling.")
                // The currentSchedule already reflects the new wakeUpTime due to the `combine`
                WaterReminderScheduler.updateNotificationPreferences(
                    getApplication<Application>().applicationContext,
                    currentSchedule, // This will have the updated wakeUpTime
                    WaterReminderScheduler.reminderIntervalHours
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateUserSleepTime(time: LocalTime) {
        viewModelScope.launch {
            profileRepository.updateSleepTime(time)
            val currentSchedule = notificationSchedule.value
            if (currentSchedule != null && currentSchedule.notificationsEnabled) {
                println("NotificationSettingsViewModel: Profile sleepTime changed to $time. Re-scheduling.")
                // The currentSchedule already reflects the new sleepTime
                WaterReminderScheduler.updateNotificationPreferences(
                    getApplication<Application>().applicationContext,
                    currentSchedule, // This will have the updated sleepTime
                    WaterReminderScheduler.reminderIntervalHours
                )
            }
        }
    }
}
