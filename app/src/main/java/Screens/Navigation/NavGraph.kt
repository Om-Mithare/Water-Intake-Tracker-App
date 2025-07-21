package Screens.Navigation

import Screens.Login.LoginScreen
import Screens.SignUp.SignUpScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun NavGraph()
{
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login")
    {
        composable("login")
        {
            LoginScreen(navController)
        }

        composable("signup")
        {
            SignUpScreen(navController)
        }

    }
}