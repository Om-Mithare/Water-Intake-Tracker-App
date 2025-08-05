package Loading

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text // MaterialTheme and Surface are already imported
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp as lerpColor // Alias to avoid confusion
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterintaketracker.R
import com.example.waterintaketracker.ui.theme.WaterIntakeTrackerTheme
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

// --- Constants for easier tuning ---
private const val MAX_PARTICLES = 70
private const val PARTICLE_GENERATION_INTERVAL_MS = 90L
private const val PARTICLE_UPDATE_INTERVAL_MS = 16L
private const val BACKGROUND_ANIM_DURATION_MS = 25000 // Slightly longer for a slower pulse

@Composable
fun LoadingScreen(
    onFinished: () -> Unit
) {
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
        )
        delay(6000)
        contentAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 500)
        )
        onFinished()
    }

    WaterIntakeTrackerTheme(darkTheme = true) {
        Box( // Main container, centers children by default if they don't have specific alignment
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center // Default alignment for children
        ) {
            RisingBubblesEffect() // Background effect, fills the entire Box

            // Logo - Centered by the Box's contentAlignment
            Image(
                painter = painterResource(id = R.drawable.your_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(180.dp)
                    .alpha(contentAlpha.value) // Apply alpha directly to the logo
                // No need for .align() if it's meant to be perfectly centered by the Box
            )

            // Text Column - Aligned to the bottom center of the Box
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter) // Align this Column to the bottom center of the Box
                    .padding(bottom = 48.dp) // Add padding from the screen bottom
                    .alpha(contentAlpha.value), // Apply alpha to the text block
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "from",
                    style = TextStyle(
                        color = Color.Gray.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "THE RADIANTS",
                    style = TextStyle(
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        color = Color(0xFF30A2FF), // Bright blue for "THE RADIANTS"
                        letterSpacing = 2.sp
                    ),
                    textAlign = TextAlign.Center // Text within this Text composable is centered
                )
            }
        }
    }
}

@Composable
fun RisingBubblesEffect() {
    val particles = remember { mutableStateListOf<WaterParticle>() }
    val density = LocalDensity.current.density
    val infiniteTransition = rememberInfiniteTransition(label = "rising_bubbles_infinite_transition")

    val particleGenerationTrigger by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(PARTICLE_GENERATION_INTERVAL_MS.toInt(), easing = LinearEasing),
            RepeatMode.Restart
        ), label = "particle_generation_bubbles"
    )

    val particleUpdateTrigger by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(PARTICLE_UPDATE_INTERVAL_MS.toInt(), easing = LinearEasing),
            RepeatMode.Restart
        ), label = "particle_update_bubbles"
    )

    val backgroundAnimProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = BACKGROUND_ANIM_DURATION_MS, easing = LinearEasing), // Slower, more subtle pulsing
            RepeatMode.Reverse // Pulse in and out
        ), label = "background_color_change"
    )

    // Adjusted colors for a more noticeable blue shift in the background
    val topColorStart = Color(0xFF010306) // Darker base
    val topColorEnd = Color(0xFF03080F)   // Slightly more blue/lighter
    val midColorStart = Color(0xFF0A0F1A).copy(alpha = 0.95f)
    val midColorEnd = Color(0xFF0E1525).copy(alpha = 0.92f) // More blue
    val bottomColorStart = Color(0xFF10182B).copy(alpha = 0.9f)
    val bottomColorEnd = Color(0xFF18253D).copy(alpha = 0.87f) // Deeper blue shift

    val animatedTopColor = remember(backgroundAnimProgress) {
        lerpColor(topColorStart, topColorEnd, backgroundAnimProgress)
    }
    val animatedMidColor = remember(backgroundAnimProgress) {
        lerpColor(midColorStart, midColorEnd, backgroundAnimProgress)
    }
    val animatedBottomColor = remember(backgroundAnimProgress) {
        lerpColor(bottomColorStart, bottomColorEnd, backgroundAnimProgress)
    }

    LaunchedEffect(particleGenerationTrigger) {
        if (particles.size < MAX_PARTICLES) {
            particles.add(WaterParticle.createBubble(density, System.currentTimeMillis()))
        }
    }

    LaunchedEffect(particleUpdateTrigger) {
        val currentTime = System.currentTimeMillis()
        val iterator = particles.listIterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            particle.updateBubble(currentTime)
            if (particle.alpha <= 0f || particle.yPosition < -0.25f) {
                iterator.remove()
            }
        }
    }

    var particleDrawColor by remember { mutableStateOf(Color.Unspecified) }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(animatedTopColor, animatedMidColor, animatedBottomColor)
            )
        )

        particles.forEach { particle ->
            particleDrawColor = particle.color.copy(
                alpha = (particle.color.alpha * particle.alpha * particle.currentBrightness)
                    .coerceIn(0f, 1f)
            )
            drawCircle(
                color = particleDrawColor,
                radius = particle.radius,
                center = Offset(
                    size.width * particle.xPosition,
                    size.height * particle.yPosition
                )
            )
        }
    }
}


