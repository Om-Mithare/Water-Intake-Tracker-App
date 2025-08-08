package com.example.waterintaketracker.Models

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
fun Date.toLocalDate(): LocalDate {
    return this.toInstant().atZone(TimeZone.getDefault().toZoneId()).toLocalDate()
}

@RequiresApi(Build.VERSION_CODES.O)
fun Long.toLocalDate(): LocalDate {
    return Date(this).toLocalDate()
}
