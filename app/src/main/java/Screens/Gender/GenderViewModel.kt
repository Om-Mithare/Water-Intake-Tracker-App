package Screens.Gender.GenderScreen


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class Gender(val label: String) {
    Male("Male"),
    Female("Female"),
    Other("Other")
}

class GenderViewModel : ViewModel() {
    private val _selectedGender = MutableStateFlow<Screens.Gender.Gender?>(null)
    val selectedGender: StateFlow<Screens.Gender.Gender?> = _selectedGender

    fun selectGender(gender: Screens.Gender.Gender) {
        _selectedGender.value = gender
    }
}