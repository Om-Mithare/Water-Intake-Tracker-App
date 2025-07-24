package Screens.Exercise

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

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
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Question Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF1F5FF))
                .border(1.5.dp, Color(0xFF4C84FF), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Text(
                text = "How much exercise do you\ndo each week?",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1C)
                ),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Subtext Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFE9F0FF))
                .padding(16.dp)
        ) {
            Text(
                text = "🏃‍♀️ Your water needs change with how much you move. Select your weekly activity level below.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFF333333)
                )
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Option Buttons
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            exerciseOptions.forEach { option ->
                val isSelected = selectedExercise == option

                val backgroundColor = if (isSelected) {
                    Brush.horizontalGradient(
                        listOf(Color(0xFF4C84FF), Color(0xFF709BFF))
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(Color.White, Color.White)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(backgroundColor)
                        .clickable { viewModel.onExerciseSelected(option) }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Color(0xFF4C84FF) else Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(start = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFF3A3A3A)
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
                    .padding(top = 30.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4C84FF),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Next",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}
