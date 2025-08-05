package Screens.Profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waterintaketracker.Models.Users // Your Users model
import com.example.waterintaketracker.data.UserRepository // Import UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository // Inject UserRepository
) : ViewModel() {

    val userProfile: StateFlow<Users?> = userRepository.userProfile
    val loading: StateFlow<Boolean> = userRepository.loading
    val errorMessage: StateFlow<String?> = userRepository.errorMessage // Expose error message

    // You can also directly expose the calculated goal if needed on the profile screen itself
    val dailyGoal: StateFlow<Int> = userRepository.calculatedDailyWaterIntake

    private val _updateMessage = MutableStateFlow<String?>(null)
    val updateMessage: StateFlow<String?> = _updateMessage.asStateFlow()


    fun updateProfileField(field: String, value: Any) {
        viewModelScope.launch {
            userRepository.updateProfileField(field, value,
                onSuccess = {
                    _updateMessage.value = "$field updated successfully."
                    // Message will clear after a delay (see below)
                },
                onFailure = { e ->
                    _updateMessage.value = "Failed to update $field: ${e.message}"
                }
            )
        }
    }

    fun clearUpdateMessage() {
        _updateMessage.value = null
    }

    fun logout(onSuccess: () -> Unit) {
        userRepository.logout(
            onSuccess = onSuccess,
            onFailure = { e ->
                // Handle logout failure if necessary, e.g., show a message
                _updateMessage.value = "Logout failed: ${e.message}"
            }
        )
    }
}
