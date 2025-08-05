package Screens.Gender

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.waterintaketracker.ui.theme.WaterIntakeTrackerTheme
import com.example.waterintaketracker.ui.theme.PixelShapes
import com.example.waterintaketracker.ui.theme.PixelTypography
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.cos
import kotlin.math.sin
import androidx.hilt.navigation.compose.hiltViewModel
import Screens.Profile.ProfileViewModel
import android.R.attr.scaleX
import android.R.attr.scaleY
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.graphicsLayer

// --- Gender Enum ---
enum class Gender(val label: String, val iconLabel: String) {
    MALE("Male", "M"),
    FEMALE("Female", "F"),
    OTHER("Other", "?")
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
    profileViewModel: ProfileViewModel = hiltViewModel(),
    navController: NavController
) {
    WaterIntakeTrackerTheme {
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
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 70.dp)
                ) {
                    Text(
                        text = "SELECT GENDER",
                        style = MaterialTheme.typography.displaySmall,
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
                        .padding(vertical = 40.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Gender.values().forEach { gender ->
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
                    onClick = {
                        selectedGender?.let { gender ->
                            profileViewModel.updateProfileField("gender", gender.label)
                            navController.navigate("age")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
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
fun PixelatedGenderCircle(
    gender: Gender,
    isSelected: Boolean,
    onGenderClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 100)
    )

    val circleSize = 100.dp
    val iconSize = 48.sp

    val colors = MaterialTheme.colorScheme

    val baseBackgroundColor = if (isSelected) colors.primaryContainer else colors.surfaceVariant
    val pressedBackgroundColor = if (isSelected) colors.primaryContainer.copy(alpha = 0.8f) else colors.primary.copy(alpha = 0.3f)
    val currentBackgroundColor = if (isPressed) pressedBackgroundColor else baseBackgroundColor

    val baseBorderColor = if (isSelected) colors.primary else colors.outline
    val pressedBorderColor = if (isSelected) colors.tertiary else colors.primary
    val currentBorderColor = if (isPressed) pressedBorderColor else baseBorderColor

    val borderWidth = if (isSelected) 3.dp else 2.dp
    val pressedBorderWidth = if (isSelected) 4.dp else 3.dp
    val currentBorderWidth = if (isPressed) pressedBorderWidth else borderWidth

    val currentContentColor = when {
        isPressed && isSelected -> colors.onPrimaryContainer.copy(alpha = 0.9f)
        isPressed && !isSelected -> colors.onPrimary
        isSelected -> colors.onPrimaryContainer
        else -> colors.onSurfaceVariant
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
            blockSize = 4.dp
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
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

@Composable
fun PixelatedBlockCircle(
    modifier: Modifier = Modifier,
    color: Color,
    borderColor: Color,
    borderWidth: Dp,
    blockSize: Dp = 4.dp
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
        val fillRadius = canvasRadius - borderPx - blockPx / 2
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
