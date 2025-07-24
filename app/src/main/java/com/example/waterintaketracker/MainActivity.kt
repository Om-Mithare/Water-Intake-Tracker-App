package com.example.waterintaketracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.waterintaketracker.ui.theme.WaterIntakeTrackerTheme
import com.waterintaketracker.screens.age.AgeScreen
import dagger.hilt.android.AndroidEntryPoint
import Screens.Exercise.ExerciseScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WaterIntakeTrackerTheme {

                ExerciseScreen()
                }

            }
        }
    }


