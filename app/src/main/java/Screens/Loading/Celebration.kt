package Screens.Celebration

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.waterintaketracker.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private const val CELEBRATION_DURATION_MS = 5000L
private const val PIXEL_WAVE_SPEED_DURATION_MS = 3000
private const val PIXEL_WAVE_BASE_AMPLITUDE_DP = 55
private const val PIXEL_SEGMENT_WIDTH_DP = 12
private const val SPARKLE_COUNT = 25
private const val RISING_BUBBLE_COUNT = 20
private const val RISING_BUBBLE_MAX_Y_FACTOR = 0.85f
private const val RISING_BUBBLE_FADE_START_Y_FACTOR = 0.3f
private const val BUBBLE_BASE_SPEED_MULTIPLIER = 2.5f

private val nightSkyColor = Color(0xFF070B1F)
private val waveLayerColors = listOf(Color(0xFF307FE2), Color(0xFF206AB0), Color(0xFF154E8C))
private val foamColorLight = Color(0xBFFFFFFF)
private val foamColorDark = Color(0x99DDEEFF)
private val sparkleColor1 = Color(0xFFF0F8FF)
private val sparkleColor2 = Color(0xFFB0E0E6)
private val risingBubbleColors = listOf(Color(0x66ADD8E6), Color(0x6687CEEB), Color(0x666495ED))

val pixelFontFamily = FontFamily(Font(R.font.press_start_2p_regular, FontWeight.Normal))

data class RisingBubbleState(
    val id: Int = Random.nextInt(),
    var x: Float,
    var y: Float,
    val size: Float,
    val speedY: Float,
    val swayXFactor: Float,
    val swaySpeed: Float,
    var currentAlpha: Float = 1f,
    val color: Color
)

@Composable
fun CelebrationScreen(modifier: Modifier = Modifier, onFinished: () -> Unit) {
    val context = LocalContext.current
    val mediaPlayer = remember {
        MediaPlayer.create(context, R.raw.cele_audio).apply {
            isLooping = false
            start()
        }
    }

    LaunchedEffect(Unit) {
        delay(CELEBRATION_DURATION_MS)
        if (isActive) {
            mediaPlayer.stop()
            mediaPlayer.release()
            onFinished()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pixel_wave")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (4f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(PIXEL_WAVE_SPEED_DURATION_MS * 2, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pixel_wave_phase"
    )

    val dynamicAmplitudeFactor by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = SineInOutEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pixel_wave_dynamic_amplitude"
    )

    val density = LocalDensity.current
    val baseWaveAmplitudePx = with(density) { PIXEL_WAVE_BASE_AMPLITUDE_DP.dp.toPx() }
    val segmentWidthPx = with(density) { PIXEL_SEGMENT_WIDTH_DP.dp.toPx() }
    val foamStrokeWidthPx = with(density) { 2.5.dp.toPx() }
    val minFoamSeparationPx = with(density) { 2.dp.toPx() }
    val textApproxHeightPx = with(density) { 100.dp.toPx() }
    val textPaddingPx = with(density) { 20.dp.toPx() }

    val risingBubbles = remember { mutableStateListOf<RisingBubbleState>() }
    var screenHeightPx by remember { mutableStateOf(0f) }
    var screenWidthPx by remember { mutableStateOf(0f) }

    LaunchedEffect(screenWidthPx, screenHeightPx) {
        if (screenWidthPx == 0f || screenHeightPx == 0f) return@LaunchedEffect
        while (isActive) {
            if (risingBubbles.size < RISING_BUBBLE_COUNT) {
                val bubbleSizePx = with(density) { (Random.nextFloat() * 5.dp + 3.dp).toPx() }
                val bubbleSpeedYPx = with(density) { (Random.nextFloat() * 5.5f + 0.5f).dp.toPx() * 0.1f * BUBBLE_BASE_SPEED_MULTIPLIER }
                val bubbleSwayXFactorPx = with(density) { (Random.nextFloat() * 20f - 10f).dp.toPx() }
                val startYOffsetPx = with(density) { Random.nextFloat() * 50.dp.toPx() }
                val bubbleColor = risingBubbleColors.random()
                risingBubbles.add(
                    RisingBubbleState(
                        x = Random.nextFloat() * screenWidthPx,
                        y = screenHeightPx + startYOffsetPx,
                        size = bubbleSizePx,
                        speedY = bubbleSpeedYPx,
                        swayXFactor = bubbleSwayXFactorPx,
                        swaySpeed = Random.nextFloat() * 0.03f + 0.01f,
                        color = bubbleColor
                    )
                )
            }
            val toRemove = mutableListOf<RisingBubbleState>()
            val currentPhase = phase
            risingBubbles.forEach { bubble ->
                bubble.y -= bubble.speedY * (1f + sin(currentPhase * 0.5f + bubble.id) * 0.3f)
                bubble.x += sin(bubble.y * bubble.swaySpeed + bubble.id) * bubble.swayXFactor * 0.05f
                if (bubble.y < screenHeightPx * (1f - RISING_BUBBLE_MAX_Y_FACTOR) || bubble.currentAlpha <= 0.01f) {
                    toRemove.add(bubble)
                }
            }
            risingBubbles.removeAll(toRemove.toSet())
            delay(30L)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(nightSkyColor),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (size.width != screenWidthPx || size.height != screenHeightPx) {
                screenWidthPx = size.width
                screenHeightPx = size.height
            }
            val canvasWidth = size.width
            val canvasHeight = size.height
            val baseOffsetY = canvasHeight / 1.7f

            waveLayerColors.forEachIndexed { index, layerColor ->
                val currentBaseAmplitude = baseWaveAmplitudePx * dynamicAmplitudeFactor
                val layerAmplitude = currentBaseAmplitude * (1f - index * 0.25f)
                val layerPhase = phase * (1f + index * 0.1f) + index * 0.9f
                val layerOffsetY = baseOffsetY + index * currentBaseAmplitude * 0.35f
                val wavePath = createComplexPixelWavePath(
                    canvasWidth, layerOffsetY, layerPhase, layerAmplitude,
                    segmentWidthPx, canvasHeight, index + 1
                )
                drawPath(
                    path = wavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            layerColor.copy(alpha = 0.65f - index * 0.1f),
                            layerColor.copy(alpha = 0.85f - index * 0.1f),
                            nightSkyColor.copy(alpha = 0.7f)
                        ),
                        startY = layerOffsetY - layerAmplitude,
                        endY = layerOffsetY + layerAmplitude * 2.5f
                    )
                )
                if (index == 0) {
                    drawDynamicPixelFoam(
                        canvasWidth, layerOffsetY, layerPhase, layerAmplitude,
                        segmentWidthPx * 0.9f, 0.35f, foamStrokeWidthPx,
                        index + 1, minFoamSeparationPx
                    )
                }
            }

            risingBubbles.forEach { bubble ->
                drawCircle(
                    color = bubble.color.copy(alpha = bubble.currentAlpha),
                    radius = bubble.size,
                    center = Offset(bubble.x.coerceIn(0f, canvasWidth), bubble.y),
                    blendMode = BlendMode.Plus
                )
            }

            drawSparklesEnhanced(
                canvasWidth, canvasHeight, SPARKLE_COUNT, phase, baseOffsetY,
                baseWaveAmplitudePx, textApproxHeightPx, textPaddingPx
            )
        }

        Text(
            text = "GOAL\nACHIEVED",
            style = TextStyle(
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = pixelFontFamily,
                shadow = Shadow(Color.Black.copy(alpha = 0.7f), offset = Offset(2f, 2f), blurRadius = 0f),
                textAlign = TextAlign.Center,
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 48.dp)
        )
    }
}

