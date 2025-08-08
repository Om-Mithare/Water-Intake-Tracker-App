// FileName: MultipleFiles/SignUpViewModel.kt (Modified)
package Screens.SignUp

import android.content.Context // Import Context
import android.content.SharedPreferences // Import SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.waterintaketracker.Models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext // Import ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val auth: FirebaseAuth, // Inject FirebaseAuth directly
    private val dbRef: FirebaseDatabase, // Inject FirebaseDatabase directly
    @ApplicationContext private val context: Context // Inject ApplicationContext
) : ViewModel() {

    private val _Signinstate = MutableStateFlow<String?>(null)
    val Signinstate = _Signinstate.asStateFlow()

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    fun SignIn(user: Users, navController: NavController) {
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(user.emailid, user.password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        // Use dbRef directly as it's injected
                        dbRef.getReference("users").child(uid).setValue(user).addOnSuccessListener {
                            _Signinstate.value = "Success"
                            // Save login state to SharedPreferences
                            sharedPreferences.edit().putBoolean("is_logged_in", true).apply()
                            navController.navigate("gender") {
                                // Clear back stack to prevent going back to login/signup
                                popUpTo("start") { inclusive = true }
                            }
                        }.addOnFailureListener {
                            _Signinstate.value = "Failed to save user: ${it.message}"
                        }
                    } else {
                        _Signinstate.value = "Signup Failed: ${task.exception?.message}"
                    }
                }
        }
    }
}
