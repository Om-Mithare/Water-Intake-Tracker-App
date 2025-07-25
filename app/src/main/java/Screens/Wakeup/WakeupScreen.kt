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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF070B26), Color(0xFF1A213F))
                )
            )
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF2C3555))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF4C84FF), Color(0xFF709BFF))
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Question
        Text(
            text = "When do you usually wake up?",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            lineHeight = 34.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Info Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(5.dp, RoundedCornerShape(16.dp))
                .background(Color(0xFF273661), shape = RoundedCornerShape(16.dp))
                .padding(18.dp)
        ) {
            Text(
                buildAnnotatedString {
                    append("💧 Hydrating ")
                    withStyle(SpanStyle(color = Color(0xFF4C84FF), fontWeight = FontWeight.Bold)) {
                        append("right after waking up")
                    }
                    append(" boosts your ")
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append("morning energy!")
                    }
                },
                fontSize = 16.sp,
                color = Color.White,
                lineHeight = 24.sp,
                letterSpacing = 0.4.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Time selection row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeDropdown(value = selectedHour, options = hours) { selectedHour = it }
            Text(":", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4C84FF),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Text(
                    "Next",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
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
                .width(100.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        if (expanded) listOf(Color(0xFF4C84FF), Color(0xFF709BFF))
                        else listOf(Color(0xFF1F2C4B), Color(0xFF1F2C4B))
                    )
                )
                .border(
                    width = if (expanded) 2.dp else 1.dp,
                    color = if (expanded) Color.White else Color(0xFF4C84FF),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable { expanded = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                fontSize = 22.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
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
                            fontSize = 18.sp,
                            fontWeight = if (option == value) FontWeight.Bold else FontWeight.Normal,
                            color = if (option == value) Color(0xFF4C84FF) else Color.White,
                            letterSpacing = 0.4.sp
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
