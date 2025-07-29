package Screens.Navigation

import Screens.Home.HomeScreen
import Screens.Home.HomeViewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.waterintaketracker.Models.NavItems
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.yourappname.ui.history.HistoryScreen

@Composable
fun BottomNav(
    navController: NavController
) {

    val NavItemslist = listOf(
        NavItems("Home", Icons.Filled.Home),
        NavItems("History", Icons.Default.Refresh),
        NavItems("Profile", Icons.Default.Person)
    )

    var selectedIndex = remember {
        mutableIntStateOf(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavItemslist.forEachIndexed { index, navItems ->
                    NavigationBarItem(
                        selected = selectedIndex.intValue == index ,
                        onClick = {
                            selectedIndex.intValue = index
                        },
                        icon = { Icon(navItems.icon, "") },
                        label = {
                            Text(text = navItems.label)
                        }
                    )
                }


            }
        }
    )
    { innerPadding ->
        contentScreen(modifier = Modifier.padding(innerPadding), selectedIndex.intValue, navController = navController )

    }

}

@Composable
fun contentScreen(modifier: Modifier = Modifier, selectedIndex: Int, navController: NavController) {
    val homeViewModel : HomeViewModel = hiltViewModel()

    when(selectedIndex){
          0 -> HomeScreen(navController, modifier = modifier, homeViewModel = homeViewModel)
          1 -> HistoryScreen()

//        2 -> DisadvantagesScreen()


    }

}