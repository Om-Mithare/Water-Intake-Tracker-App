package com.example.waterintaketracker.data

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

data class UserProfile @RequiresApi(Build.VERSION_CODES.O) constructor(
    val userName: String = "User", // Example field
    val wakeUpTime: LocalTime = LocalTime.of(7, 0), // Default wake-up time
    val sleepTime: LocalTime = LocalTime.of(22, 0)  // Default sleep time
)

@Singleton
class ProfileRepository @Inject constructor() {
    // Mock implementation using MutableStateFlow for simplicity
    @RequiresApi(Build.VERSION_CODES.O)
    private val _userProfileFlow = MutableStateFlow(UserProfile())
    @RequiresApi(Build.VERSION_CODES.O)
    val userProfileFlow: Flow<UserProfile> = _userProfileFlow.asStateFlow()

    // Method to get the current profile, useful for synchronous needs if any (e.g., initial VM state)
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentUserProfile(): UserProfile = _userProfileFlow.value

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateWakeUpTime(time: LocalTime) {
        val newProfile = _userProfileFlow.value.copy(wakeUpTime = time)
        _userProfileFlow.value = newProfile // Emit the new state
        println("ProfileRepository: Updated wakeUpTime to $time. New profile: $newProfile")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateSleepTime(time: LocalTime) {
        val newProfile = _userProfileFlow.value.copy(sleepTime = time)
        _userProfileFlow.value = newProfile // Emit the new state
        println("ProfileRepository: Updated sleepTime to $time. New profile: $newProfile")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveUserProfile(profile: UserProfile) {
        _userProfileFlow.value = profile
        println("ProfileRepository: Saved UserProfile: $profile")
    }
}
