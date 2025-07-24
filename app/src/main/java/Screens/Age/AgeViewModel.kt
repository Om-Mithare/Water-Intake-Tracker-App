package com.waterintaketracker.screens.age

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AgeViewModel : ViewModel() {

    private val _age = MutableStateFlow("")
    val age: StateFlow<String> = _age

    fun onAgeChange(newAge: String) {
        if (newAge.length <= 3 && newAge.all { it.isDigit() }) {
            _age.value = newAge
        }
    }
}
