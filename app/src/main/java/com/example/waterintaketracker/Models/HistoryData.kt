package com.example.waterintaketracker.Models

import java.util.*
data class HistoryData(
    val weeklyData: List<DailyData>,
    val monthlyAverages: List<MonthlyAverage>,
    val overallStats: OverallStats
)
data class DailyData(
    val date: Date,
    val amount: Int,  // in ml
    val dayOfWeek: String
)
data class MonthlyAverage(
    val monthName: String,
    val averageIntake: Int
)
data class OverallStats(
    val weeklyAverage: Int,
    val monthlyAverage: Int,
    val completionRate: Float,  // 0.0 to 1.0 (percentage)
    val drinkFrequency: Float   // drinks per day average
)