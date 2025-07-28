

package Screens.Gender // Assuming this is your package structure

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsHoveredAsState // For mouse hover
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape // Still useful for clipping content if needed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape // For explicit pixel blocks
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waterintaketracker.ui.theme.WaterIntakeTrackerTheme // IMPORT YOUR THEME
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// --- Gender Enum ---
enum class Gender(val label: String, val iconLabel: String) {
    MALE("Male", "M"),
    FEMALE("Female", "F"),
    OTHER("Other", "?") // Or use an icon
}

// --- ViewModel ---
class DefaultGenderViewModel : ViewModel() {
    private val _selectedGender = MutableStateFlow<Gender?>(null)
    val selectedGender: StateFlow<Gender?> = _selectedGender.asStateFlow()

    fun selectGender(gender: Gender) {
        _selectedGender.value = if (_selectedGender.value == gender) null else gender
    }
}

// --- Composable Screens ---
@Composable
fun GenderScreen(
    viewModel: DefaultGenderViewModel = viewModel(),
    onNextClick: () -> Unit = {}
) {
    WaterIntakeTrackerTheme { // Ensure your PixelTypography and PixelShapes are applied
        val selectedGender by viewModel.selectedGender.collectAsState()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween // Pushes content and button
            ) {
                // Header Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 70.dp)
                ) {
                    Text(
                        text = "SELECT GENDER",
                        style = MaterialTheme.typography.displaySmall, // Larger, more impactful
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "[CHOOSE ONE]",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }

                // Gender Selection Circles
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp), // Add more padding around the circles
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Gender.entries.forEach { gender ->
                        PixelatedGenderCircle(
                            gender = gender,
                            isSelected = selectedGender == gender,
                            onGenderClick = { viewModel.selectGender(gender) }
                        )
                    }
                }

                // Next Button placeholder to ensure space is managed
                Box(modifier = Modifier.height(80.dp)) // Reserve space for button
            }


            // Next Button (Aligned to bottom)
            AnimatedVisibility(
                visible = selectedGender != null,
                enter = fadeIn(animationSpec = tween(300, delayMillis = 100)) +
                        slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(300, delayMillis = 100)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Button(
                    onClick = { onNextClick() },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(56.dp)
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                            MaterialTheme.shapes.medium // Use theme shape for button
                        ),
                    shape = MaterialTheme.shapes.medium, // Use theme shape for button
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        "NEXT >",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}


@Composable
fun PixelatedBlockCircle(
    modifier: Modifier = Modifier,
    color: Color,
    borderColor: Color,
    borderWidth: Dp,
    blockSize: Dp = 4.dp // Size of each "pixel" block
) {
    Canvas(modifier = modifier) {
        val canvasRadius = size.minDimension / 2
        val blockPx = blockSize.toPx()
        val borderPx = borderWidth.toPx()

        // Draw pixelated border
        val numBorderSteps = (2 * Math.PI * (canvasRadius - borderPx / 2) / blockPx).toInt()
        for (i in 0 until numBorderSteps) {
            val angle = i.toFloat() / numBorderSteps.toFloat() * 2 * Math.PI.toFloat()
            val x = center.x + (canvasRadius - borderPx / 2) * cos(angle)
            val y = center.y + (canvasRadius - borderPx / 2) * sin(angle)
            drawRect(
                color = borderColor,
                topLeft = Offset(x - blockPx / 2, y - blockPx / 2),
                size = Size(blockPx, blockPx)
            )
        }

        // Draw pixelated fill (slightly inset from the border)
        val fillRadius = canvasRadius - borderPx - blockPx / 2 // Ensure fill is within border
        if (fillRadius > 0) {
            val numFillSteps = (2 * Math.PI * fillRadius / blockPx).toInt()
            for (rStep in 0..(fillRadius / blockPx).toInt()) {
                val currentRadius = rStep * blockPx
                if (currentRadius > fillRadius) break
                val numCircumferenceSteps = (2 * Math.PI * currentRadius / blockPx).toInt().coerceAtLeast(1)
                for (i in 0 until numCircumferenceSteps) {
                    val angle = i.toFloat() / numCircumferenceSteps.toFloat() * 2 * Math.PI.toFloat()
                    val x = center.x + currentRadius * cos(angle)
                    val y = center.y + currentRadius * sin(angle)
                    drawRect(
                        color = color,
                        topLeft = Offset(x - blockPx / 2, y - blockPx / 2),
                        size = Size(blockPx, blockPx)
                    )
                }
            }
            // Fill center block if radius is very small
            if (numFillSteps == 0 && fillRadius > 0) {
                drawRect(
                    color = color,
                    topLeft = Offset(center.x - blockPx / 2, center.y - blockPx / 2),
                    size = Size(blockPx, blockPx)
                )
            }
        }
    }
}


