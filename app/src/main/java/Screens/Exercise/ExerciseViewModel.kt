package Screens.Exercise


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ExerciseViewModel : ViewModel() {

    private val _selectedExercise = MutableStateFlow("")
    val selectedExercise: StateFlow<String> = _selectedExercise

    fun onExerciseSelected(value: String) {
        _selectedExercise.value = value
    }
}
