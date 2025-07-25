package Screens.Wakeup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.waterintaketracker.ui.theme.PixelTypography

@Composable
fun WakeupTimeScreen(
    onNextClick: () -> Unit = {}
) {
    var selectedHour by remember { mutableStateOf("08") }
    var selectedMinute by remember { mutableStateOf("00") }
    var selectedPeriod by remember { mutableStateOf("AM") }

    val hours = (1..12).map { it.toString().padStart(2, '0') }
    val minutes = listOf("00", "05", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55")
    val periods = listOf("AM", "PM")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 82.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Question
        Text(
            text = "When do you usually wake up?",
            style = MaterialTheme.typography.displaySmall.copy(
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(80.dp)) // space before time selection

        // Time selection row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeDropdown(value = selectedHour, options = hours) { selectedHour = it }
            Text(
                ":",
                style = MaterialTheme.typography.displayMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            TimeDropdown(value = selectedMinute, options = minutes) { selectedMinute = it }
            TimeDropdown(value = selectedPeriod, options = periods) { selectedPeriod = it }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Next Button
        AnimatedVisibility(
            visible = selectedHour.isNotBlank(),
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 },
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = onNextClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Text(
                    "Next",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun TimeDropdown(
    value: String,
    options: List<String>,
    onValueSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(70.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = if (expanded) 2.dp else 1.dp,
                    color = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.medium
                )
                .clickable { expanded = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = if (option == value)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    onClick = {
                        onValueSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