private val SineInOutEasing = Easing { fraction ->
    (1f - kotlin.math.cos(PI * fraction).toFloat()) / 2f
}
// createComplexPixelWavePath and helper draw methods are unchanged and assumed present.
// Helper functions (createComplexPixelWavePath, drawDynamicPixelFoam, drawSparklesEnhanced) are not changed as they are correct.
fun createComplexPixelWavePath(
    canvasWidth: Float, yOffset: Float, phase: Float, amplitude: Float,
    segmentWidthPx: Float, canvasHeight: Float, complexityFactor: Int
): Path {
    val path = Path()
    val frequency1 = (2f * PI / (canvasWidth * 1.6f)).toFloat()
    val frequency2 = (2f * PI / (canvasWidth * 0.7f)).toFloat()
    val amplitude2 = amplitude * 0.3f * (1f / complexityFactor.toFloat().coerceAtLeast(1f))
    val initialX = -segmentWidthPx
    var lastCalculatedY = yOffset +
            (amplitude * sin(initialX * frequency1 + phase)) +
            (amplitude2 * sin(initialX * frequency2 + phase * 1.3f + complexityFactor * 0.5f))
    path.moveTo(initialX, lastCalculatedY)
    val numSegments = (canvasWidth / segmentWidthPx).toInt() + 3
    for (i in 0..numSegments) {
        val currentX = i * segmentWidthPx
        val angle1 = currentX * frequency1 + phase
        val y1 = amplitude * sin(angle1)
        val angle2 = currentX * frequency2 + phase * 1.3f + complexityFactor * 0.5f
        val y2 = amplitude2 * sin(angle2)
        val currentY = yOffset + y1 + y2
        path.lineTo(currentX, currentY)
    }
    path.lineTo(canvasWidth + segmentWidthPx, canvasHeight + 100f)
    path.lineTo(-segmentWidthPx, canvasHeight + 100f)
    path.close()
    return path
}

