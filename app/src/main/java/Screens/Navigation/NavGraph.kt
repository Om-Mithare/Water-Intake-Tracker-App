package Screens.Navigation

import Screens.Celebration.CelebrationScreen
import Screens.Exercise.ExerciseScreen
import Screens.Gender.GenderScreen
import Screens.Login.LoginScreen
import Screens.Profile.ProfileViewModel
import Screens.SignUp.SignUpScreen
import Screens.Sleep.SleepScreen
import Screens.Wakeup.WakeupTimeScreen
import Screens.Weight.WeightScreen
import Screens.Home.HomeScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.waterintaketracker.BottomNav
import com.example.waterintaketracker.ViewModels.HomeViewModel
import Loading.LoadingScreen
import com.waterintaketracker.screens.age.AgeScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(isUserLoggedIn: Boolean) {
    val navController = rememberNavController()
    var isLoadingComplete by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = "start"
    ) {
        composable("start") {
            LoadingScreen(onFinished = {
                isLoadingComplete = true
                navController.navigate(if (isUserLoggedIn) "bottom" else "login") {
                    popUpTo("start") { inclusive = true }
                }
            })
        }

        composable("login") {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate("bottom") {
                        popUpTo("start") { inclusive = true }
                    }
                }
            )
        }

        composable("signup") {
            SignUpScreen(
                navController = navController,
                onSignUpSuccess = {
                    navController.navigate("gender") {
                        popUpTo("start") { inclusive = true }
                    }
                }
            )
        }

        composable("bottom") {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val homeViewModel: HomeViewModel = hiltViewModel()
            BottomNav(navController, homeViewModel, profileViewModel)
        }

        composable("gender") {
            val profileViewModel: ProfileViewModel = hiltViewModel()
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

        // 🎉 Celebration screen — full-screen, no bottom nav
        composable("celebration") {
            CelebrationScreen(
                onFinished = {
                    navController.popBackStack() // Back to home
                }
            )
        }
    }
}
