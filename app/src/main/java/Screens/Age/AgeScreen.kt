package com.waterintaketracker.screens.age

import Screens.Profile.ProfileViewModel
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.example.waterintaketracker.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AgeScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel() // Inject ProfileViewModel
) {
    val ages = (0..115).toList()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    // Scroll to default age (e.g. 20)
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            listState.scrollToItem(20) // scroll to age 20 (index 20)
        }
    }

    val selectedAge = remember {
        derivedStateOf {
            val centerIndex = listState.firstVisibleItemIndex + 1
            ages.getOrElse(centerIndex) { 20 }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelWaterDarkBackground)
            .padding(horizontal = 24.dp)
    ) {
        // Question Text
        Text(
            text = "How old are you?",
            style = PixelTypography.displaySmall.copy(
                color = PixelWaterDarkOnSurface,
                fontSize = 28.sp,
                lineHeight = 36.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = screenHeight * 0.08f)
        )

        // Scrollable Picker
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
                itemsIndexed(ages) { index, age ->
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
                        text = "$age",
                        fontSize = fontSize,
                        fontFamily = PressStart2PFamily,
                        fontWeight = if (distance == 0) FontWeight.Bold else FontWeight.Normal,
                        color = PixelWaterDarkOnSurface,
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
                // Save the selected age to Firebase
                profileViewModel.updateProfileField("age", selectedAge.value)
                navController.navigate("weight")
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .fillMaxWidth()
                .height(56.dp),
            shape = PixelShapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = PixelWaterDarkPrimary,
                contentColor = PixelWaterDarkOnPrimary
            )
        ) {
            Text(
                text = "Next",
                style = PixelTypography.labelMedium
            )
        }
    }
}