data class WaterParticle(
    var xPosition: Float,
    var yPosition: Float,
    var radius: Float,
    var alpha: Float,
    var color: Color,
    private val initialYVelocity: Float,
    var currentYVelocity: Float,
    private val initialXVelocity: Float,
    var currentXVelocity: Float,
    private val startTime: Long,
    private val lifeSpanDuration: Long,
    private val phaseOffset: Float = Random.nextFloat() * (2 * PI.toFloat()),
    var currentBrightness: Float = 1f,
    private val fadeInDurationMs: Float = lifeSpanDuration * 0.15f,
    private val fadeOutStartTimeMs: Float = lifeSpanDuration * 0.70f,
    private val fadeOutDurationMs: Float = lifeSpanDuration * 0.30f,
    private val maxDynamicAlpha: Float = 0.7f
) {
    companion object {
        private val oceanBlueColors = listOf(
            Color(0xFF2E86C1), Color(0xFF3498DB), Color(0xFF5DADE2),
            Color(0xFF85C1E9), Color(0xFFAED6F1), Color(0xFF4A90E2)
        )
        private const val MIN_LIFESPAN_MS = 7000L
        private const val MAX_LIFESPAN_VARIATION_MS = 6000L
        private const val BASE_RADIUS_MIN_DP = 2.0f
        private const val BASE_RADIUS_VARIATION_DP = 3.5f
        private const val Y_VELOCITY_BASE = 0.00015f
        private const val Y_VELOCITY_VARIATION = 0.0005f
        private const val X_DRIFT_BASE = 0.00025f

        fun createBubble(density: Float, currentTime: Long): WaterParticle {
            val lifeSpan = MIN_LIFESPAN_MS + Random.nextLong(MAX_LIFESPAN_VARIATION_MS)
            val bubbleBaseColor = oceanBlueColors.random()
            val finalBubbleColor = bubbleBaseColor.copy(alpha = Random.nextFloat() * 0.5f + 0.5f)

            return WaterParticle(
                xPosition = Random.nextFloat(),
                yPosition = 1.0f + Random.nextFloat() * 0.2f,
                radius = (Random.nextFloat() * BASE_RADIUS_VARIATION_DP + BASE_RADIUS_MIN_DP) * density,
                alpha = Random.nextFloat() * 0.25f + 0.45f,
                color = finalBubbleColor,
                initialYVelocity = -(Random.nextFloat() * Y_VELOCITY_VARIATION + Y_VELOCITY_BASE) * density,
                currentYVelocity = -(Random.nextFloat() * Y_VELOCITY_VARIATION + Y_VELOCITY_BASE) * density,
                initialXVelocity = (Random.nextFloat() - 0.5f) * X_DRIFT_BASE * density,
                currentXVelocity = (Random.nextFloat() - 0.5f) * X_DRIFT_BASE * density,
                startTime = currentTime,
                lifeSpanDuration = lifeSpan
            )
        }
    }

    fun updateBubble(currentTime: Long) {
        val elapsedTime = currentTime - startTime
        val progress = (elapsedTime.toFloat() / lifeSpanDuration).coerceIn(0f, 1f)

        currentYVelocity += 0.0000004f * (elapsedTime.toFloat() / 1000f).coerceAtMost(3f)
        if (currentYVelocity > -0.00004f) { currentYVelocity = -0.00004f }
        yPosition += currentYVelocity

        val swayAmplitude = 0.00030f * (1f - progress * 0.6f)
        val swayFrequencyFactor = 0.0006f + sin(phaseOffset) * 0.00015f
        currentXVelocity = initialXVelocity + sin(elapsedTime * swayFrequencyFactor + phaseOffset) * swayAmplitude
        xPosition += currentXVelocity

        if (xPosition < -0.15f) xPosition = 1.15f
        if (xPosition > 1.15f) xPosition = -0.15f

        when {
            elapsedTime < fadeInDurationMs -> {
                alpha = (elapsedTime / fadeInDurationMs) * maxDynamicAlpha
            }
            elapsedTime > fadeOutStartTimeMs -> {
                val timeIntoFadeOut = elapsedTime - fadeOutStartTimeMs
                alpha = (1f - (timeIntoFadeOut / fadeOutDurationMs)) * maxDynamicAlpha
            }
            else -> {
                alpha = maxDynamicAlpha
            }
        }
        alpha = alpha.coerceIn(0f, maxDynamicAlpha)

        currentBrightness = 0.65f + 0.35f * (sin(elapsedTime * 0.0018f + phaseOffset * 1.8f) * 0.5f + 0.5f)
        currentBrightness = currentBrightness.coerceIn(0.6f, 1f)
    }
}