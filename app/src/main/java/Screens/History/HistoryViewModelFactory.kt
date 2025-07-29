package Screens.History
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.yourappname.ui.history.HistoryViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class HistoryViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HistoryViewModel(
            firebaseAuth = FirebaseAuth.getInstance(),
            firebaseDatabase = FirebaseDatabase.getInstance()
        ) as T
    }
}
