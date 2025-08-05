package com.example.waterintaketracker.data // Or your preferred package for data components

import com.example.waterintaketracker.Models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton // Important for a shared repository

@Singleton // Ensures a single instance of this repository
class UserRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseDatabase: FirebaseDatabase
) {
    private val _userProfile = MutableStateFlow<Users?>(null)
    val userProfile: StateFlow<Users?> = _userProfile.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var userListener: ValueEventListener? = null
    private var currentUserId: String? = null

    // Use a custom scope for repository operations that might outlive a ViewModel
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val calculatedDailyWaterIntake: StateFlow<Int> = userProfile.map { profile ->
        calculateGoal(profile)
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000), // Keep active for 5s after last collector
        initialValue = 2500 // Default initial value
    )

    init {
        firebaseAuth.addAuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                if (currentUserId != firebaseUser.uid) {
                    currentUserId = firebaseUser.uid
                    listenToUserProfile(firebaseUser.uid)
                }
            } else {
                cleanupListener()
                currentUserId = null
                _userProfile.value = null
                _loading.value = false
            }
        }
        // Initial check
        firebaseAuth.currentUser?.uid?.let { userId ->
            if (currentUserId == null) {
                currentUserId = userId
                listenToUserProfile(userId)
            }
        } ?: run {
            _loading.value = false
        }
    }

    private fun listenToUserProfile(userId: String) {
        _loading.value = true
        _errorMessage.value = null
        cleanupListener()

        val userRef = firebaseDatabase.getReference("users").child(userId)
        userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(Users::class.java)
                _userProfile.value = user
                _loading.value = false
                _errorMessage.value = if (user == null) "Profile not found." else null
            }

            override fun onCancelled(error: DatabaseError) {
                _loading.value = false
                _errorMessage.value = "Failed to load profile: ${error.message}"
                _userProfile.value = null
            }
        }
        userRef.addValueEventListener(userListener!!)
    }

    fun updateProfileField(field: String, value: Any, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = currentUserId ?: run {
            onFailure(IllegalStateException("User not logged in."))
            return
        }
        _loading.value = true
        firebaseDatabase.getReference("users").child(userId).child(field).setValue(value)
            .addOnSuccessListener {
                _loading.value = false // Listener will update profile, but stop loading indicator here
                onSuccess()
            }
            .addOnFailureListener { e ->
                _loading.value = false
                onFailure(e)
            }
    }

    private fun calculateGoal(profile: Users?): Int {
        val weight = profile?.weight ?: 0
        if (profile == null || weight <= 0) {
            return 2500 // Default goal
        }
        return when (profile.gender?.trim()?.lowercase()) {
            "male" -> (weight * 35)
            "female" -> (weight * 31)
            else -> (weight * 33)
        }.coerceAtLeast(500)
    }

    fun logout(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        repositoryScope.launch { // Use repositoryScope for operations like logout
            try {
                firebaseAuth.signOut() // Auth listener will handle cleanup
                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    private fun cleanupListener() {
        currentUserId?.let { userId ->
            userListener?.let { listener ->
                firebaseDatabase.getReference("users").child(userId).removeEventListener(listener)
            }
        }
        userListener = null
    }

    // Call this if the repository itself is being cleared/destroyed, though with @Singleton it lives with app
    fun clear() {
        cleanupListener()
        repositoryScope.cancel()
    }
}
