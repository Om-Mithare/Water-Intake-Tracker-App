package com.example.waterintaketracker.Models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class WaterLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val amountMl: Int = 0, // Initialize with a default value for Firebase deserialization
    val timestamp: Long = System.currentTimeMillis(),
    val timeFormatted: String = "", // Initialize with a default value for Firebase deserialization
    val userId: String = "" // New field to link to user
)