@Composable
fun PixelatedGenderCircle(
    gender: Gender,
    isSelected: Boolean,
    onGenderClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    // val isHovered by interactionSource.collectIsHoveredAsState() // Use if you need mouse hover

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f, // Slightly more noticeable scale
        animationSpec = tween(durationMillis = 100),
        label = "scaleAnimation"
    )

    val circleSize = 100.dp
    val iconSize = 48.sp

    // --- Color Scheme ---
    val currentScheme = MaterialTheme.colorScheme
    val pressedColorShiftMultiplier = 0.8f // Darken/lighten factor on press

    val baseBackgroundColor = if (isSelected) currentScheme.primaryContainer else currentScheme.surfaceVariant
    val pressedBackgroundColor = if (isSelected) {
        Color(
            red = (baseBackgroundColor.red * pressedColorShiftMultiplier).coerceIn(0f, 1f),
            green = (baseBackgroundColor.green * pressedColorShiftMultiplier).coerceIn(0f, 1f),
            blue = (baseBackgroundColor.blue * pressedColorShiftMultiplier).coerceIn(0f, 1f),
            alpha = baseBackgroundColor.alpha
        )
    } else {
        currentScheme.primary.copy(alpha = 0.3f) // A distinct press color for non-selected
    }
    val currentBackgroundColor = if (isPressed) pressedBackgroundColor else baseBackgroundColor


    val baseBorderColor = if (isSelected) currentScheme.primary else currentScheme.outline
    val pressedBorderColor = if (isSelected) {
        currentScheme.tertiary // Example: shift to tertiary on press for selected
    } else {
        currentScheme.primary // Example: shift to primary on press for non-selected
    }
    val currentBorderColor = if (isPressed) pressedBorderColor else baseBorderColor

    val borderWidth = if (isSelected) 3.dp else 2.dp
    val pressedBorderWidth = if (isSelected) 4.dp else 3.dp // Thicker border on press
    val currentBorderWidth = if (isPressed) pressedBorderWidth else borderWidth


    val currentContentColor = when {
        isPressed && isSelected -> currentScheme.onPrimaryContainer.copy(alpha = 0.9f) // Slightly different on press selected
        isPressed && !isSelected -> currentScheme.onPrimary // Content color for pressed non-selected
        isSelected -> currentScheme.onPrimaryContainer
        else -> currentScheme.onSurfaceVariant
    }


    Box(
        modifier = Modifier
            .size(circleSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onGenderClick
            ),
        contentAlignment = Alignment.Center
    ) {
        PixelatedBlockCircle(
            modifier = Modifier.fillMaxSize(),
            color = currentBackgroundColor,
            borderColor = currentBorderColor,
            borderWidth = currentBorderWidth,
            blockSize = 4.dp // Adjust for finer or coarser pixelation of the circle itself
        )

        // Content (Text/Icon) - Placed on top of the PixelatedBlockCircle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            // Modifier.clip(CircleShape) // Optional: Clip content to a smooth circle if text overflows the blocky one
        ) {
            Text(
                text = gender.iconLabel,
                style = MaterialTheme.typography.displayMedium.copy(fontSize = iconSize),
                color = currentContentColor,
                fontWeight = FontWeight.Bold
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = gender.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = currentContentColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


// --- Previews ---
@Preview(showBackground = true, name = "Gender Screen Light - Female Selected", widthDp = 360, heightDp = 740)
@Composable
fun GenderScreenLightFemalePreview() {
    WaterIntakeTrackerTheme(darkTheme = false) {
        GenderScreen(
            viewModel = remember { DefaultGenderViewModel().apply { selectGender(Gender.FEMALE) } },
            onNextClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Gender Screen Dark - No Selection", widthDp = 360, heightDp = 740)
@Composable
fun GenderScreenDarkPreview() {
    WaterIntakeTrackerTheme(darkTheme = true) {
        GenderScreen(
            viewModel = remember { DefaultGenderViewModel() },
            onNextClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Gender Circle - Male Selected", widthDp = 150, heightDp = 200)
@Composable
fun GenderCircleSelectedPreview() {
    WaterIntakeTrackerTheme(darkTheme = false) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)) {
            PixelatedGenderCircle(gender = Gender.MALE, isSelected = true, onGenderClick = {})
        }
    }
}

@Preview(showBackground = true, name = "Gender Circle - Female Default", widthDp = 150, heightDp = 200)
@Composable
fun GenderCircleDefaultPreview() {
    WaterIntakeTrackerTheme(darkTheme = true) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)) {
            PixelatedGenderCircle(gender = Gender.FEMALE, isSelected = false, onGenderClick = {})
        }
    }
}

@Preview(showBackground = true, name = "Gender Circle - Female Pressed", widthDp = 150, heightDp = 200)
@Composable
fun GenderCircleFemalePressedPreview() {
    WaterIntakeTrackerTheme(darkTheme = false) {
        val interactionSource = remember { MutableInteractionSource() }
        // Simulate pressed state for preview
        LaunchedEffect(Unit) {
            interactionSource.tryEmit(PressInteraction.Press(Offset.Zero))
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)) {
            PixelatedGenderCircle(
                gender = Gender.FEMALE,
                isSelected = false, // Test pressed state for non-selected
                onGenderClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Gender Circle - Male Selected Pressed", widthDp = 150, heightDp = 200)
@Composable
fun GenderCircleMaleSelectedPressedPreview() {
    WaterIntakeTrackerTheme(darkTheme = false) {
        val interactionSource = remember { MutableInteractionSource() }
        // Simulate pressed state for preview
        LaunchedEffect(Unit) {
            interactionSource.tryEmit(PressInteraction.Press(Offset.Zero))
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)) {
            PixelatedGenderCircle(
                gender = Gender.MALE,
                isSelected = true, // Test pressed state for selected
                onGenderClick = {}
            )
        }
    }
}