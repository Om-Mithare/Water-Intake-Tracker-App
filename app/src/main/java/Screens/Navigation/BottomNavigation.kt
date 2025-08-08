package com.example.waterintaketracker

import Screens.Home.HomeScreen
import Screens.Profile.ProfileScreen
import Screens.Profile.ProfileViewModel
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.waterintaketracker.Models.NavItems
import com.example.waterintaketracker.ViewModels.HomeViewModel
import com.example.yourappname.ui.history.HistoryScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BottomNav(
    navController: NavController,
    homeViewModel: HomeViewModel,
    profileViewModel: ProfileViewModel
) {
    val navItemsList = listOf(
        NavItems("Home", Icons.Filled.Home),
        NavItems("History", Icons.Default.Refresh),
        NavItems("Profile", Icons.Default.Person)
    )

    var selectedIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                navItemsList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                        },
                        icon = { Icon(navItem.icon, "") },
                        label = { Text(text = navItem.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        contentScreen(
            modifier = Modifier.padding(innerPadding),
            selectedIndex = selectedIndex,
            navController = navController,
            homeViewModel = homeViewModel,
            profileViewModel = profileViewModel
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun contentScreen(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    navController: NavController,
    homeViewModel: HomeViewModel,
    profileViewModel: ProfileViewModel
) {
    when (selectedIndex) {
        0 -> HomeScreen(navController = navController, modifier = modifier, homeViewModel = homeViewModel)
        1 -> HistoryScreen()
        2 -> ProfileScreen(navController = navController)
    }
}
