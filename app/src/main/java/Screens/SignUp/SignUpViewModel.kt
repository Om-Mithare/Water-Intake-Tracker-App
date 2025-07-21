package Screens.SignUp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.waterintaketracker.Models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor() : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().getReference("users")

    private val _Signinstate = MutableStateFlow<String?>(null)
    val Signinstate = _Signinstate.asStateFlow()

    fun SignIn(user: Users, navController: NavController) {
        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(user.emailid, user.password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        dbRef.child(uid).setValue(user).addOnSuccessListener {
                            _Signinstate.value = "Success"
                            navController.navigate("profile")
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