fun DrawScope.drawDynamicPixelFoam(
    canvasWidth: Float, yOffset: Float, phase: Float, amplitude: Float,
    segmentWidthPx: Float, foamStrengthFactor: Float,
    strokeWidthPx: Float,
    waveComplexityFactor: Int,
    minFoamSeparationPx: Float
) {
    val foamPath = Path()
    val frequency1 = (2f * PI / (canvasWidth * 1.6f)).toFloat()
    val frequency2 = (2f * PI / (canvasWidth * 0.7f)).toFloat()
    val amplitude2Factor = 0.3f * (1f / waveComplexityFactor.toFloat().coerceAtLeast(1f))
    val firstX = -segmentWidthPx
    val firstAngle1 = firstX * frequency1 + phase
    val firstAngle2 = firstX * frequency2 + phase * 1.3f + waveComplexityFactor * 0.5f
    val firstWaveY = yOffset + (amplitude * sin(firstAngle1)) + (amplitude * amplitude2Factor * sin(firstAngle2))
    val foamHeightNoise = (sin(phase * 2f + firstX * 0.05f) * 0.5f + 0.5f) * 0.6f + 0.7f
    val firstFoamY = firstWaveY - amplitude * foamStrengthFactor * foamHeightNoise
    foamPath.moveTo(firstX, firstFoamY)
    val numSegments = (canvasWidth / segmentWidthPx).toInt() + 2
    for (i in 0..numSegments) {
        val currentX = i * segmentWidthPx
        val angle1 = currentX * frequency1 + phase
        val y1 = amplitude * sin(angle1)
        val angle2 = currentX * frequency2 + phase * 1.3f + waveComplexityFactor * 0.5f
        val y2 = amplitude * amplitude2Factor * sin(angle2)
        val waveY = yOffset + y1 + y2
        val currentFoamNoise = sin(currentX * 0.15f + phase * 3.5f + i * 0.2f) * 0.5f + 0.5f
        val dynamicFoamHeightFactor = (0.4f + currentFoamNoise * 0.8f)
        val currentFoamY = waveY - amplitude * foamStrengthFactor * dynamicFoamHeightFactor
        foamPath.lineTo(currentX, currentFoamY.coerceAtMost(waveY - minFoamSeparationPx))
    }
    drawPath(
        path = foamPath,
        brush = Brush.horizontalGradient(
            colors = listOf(foamColorLight, foamColorDark, foamColorLight.copy(alpha = 0.5f)),
            startX = 0f, endX = canvasWidth
        ),
        style = Stroke(
            width = strokeWidthPx,
            pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(8f, 10f),
                phase = phase * 12 + waveComplexityFactor * 5
            )
        )
    )
}

fun DrawScope.drawSparklesEnhanced(
    width: Float, height: Float, count: Int, phase: Float,
    waveBaseY: Float,
    baseWaveAmplitudePx: Float,
    textApproxHeightPx: Float,
    textPaddingPx: Float
) {
    val textCenterY = height / 2f
    val sparkleCeiling = textCenterY - (textApproxHeightPx / 2f) - textPaddingPx
    val sparkleFloor = textCenterY + (textApproxHeightPx / 2f) + textPaddingPx
    val textRegionLeft = width * 0.2f
    val textRegionRight = width * 0.8f
    val maxSparkleY = waveBaseY - baseWaveAmplitudePx * 1.8f
    repeat(count) { index ->
        val randomSeed = index + (phase * 2f).toLong()
        val random = Random(randomSeed)
        val x = random.nextFloat() * width
        var y = random.nextFloat() * maxSparkleY
        val isInHorizontalTextRegion = x > textRegionLeft && x < textRegionRight
        val isInVerticalTextRegion = y > sparkleCeiling && y < sparkleFloor
        if (isInHorizontalTextRegion && isInVerticalTextRegion) {
            y = if (random.nextBoolean()) {
                random.nextFloat() * sparkleCeiling
            } else {
                sparkleFloor + random.nextFloat() * (maxSparkleY - sparkleFloor).coerceAtLeast(0f)
            }
            y = y.coerceIn(0f, maxSparkleY)
        }
        val baseSizeFactor = random.nextFloat() * 1.8f + 0.8f
        val twinkleFactor = (sin(phase * (random.nextFloat() * 1.5f + 0.5f) + index * 0.5f) * 0.5f + 0.5f)
        val currentSizePx = baseSizeFactor * (0.5f + twinkleFactor * 0.8f)
        val color = if (random.nextBoolean()) sparkleColor1 else sparkleColor2
        val alpha = (0.2f + twinkleFactor * 0.8f).coerceIn(0.1f, 1f)
        if (currentSizePx > 0.3f && y > 0 && y < maxSparkleY) {
            drawCircle(
                color = color.copy(alpha = alpha),
                radius = currentSizePx,
                center = Offset(x, y),
                blendMode = BlendMode.Screen
            )
        }
    }
}
