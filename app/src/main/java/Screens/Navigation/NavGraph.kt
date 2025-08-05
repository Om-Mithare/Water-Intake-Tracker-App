package Screens.Navigation

import Loading.LoadingScreen
import Screens.Exercise.ExerciseScreen
import Screens.Gender.GenderScreen
import Screens.Login.LoginScreen
import Screens.Profile.ProfileViewModel
import Screens.SignUp.SignUpScreen
import Screens.Sleep.SleepScreen
import Screens.Wakeup.WakeupTimeScreen
import Screens.Weight.WeightScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.waterintaketracker.BottomNav
import com.example.waterintaketracker.ViewModels.HomeViewModel
import com.example.waterintaketracker.ui.NotificationSettingsScreen
import com.example.waterintaketracker.ui.NotificationSettingsViewModel
import com.waterintaketracker.screens.age.AgeScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "start"
    ) {
        composable("start") {
            LoadingScreen(onFinished = { navController.navigate("login") })
        }

        composable("login") {
            LoginScreen(navController)
        }

        composable("signup") {
            SignUpScreen(navController)
        }

        composable("bottom") {
            val profileViewModel: ProfileViewModel = hiltViewModel() // Get ProfileViewModel
            val homeViewModel: HomeViewModel = hiltViewModel() // Get HomeViewModel
            BottomNav(navController, homeViewModel, profileViewModel)
        }

        composable("gender") {
            val profileViewModel: ProfileViewModel = hiltViewModel() // Get ProfileViewModel
            GenderScreen(navController = navController, profileViewModel = profileViewModel)
        }

        composable("age") {
            AgeScreen(navController = navController)
        }

        composable("weight") {
            WeightScreen(navController = navController)
        }

        composable("wakeup") {
            WakeupTimeScreen(navController = navController)
        }

        composable("sleep") {
            SleepScreen(navController = navController)
        }

        composable("exercise") {
            ExerciseScreen(navController = navController)
        }

        composable("notification_settings"){
            val viewModel: NotificationSettingsViewModel = hiltViewModel()
            NotificationSettingsScreen(viewModel = viewModel, navController = navController)
        }
    }
}
