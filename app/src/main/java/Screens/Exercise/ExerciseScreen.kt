package Screens.Exercise

import Screens.Profile.ProfileViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.waterintaketracker.ui.theme.* // Make sure this path is correct
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun ExerciseScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    var selectedExercise by remember { mutableStateOf("") }

    val exerciseOptions = listOf(
        "🪑 Rarely exercise",
        "💧 Sometimes exercise",
        "💪 Regularly exercise",
        "🏋️‍♂️ Often exercise"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelWaterDarkBackground) // Consistent background from your theme
            .padding(horizontal = 24.dp, vertical = 32.dp), // Consistent screen padding
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Question/Title
        Text(
            text = "How much exercise\ndo you do weekly?",
            style = MaterialTheme.typography.displaySmall.copy(
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp) // Spacing for the title from the top of the content area
        )

        Spacer(modifier = Modifier.height(32.dp)) // Consistent spacing after the title

        // Options List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp) // Consistent spacing between option boxes
        ) {
            exerciseOptions.forEach { optionText ->
                val isSelected = selectedExercise == optionText

                // Determine colors based on selection state, using your theme colors
                val backgroundColor = if (isSelected) PixelWaterDarkPrimary else PixelWaterDarkSurfaceVariant
                val textColor = if (isSelected) PixelWaterDarkOnPrimary else PixelWaterDarkOnSurface
                val borderColor = if (isSelected) PixelWaterDarkPrimary else PixelWaterDarkOutline // Or another theme color for unselected borders

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp) // Define a consistent height for these selectable boxes
                        .clip(PixelShapes.medium) // Consistent rounded corners from your theme
                        .background(backgroundColor)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp, // Slightly thicker border when selected
                            color = borderColor,
                            shape = PixelShapes.medium
                        )
                        .clickable {
                            selectedExercise = optionText
                            // Update the ViewModel with the selected exercise level
                            profileViewModel.updateProfileField("exerciseLevel", optionText)
                        }
                        .padding(horizontal = 20.dp), // Internal padding for the content of the box
                    contentAlignment = Alignment.CenterStart // Align text and icon to the start (left)
                ) {
                    Text(
                        text = optionText,
                        style = PixelTypography.titleMedium.copy(color = textColor), // Consistent text style from your theme
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f)) // This pushes the button to the bottom of the screen

        // Next Button - appears when an option is selected
        AnimatedVisibility(
            visible = selectedExercise.isNotBlank(), // Button is visible only when an option is selected
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }), // Animation for appearance
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    // Navigate to the next screen, e.g., your main app screen with bottom navigation
                    navController.navigate("bottom")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), // Define a consistent height for primary action buttons
                shape = PixelShapes.medium, // Consistent button shape from your theme
                colors = ButtonDefaults.buttonColors(
                    containerColor = PixelWaterDarkPrimary, // Consistent button color from your theme
                    contentColor = PixelWaterDarkOnPrimary    // Consistent button text color from your theme
                )
            ) {
                Text(
                    text = "NEXT",
                    style = PixelTypography.labelLarge // Or labelMedium, ensure consistency with other buttons
                )
            }
        }
    }
}
