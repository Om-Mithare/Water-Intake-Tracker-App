package Screens.Login

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.waterintaketracker.Models.Users
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LogInViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _loginState = MutableStateFlow<String?>(null)
    val loginstate: StateFlow<String?> = _loginState

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    fun Login(users: Users, navController: NavController) {
        auth.signInWithEmailAndPassword(users.emailid, users.password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _loginState.value = "Login Success"
                // Save login state to SharedPreferences
                sharedPreferences.edit().putBoolean("is_logged_in", true).apply()
                navController.navigate("bottom") {
                    // Clear back stack to prevent going back to login/signup
                    popUpTo("start") { inclusive = true }
                }
            } else {
                _loginState.value = task.exception?.message
            }
        }
    }
}
