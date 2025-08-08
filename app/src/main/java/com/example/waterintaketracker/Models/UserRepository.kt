// FileName: MultipleFiles/UserRepository.kt
package com.example.waterintaketracker.data

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.waterintaketracker.Models.Users
import com.example.waterintaketracker.Models.toLocalDate
import com.example.waterintaketracker.WaterReminderWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase,
    @ApplicationContext private val context: Context // Inject application context
) {
    private val _userProfile = MutableStateFlow<Users?>(null)
    val userProfile: StateFlow<Users?> = _userProfile.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var userListener: ValueEventListener? = null
    private var currentUserId: String? = null

    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val calculatedDailyWaterIntake: StateFlow<Int> = userProfile.map { profile ->
        calculateGoal(profile)
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 2500
    )

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            val firebaseUser  = auth.currentUser
            if (firebaseUser  != null) {
                if (currentUserId != firebaseUser .uid) {
                    currentUserId = firebaseUser .uid
                    listenToUserProfile(firebaseUser .uid)
                    scheduleWaterReminderWorker() // Schedule worker on login
                }
            } else {
                cleanupListener()
                currentUserId = null
                _userProfile.value = null
                _loading.value = false
                cancelWaterReminderWorker() // Cancel worker on logout
            }
        }
        firebaseAuth.currentUser ?.uid?.let { userId ->
            if (currentUserId == null) {
                currentUserId = userId
                listenToUserProfile(userId)
                scheduleWaterReminderWorker() // Schedule worker on initial app start if already logged in
            }
        } ?: run {
            _loading.value = false
        }
    }

    private fun listenToUserProfile(userId: String) {
        _loading.value = true
        _errorMessage.value = null
        cleanupListener()

        val userRef = firebaseDatabase.getReference("users").child(userId)
        userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                _userProfile.value = user
                _loading.value = false
                _errorMessage.value = if (user == null) "Profile not found." else null
                // Re-schedule worker if profile data changes (e.g., wake/sleep time)
                scheduleWaterReminderWorker()
            }

            override fun onCancelled(error: DatabaseError) {
                _loading.value = false
                _errorMessage.value = "Failed to load profile: ${error.message}"
                _userProfile.value = null
            }
        }
        userRef.addValueEventListener(userListener!!)
    }

    fun updateProfileField(field: String, value: Any, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = currentUserId ?: run {
            onFailure(IllegalStateException("User  not logged in."))
            return
        }
        _loading.value = true
        firebaseDatabase.getReference("users").child(userId).child(field).setValue(value)
            .addOnSuccessListener {
                _loading.value = false
                onSuccess()
                // If wake-up/sleep time is updated, re-schedule the worker
                if (field == "wakeupTime" || field == "sleepTime") {
                    scheduleWaterReminderWorker()
                }
            }
            .addOnFailureListener { e ->
                _loading.value = false
                onFailure(e)
            }
    }

    private fun calculateGoal(profile: Users?): Int {
        val weight = profile?.weight ?: 0
        if (profile == null || weight <= 0) {
            return 2500
        }
        return when (profile.gender?.trim()?.lowercase()) {
            "male" -> (weight * 35)
            "female" -> (weight * 31)
            else -> (weight * 33)
        }.coerceAtLeast(500)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateStreak(completedToday: Boolean) {
        repositoryScope.launch {
            val sharedPrefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            val lastCompletedDate = sharedPrefs.getString("last_completed_date", "")
            val today = LocalDate.now().toString()

            if (completedToday) {
                if (lastCompletedDate == today) {
                    // Already updated today
                    return@launch
                }

                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val yesterday = calendar.time.toLocalDate().toString()

                if (lastCompletedDate == yesterday) {
                    // Consecutive day - increment streak
                    val newStreak = sharedPrefs.getInt("current_streak", 0) + 1
                    sharedPrefs.edit()
                        .putInt("current_streak", newStreak)
                        .putString("last_completed_date", today)
                        .apply()
                    _currentStreak.value = newStreak
                } else {
                    // Not consecutive - reset to 1
                    sharedPrefs.edit()
                        .putInt("current_streak", 1)
                        .putString("last_completed_date", today)
                        .apply()
                    _currentStreak.value = 1
                }
            } else {
                // Check for streak break
                if (!lastCompletedDate.isNullOrEmpty() && lastCompletedDate != today) {
                    val lastDate = LocalDate.parse(lastCompletedDate)
                    if (lastDate.isBefore(LocalDate.now().minusDays(1))) {
                        // Missed a day - reset streak
                        sharedPrefs.edit()
                            .putInt("current_streak", 0)
                            .apply()
                        _currentStreak.value = 0
                    }
                }
            }
        }
    }

    fun logout(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        repositoryScope.launch { // This is Dispatchers.IO
            try {
                firebaseAuth.signOut()
                cancelWaterReminderWorker() // Cancel worker on explicit logout
                clearLoginState() // Clear login state
                onSuccess() // This callback is currently executed on Dispatchers.IO
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun clearLoginState() {
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_logged_in", false)
            .putInt("current_streak", 0)  // Reset streak on logout
            .putString("last_completed_date", "")
            .apply()
    }

    private fun cleanupListener() {
        currentUserId?.let { userId ->
            userListener?.let { listener ->
                firebaseDatabase.getReference("users").child(userId).removeEventListener(listener)
            }
        }
        userListener = null
    }

    // --- WorkManager Scheduling Logic ---
    private fun scheduleWaterReminderWorker() {
        val waterReminderWorkRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            2, TimeUnit.HOURS // Repeat every 2 hours
        )
            .setInitialDelay(1, TimeUnit.MINUTES) // Small initial delay to allow profile to load
            .addTag("water_reminder_worker")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "WaterReminderWork",
            ExistingPeriodicWorkPolicy.UPDATE, // Replace existing work if it exists
            waterReminderWorkRequest
        )
        println("WorkManager: WaterReminderWorker scheduled.")
    }

    private fun cancelWaterReminderWorker() {
        WorkManager.getInstance(context).cancelUniqueWork("WaterReminderWork")
        println("WorkManager: WaterReminderWorker cancelled.")
    }

    fun clear() {
        cleanupListener()
        repositoryScope.cancel()
        cancelWaterReminderWorker() // Ensure worker is cancelled if repository is cleared
    }
}
