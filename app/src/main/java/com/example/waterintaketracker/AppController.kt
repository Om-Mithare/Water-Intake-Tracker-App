package com.example.waterintaketracker

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppController : Application()
{
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}