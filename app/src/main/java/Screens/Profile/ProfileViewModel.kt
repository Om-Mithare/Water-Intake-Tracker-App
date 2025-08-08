package Screens.Profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waterintaketracker.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val userProfile = userRepository.userProfile
    val loading = userRepository.loading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun logout() {
        viewModelScope.launch {
            try {
                userRepository.logout(
                    onSuccess = {
                        userRepository.clearLoginState()
                    },
                    onFailure = { e ->
                        _errorMessage.value = "Logout failed: ${e.message}"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Logout error: ${e.message}"
            }
        }
    }

    fun updateProfileField(field: String, value: Any) {
        viewModelScope.launch {
            try {
                userRepository.updateProfileField(
                    field = field,
                    value = value,
                    onSuccess = { /* Success handled by state flows */ },
                    onFailure = { e ->
                        _errorMessage.value = "Update failed: ${e.message}"
                    }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Update error: ${e.message}"
            }
        }
    }
}
