package com.example.waterintaketracker.ViewModels

// No longer need to import ProfileViewModel directly
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waterintaketracker.Models.WaterLogEntry
import com.example.waterintaketracker.data.UserRepository // Import UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth, // Keep for auth state if needed for logs
    private val firebaseDatabase: FirebaseDatabase, // Keep for water logs
    private val userRepository: UserRepository // Inject UserRepository
) : ViewModel() {

    private val _todaysLog = mutableStateListOf<WaterLogEntry>()
    val todaysLog: List<WaterLogEntry> get() = _todaysLog

    private val _totalIntakeToday = MutableStateFlow(0)
    val totalIntakeToday: StateFlow<Int> = _totalIntakeToday.asStateFlow()

    // Get the daily goal directly from the shared UserRepository
    val dailyGoalMl: StateFlow<Int> = userRepository.calculatedDailyWaterIntake

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()

    private var waterLogListener: ValueEventListener? = null
    private var currentHomeUserId: String? = null

    init {
        // Observe dailyGoalMl changes from the repository to recalculate streak
        viewModelScope.launch {
            dailyGoalMl.collectLatest { goal ->
                // Only recalculate if a user is active and goal actually changed (or on init)
                if (currentHomeUserId != null) {
                    recalculateTotalsAndUpdateStreak()
                }
            }
        }

        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                if (currentHomeUserId != user.uid) {
                    cleanupPreviousUserListeners()
                    currentHomeUserId = user.uid
                    initializeDataForUser(user.uid)
                }
            } else {
                cleanupPreviousUserListeners()
                currentHomeUserId = null
                clearHomeUserData()
            }
        }
        firebaseAuth.currentUser?.uid?.let { userId ->
            if (currentHomeUserId == null) {
                currentHomeUserId = userId
                initializeDataForUser(userId)
            }
        }
    }

    private fun initializeDataForUser(userId: String) {
        startListeningForWaterLogs(userId)
        // Streak will be recalculated when dailyGoalMl emits or logs change
    }

    private fun clearHomeUserData() {
        _todaysLog.clear()
        _totalIntakeToday.value = 0
        _currentStreak.value = 0
        // dailyGoalMl will automatically reset via userRepository if user logs out
    }

    // ... (rest of HomeViewModel: startListeningForWaterLogs, addWater, removeLogEntry,
    //      recalculateTotalsAndUpdateStreak, calculateStreakInternal, getStartOfDayMillis, getEndOfDayMillis,
    //      cleanupPreviousUserListeners, onCleared)
    // IMPORTANT: Ensure calculateStreakInternal() uses `dailyGoalMl.value`

    private fun cleanupPreviousUserListeners() {
        currentHomeUserId?.let { userId ->
            waterLogListener?.let { listener ->
                firebaseDatabase.getReference("waterLogs").child(userId).removeEventListener(listener)
            }
        }
        waterLogListener = null
    }

    override fun onCleared() {
        super.onCleared()
        cleanupPreviousUserListeners()
    }

    private fun startListeningForWaterLogs(userId: String) {
        cleanupPreviousUserListeners() // Ensure old listeners are removed

        val todayStartMillis = getStartOfDayMillis(System.currentTimeMillis())
        val todayEndMillis = getEndOfDayMillis(System.currentTimeMillis())

        val waterLogsRef = firebaseDatabase.getReference("waterLogs")
            .child(userId)
            .orderByChild("timestamp")
            .startAt(todayStartMillis.toDouble())
            .endAt(todayEndMillis.toDouble())

        waterLogListener = object : ValueEventListener { // Assign directly
            override fun onDataChange(snapshot: DataSnapshot) {
                val newLogs = mutableListOf<WaterLogEntry>()
                snapshot.children.forEach { logSnapshot ->
                    logSnapshot.getValue(WaterLogEntry::class.java)?.let { newLogs.add(it) }
                }
                _todaysLog.clear()
                _todaysLog.addAll(newLogs.sortedByDescending { it.timestamp })
                recalculateTotalsAndUpdateStreak()
            }

            override fun onCancelled(error: DatabaseError) {
                System.err.println("Failed to load water logs: ${error.message}")
                _todaysLog.clear()
                recalculateTotalsAndUpdateStreak()
            }
        }
        waterLogsRef.addValueEventListener(waterLogListener!!)
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            val userId = currentHomeUserId ?: return@launch
            if (amountMl <= 0) return@launch

            val currentTime = System.currentTimeMillis()
            val newEntry = WaterLogEntry(
                id = UUID.randomUUID().toString(),
                amountMl = amountMl,
                timestamp = currentTime,
                timeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(currentTime)),
                userId = userId
            )
            firebaseDatabase.getReference("waterLogs").child(userId).child(newEntry.id)
                .setValue(newEntry)
        }
    }

    fun removeLogEntry(entry: WaterLogEntry) {
        viewModelScope.launch {
            val userId = currentHomeUserId ?: return@launch
            firebaseDatabase.getReference("waterLogs").child(userId).child(entry.id)
                .removeValue()
        }
    }


    private fun recalculateTotalsAndUpdateStreak() {
        _totalIntakeToday.value = _todaysLog.sumOf { it.amountMl }
        calculateStreakInternal()
    }

    private fun calculateStreakInternal() {
        val userId = currentHomeUserId
        if (userId == null) {
            _currentStreak.value = 0
            return
        }
        val currentGoal = dailyGoalMl.value // Use the value from the StateFlow

        firebaseDatabase.getReference("waterLogs").child(userId)
            .orderByChild("timestamp")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val allUserLogs = mutableListOf<WaterLogEntry>()
                    snapshot.children.forEach { logSnapshot ->
                        logSnapshot.getValue(WaterLogEntry::class.java)?.let { allUserLogs.add(it) }
                    }

                    if (allUserLogs.isEmpty()) {
                        _currentStreak.value = 0
                        return
                    }

                    val loggedDaysMetGoal = allUserLogs
                        .groupBy { getStartOfDayMillis(it.timestamp) }
                        .mapValues { entry -> entry.value.sumOf { log -> log.amountMl } }
                        .filterValues { dailyTotal -> dailyTotal >= currentGoal }
                        .keys
                        .sortedDescending()

                    if (loggedDaysMetGoal.isEmpty()) {
                        _currentStreak.value = 0
                        return
                    }

                    var streak = 0
                    val todayStart = getStartOfDayMillis(System.currentTimeMillis())
                    val calendar = Calendar.getInstance()

                    for (i in loggedDaysMetGoal.indices) {
                        calendar.timeInMillis = todayStart
                        calendar.add(Calendar.DAY_OF_YEAR, -i)
                        val expectedDayForStreak = calendar.timeInMillis

                        if (i < loggedDaysMetGoal.size && loggedDaysMetGoal[i] == expectedDayForStreak) {
                            streak++
                        } else {
                            break
                        }
                    }
                    _currentStreak.value = streak
                }

                override fun onCancelled(error: DatabaseError) {
                    System.err.println("Failed to calculate streak: ${error.message}")
                    _currentStreak.value = 0
                }
            })
    }

    private fun getStartOfDayMillis(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfDayMillis(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }
}
