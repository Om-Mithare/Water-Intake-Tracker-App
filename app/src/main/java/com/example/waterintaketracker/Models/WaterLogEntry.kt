package com.example.waterintaketracker.Models

data class WaterLogEntry(
    val id: String = "",
    val amountMl: Int = 0, // Amount of water in milliliters
    val timestamp: Long = 0, // Timestamp of the log entry
    val timeFormatted: String = "", // Formatted time string (e.g., "8:00 AM")
    val userId: String = "" // User ID associated with the log entry
)
