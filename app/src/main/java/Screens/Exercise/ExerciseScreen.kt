package Screens.Exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waterintaketracker.ui.theme.*

@Composable
fun ExerciseScreen(
    viewModel: ExerciseViewModel = viewModel(),
    onNextClick: () -> Unit = {}
) {
    val selectedExercise by viewModel.selectedExercise.collectAsState()

    val exerciseOptions = listOf(
        "🪑 Rarely exercise",
        "💧 Sometimes exercise",
        "💪 Regularly exercise",
        "🏋️‍♂️ Often exercise"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelWaterDarkBackground)
            .padding(horizontal = 20.dp, vertical = 20.dp)
    ) {
        // Small top spacing
        Spacer(modifier = Modifier.height(20.dp))

        // Question Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(PixelShapes.medium)
                .background(PixelWaterDarkSurface)
                .border(2.dp, PixelWaterDarkPrimary, PixelShapes.medium)
                .padding(12.dp)
        ) {
            Text(
                text = "How much exercise\ndo you do weekly?",
                style = PixelTypography.headlineSmall.copy(
                    color = PixelWaterDarkOnSurface
                )
            )
        }

        Spacer(modifier = Modifier.height(26.dp)) // Reduced space

        // Options List
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            exerciseOptions.forEach { option ->
                val isSelected = selectedExercise == option
                val bgColor = if (isSelected) PixelWaterDarkPrimary else PixelWaterDarkSurfaceVariant
                val borderColor = if (isSelected) PixelWaterDarkOnPrimary else PixelWaterDarkOnSurface

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(PixelShapes.medium)
                        .background(bgColor)
                        .clickable { viewModel.onExerciseSelected(option) }
                        .border(2.dp, borderColor, PixelShapes.medium)
                        .padding(start = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = option,
                        style = PixelTypography.titleSmall.copy(
                            color = PixelWaterDarkOnSurface
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Next Button
        AnimatedVisibility(
            visible = selectedExercise.isNotBlank(),
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onNextClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                shape = PixelShapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PixelWaterDarkPrimary,
                    contentColor = PixelWaterDarkOnPrimary
                )
            ) {
                Text(
                    text = "NEXT",
                    style = PixelTypography.labelMedium
                )
            }
        }
    }
}
