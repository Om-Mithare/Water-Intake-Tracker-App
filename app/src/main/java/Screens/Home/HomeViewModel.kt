package Screens.Home

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waterintaketracker.Models.WaterLogEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

// For HistoryScreen's graph
data class IntakeGraphPoint(
    val timeLabel: String, // e.g., "10 AM", "2 PM"
    val amountNormalised: Float, // 0.0 to 1.0 for bar height
    val amountActual: Int // e.g., 250ml
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) : ViewModel() {

    private val _todaysLog = mutableStateListOf<WaterLogEntry>()
    val todaysLog: List<WaterLogEntry> = _todaysLog

    private val _totalIntakeToday = MutableStateFlow(0)
    val totalIntakeToday: StateFlow<Int> = _totalIntakeToday.asStateFlow()

    private val _dailyGoalMl = MutableStateFlow(2500) // Default goal
    val dailyGoalMl: StateFlow<Int> = _dailyGoalMl.asStateFlow()

    private val _todayIntakeGraphData = MutableStateFlow<List<IntakeGraphPoint>>(emptyList())
    val todayIntakeGraphData: StateFlow<List<IntakeGraphPoint>> = _todayIntakeGraphData.asStateFlow()

    private var waterLogListener: ValueEventListener? = null
    private var dailyGoalListener: ValueEventListener? = null

    init {
        // Observe authentication state to load data for the current user
        firebaseAuth.addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                val userId = auth.currentUser!!.uid
                startListeningForWaterLogs(userId)
                startListeningForDailyGoal(userId)
            } else {
                stopListeningForWaterLogs()
                stopListeningForDailyGoal()
                _todaysLog.clear()
                _dailyGoalMl.value = 2500 // Reset to default if logged out
                recalculateTotalsAndGraph()
            }
        }
        // If user is already logged in on app start, load data
        firebaseAuth.currentUser?.let {
            startListeningForWaterLogs(it.uid)
            startListeningForDailyGoal(it.uid)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningForWaterLogs()
        stopListeningForDailyGoal()
    }

    private fun startListeningForWaterLogs(userId: String) {
        stopListeningForWaterLogs() // Ensure only one listener is active

        val todayStartMillis = getStartOfDayMillis(System.currentTimeMillis())
        val todayEndMillis = todayStartMillis + (24 * 60 * 60 * 1000) - 1 // End of today

        val waterLogsRef = firebaseDatabase.getReference("waterLogs")
            .child(userId)
            .orderByChild("timestamp")
            .startAt(todayStartMillis.toDouble())
            .endAt(todayEndMillis.toDouble())

        waterLogListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newLogs = mutableListOf<WaterLogEntry>()
                for (logSnapshot in snapshot.children) {
                    val logEntry = logSnapshot.getValue(WaterLogEntry::class.java)
                    logEntry?.let { newLogs.add(it) }
                }
                // Update _todaysLog and trigger recalculation
                _todaysLog.clear()
                _todaysLog.addAll(newLogs.sortedByDescending { it.timestamp }) // Sort by newest first
                recalculateTotalsAndGraph()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                println("Failed to load water logs: ${error.message}")
            }
        }
        waterLogsRef.addValueEventListener(waterLogListener!!)
    }

    private fun stopListeningForWaterLogs() {
        waterLogListener?.let {
            firebaseAuth.currentUser?.uid?.let { userId ->
                firebaseDatabase.getReference("waterLogs").child(userId).removeEventListener(it)
            }
            waterLogListener = null
        }
    }

    private fun startListeningForDailyGoal(userId: String) {
        stopListeningForDailyGoal() // Ensure only one listener is active

        val userGoalRef = firebaseDatabase.getReference("users").child(userId).child("dailyGoalMl")

        dailyGoalListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val goal = snapshot.getValue(Int::class.java)
                _dailyGoalMl.value = goal ?: 2500 // Default if not found
                recalculateTotalsAndGraph()
            }

            override fun onCancelled(error: DatabaseError) {
                println("Failed to load daily goal: ${error.message}")
            }
        }
        userGoalRef.addValueEventListener(dailyGoalListener!!)
    }

    private fun stopListeningForDailyGoal() {
        dailyGoalListener?.let {
            firebaseAuth.currentUser?.uid?.let { userId ->
                firebaseDatabase.getReference("users").child(userId).child("dailyGoalMl").removeEventListener(it)
            }
            dailyGoalListener = null
        }
    }


    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.uid ?: run {
                // Handle case where user is not logged in (e.g., show a message)
                println("User not logged in. Cannot add water.")
                return@launch
            }

            val currentTime = System.currentTimeMillis()
            val timeFormatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(currentTime))
            val newEntry = WaterLogEntry(
                id = UUID.randomUUID().toString(),
                amountMl = amountMl,
                timestamp = currentTime,
                timeFormatted = timeFormatted,
                userId = userId // Assign current user's ID
            )

            firebaseDatabase.getReference("waterLogs")
                .child(userId)
                .child(newEntry.id)
                .setValue(newEntry)
                .addOnSuccessListener {
                    println("Water log added successfully to Firebase!")
                    // The ValueEventListener will automatically update _todaysLog
                }
                .addOnFailureListener { e ->
                    println("Failed to add water log to Firebase: ${e.message}")
                }
        }
    }

    fun removeLogEntry(entry: WaterLogEntry) {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.uid ?: run {
                println("User not logged in. Cannot remove water log.")
                return@launch
            }

            firebaseDatabase.getReference("waterLogs")
                .child(userId)
                .child(entry.id)
                .removeValue()
                .addOnSuccessListener {
                    println("Water log removed successfully from Firebase!")
                    // The ValueEventListener will automatically update _todaysLog
                }
                .addOnFailureListener { e ->
                    println("Failed to remove water log from Firebase: ${e.message}")
                }
        }
    }

    fun setDailyGoal(newGoal: Int) {
        _dailyGoalMl.value = newGoal.coerceAtLeast(0)
        // Save the daily goal to Firebase for the user
        firebaseAuth.currentUser?.uid?.let { userId ->
            firebaseDatabase.getReference("users").child(userId).child("dailyGoalMl").setValue(newGoal)
                .addOnSuccessListener { println("Daily goal updated in Firebase.") }
                .addOnFailureListener { e -> println("Failed to update daily goal: ${e.message}") }
        }
        recalculateTotalsAndGraph() // Recalculate graph as goal might affect normalization
    }


    private fun recalculateTotalsAndGraph() {
        val total = _todaysLog.sumOf { it.amountMl }
        _totalIntakeToday.value = total
        generateIntakeGraphData()
    }

    private fun generateIntakeGraphData() {
        if (_todaysLog.isEmpty()) {
            _todayIntakeGraphData.value = emptyList()
            return
        }

        val sortedLogs = _todaysLog.sortedBy { it.timestamp }
        // Normalize against the daily goal if it's greater than any single log entry,
        // otherwise normalize against the max single log entry for better bar representation.
        val maxIntakeInSingleLog = sortedLogs.maxOfOrNull { it.amountMl }?.toFloat() ?: _dailyGoalMl.value.toFloat()
        val normalizationBase = maxOf(maxIntakeInSingleLog, _dailyGoalMl.value.toFloat())

        if (normalizationBase == 0f) { // Avoid division by zero
            _todayIntakeGraphData.value = emptyList()
            return
        }

        _todayIntakeGraphData.value = sortedLogs.map { log ->
            IntakeGraphPoint(
                timeLabel = log.timeFormatted,
                amountNormalised = (log.amountMl.toFloat() / normalizationBase).coerceIn(0.05f, 1f), // min 5% height
                amountActual = log.amountMl
            )
        }
    }

    private fun getStartOfDayMillis(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
