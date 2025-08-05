package com.example.waterintaketracker.Models

data class Users(
    val emailid: String = "",
    val password: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val profileImage: String = "",
    val weight: Int = 0, // Weight in kg
    val gender: String = "", // Gender ("Male", "Female", "Other")
    val age: Int = 0, // Age in years
    val wakeupTime: String = "", // Wakeup time (e.g., "08:00 AM")
    val sleepTime: String = "", // Sleep time (e.g., "10:00 PM")
    val exerciseLevel: String = "" // Exercise level (e.g., "Regularly exercise")
)
