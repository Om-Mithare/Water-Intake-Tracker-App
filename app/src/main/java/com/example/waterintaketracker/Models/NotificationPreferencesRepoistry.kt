package com.example.waterintaketracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.time.LocalTime // Ensure this is imported
import javax.inject.Inject
import javax.inject.Singleton

// Define a delegate for creating the DataStore instance
// This can be in a separate ContextExt.kt file or here for simplicity
val Context.notificationDataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_prefs")

// This data class is used by the ViewModel and Scheduler to combine states.
// It's a convenient way to pass around the relevant notification settings.
data class NotificationScheduleTimes(
    val notificationsEnabled: Boolean,
    val wakeUpTime: LocalTime,
    val sleepTime: LocalTime
)

@Singleton
class NotificationPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Access the DataStore instance using the extension property
    private val dataStore = context.notificationDataStore

    private object PreferencesKeys {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        // WAKE_UP_TIME and SLEEP_TIME are no longer stored here; they come from ProfileRepository
    }

    val notificationsEnabledFlow: Flow<Boolean> = dataStore.data
        .catch { exception ->
            // Handle I/O errors when reading preferences
            if (exception is IOException) {
                System.err.println("Error reading notifications_enabled preference: $exception")
                emit(emptyPreferences()) // Emit empty preferences to recover, or handle differently
            } else {
                throw exception // Rethrow other exceptions
            }
        }
        .map { preferences ->
            // Retrieve the boolean value, defaulting to true if not found
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
        }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
        println("NotificationPreferencesRepository: Updated notificationsEnabled to $enabled")
    }
}
