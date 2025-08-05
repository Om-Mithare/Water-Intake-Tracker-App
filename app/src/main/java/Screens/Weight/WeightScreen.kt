package Screens.Weight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import Screens.Profile.ProfileViewModel // Import your ProfileViewModel

@Composable
fun WeightScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel() // Inject ProfileViewModel
) {
    val weights = (0..150).toList()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    // Scroll to default weight
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            listState.scrollToItem(60) // Scroll to weight 60 (index 60)
        }
    }

    val selectedWeight = remember {
        derivedStateOf {
            val centerIndex = listState.firstVisibleItemIndex + 1
            weights.getOrElse(centerIndex) { 60 }
        }
    }

    // Theme colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 24.dp)
    ) {
        // Question Text
        Text(
            text = "How much do you weigh?",
            style = MaterialTheme.typography.displayMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 42.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = screenHeight * 0.1f)
        )

        // Centered number list
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .align(Alignment.Center)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(weights) { index, weight ->
                    val centerIndex = listState.firstVisibleItemIndex + 1
                    val distance = kotlin.math.abs(index - centerIndex)

                    val fontSize = when (distance) {
                        0 -> 44.sp
                        1 -> 28.sp
                        2 -> 20.sp
                        else -> 14.sp
                    }

                    val alpha = when (distance) {
                        0 -> 1f
                        1 -> 0.6f
                        2 -> 0.3f
                        else -> 0.1f
                    }

                    Text(
                        text = "$weight",
                        fontSize = fontSize,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (distance == 0) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(alpha)
                            .padding(vertical = 10.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Next Button
        Button(
            onClick = {
                // Save the selected weight to Firebase
                profileViewModel.updateProfileField("weight", selectedWeight.value)
                navController.navigate("wakeup")
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 42.dp)
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryColor,
                contentColor = onPrimaryColor
            )
        ) {
            Text(
                text = "Next",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
