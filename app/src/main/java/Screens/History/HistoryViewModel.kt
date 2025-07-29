package com.example.yourappname.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class DailyData(
    val dayName: String,
    val amount: Int
)

data class WaterLogEntry(
    val amountMl: Int = 0,
    val timestamp: Long = 0
)

class HistoryViewModel(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        object Empty : UiState()
        data class Error(val message: String) : UiState()
        data class Success(
            val weeklyData: List<DailyData>,
            val monthlyAverage: Int,
            val goal: Int,
            val completionRate: Float,
            val avgDailyIntake: Int,
            val drinkFrequency: Float
        ) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var waterLogListener: ValueEventListener? = null
    private var currentUserId: String? = null

    init {
        viewModelScope.launch {
            currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId != null) {
                setupRealtimeListener(currentUserId!!)
                loadHydrationData(currentUserId!!)
            } else {
                _uiState.value = UiState.Error("User not logged in.")
            }
        }
    }

    private fun setupRealtimeListener(userId: String) {
        waterLogListener?.let { listener ->
            firebaseDatabase.getReference("waterLogs/$userId").removeEventListener(listener)
        }

        waterLogListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                loadHydrationData(userId)
            }

            override fun onCancelled(error: DatabaseError) {
                _uiState.value = UiState.Error("Database error: ${error.message}")
            }
        }

        firebaseDatabase.getReference("waterLogs/$userId")
            .addValueEventListener(waterLogListener!!)
    }

    fun reloadData() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val userId = currentUserId ?: firebaseAuth.currentUser?.uid
            if (userId != null) {
                currentUserId = userId
                if (waterLogListener == null) {
                    setupRealtimeListener(userId)
                }
                loadHydrationData(userId)
            } else {
                _uiState.value = UiState.Error("User not logged in. Cannot reload data.")
            }
        }
    }

    private fun loadHydrationData(userId: String) {
        viewModelScope.launch {
            try {
                val goalSnapshot = firebaseDatabase.getReference("users/$userId/dailyGoalMl")
                    .get().await()
                val goal = goalSnapshot.getValue(Int::class.java) ?: 2500

                val thirtyDaysAgo = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -30)
                }.timeInMillis

                val logsSnapshot = firebaseDatabase.getReference("waterLogs/$userId")
                    .orderByChild("timestamp")
                    .startAt(thirtyDaysAgo.toDouble())
                    .get().await()

                val logs = logsSnapshot.children.mapNotNull { it.getValue(WaterLogEntry::class.java) }

                if (logs.isEmpty()) {
                    _uiState.value = UiState.Empty
                    return@launch
                }

                val weeklyData = processWeeklyData(logs)
                val monthlyAverage = calculateMonthlyAverage(logs)

                val (avgDailyIntakeForWeek, daysGoalMetInWeek) = calculateWeeklyStats(weeklyData, goal)
                val completionRateForWeek = if (weeklyData.isNotEmpty()) daysGoalMetInWeek.toFloat() / weeklyData.size else 0f
                val avgDrinksPerDayOverPeriod = calculateDrinkFrequency(logs)

                _uiState.value = UiState.Success(
                    weeklyData = weeklyData,
                    monthlyAverage = monthlyAverage,
                    goal = goal,
                    completionRate = completionRateForWeek,
                    avgDailyIntake = avgDailyIntakeForWeek,
                    drinkFrequency = avgDrinksPerDayOverPeriod
                )

            } catch (e: Exception) {
                _uiState.value = UiState.Error("Loading hydration data failed: ${e.message}")
            }
        }
    }

    private fun calculateWeeklyStats(weeklyLogData: List<DailyData>, goal: Int): Pair<Int, Int> {
        if (weeklyLogData.isEmpty()) return Pair(0, 0)
        val totalIntake = weeklyLogData.sumOf { it.amount }
        val daysGoalMet = weeklyLogData.count { it.amount >= goal }
        val averageIntake = totalIntake / weeklyLogData.size
        return Pair(averageIntake, daysGoalMet)
    }

    private fun calculateDrinkFrequency(logs: List<WaterLogEntry>): Float {
        if (logs.isEmpty()) return 0f
        val distinctDaysWithLogs = logs.groupBy { log ->
            Calendar.getInstance().apply {
                timeInMillis = log.timestamp
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }.size.toFloat()
        return if (distinctDaysWithLogs > 0) logs.size.toFloat() / distinctDaysWithLogs else 0f
    }

    private fun processWeeklyData(logs: List<WaterLogEntry>): List<DailyData> {
        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return (0..6).map { daysBack ->
            val targetDay = Calendar.getInstance().apply {
                timeInMillis = today.timeInMillis
                add(Calendar.DAY_OF_YEAR, -daysBack)
            }
            val dayStart = targetDay.timeInMillis
            val dayEnd = Calendar.getInstance().apply {
                timeInMillis = dayStart
                add(Calendar.DAY_OF_YEAR, 1)
                add(Calendar.MILLISECOND, -1)
            }.timeInMillis

            val amountForDay = logs.filter { it.timestamp in dayStart..dayEnd }
                .sumOf { it.amountMl }
            DailyData(
                dayName = dateFormat.format(Date(dayStart)).take(3),
                amount = amountForDay
            )
        }.reversed()
    }

    private fun calculateMonthlyAverage(logs: List<WaterLogEntry>): Int {
        if (logs.isEmpty()) return 0
        val dailySums = logs.groupBy { log ->
            Calendar.getInstance().apply {
                timeInMillis = log.timestamp
                set(Calendar.HOUR_OF_DAY, 0)
            }.timeInMillis
        }.mapValues { entry ->
            entry.value.sumOf { it.amountMl }
        }
        return if (dailySums.isNotEmpty()) dailySums.values.average().toInt() else 0
    }

    override fun onCleared() {
        super.onCleared()
        waterLogListener?.let { listener ->
            currentUserId?.let { userId ->
                firebaseDatabase.getReference("waterLogs/$userId").removeEventListener(listener)
            }
        }
        waterLogListener = null
    }
}
