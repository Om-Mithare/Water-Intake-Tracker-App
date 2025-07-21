package com.example.waterintaketracker.Models

data class Users (
    val emailid: String = "",
    val password: String = "",
    val username: String = "",
    val phoneNumber: String = "",
    val profileImage: String = "",
    val weight : Int = 0
)
