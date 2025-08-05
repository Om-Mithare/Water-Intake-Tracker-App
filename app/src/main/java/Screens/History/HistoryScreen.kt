package com.example.yourappname.ui.history // Adjust package name as needed

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.max
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen() {
    val viewModel: HistoryViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Hydration History",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = colors.onSurface
                )
            )
        },
        containerColor = colors.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 56.dp) // Added padding for bottom navigation
        ) {
            when (val currentState = uiState) {
                is HistoryViewModel.UiState.Loading -> LoadingView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp)
                )

                is HistoryViewModel.UiState.Empty -> EmptyView(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp)
                )

                is HistoryViewModel.UiState.Error -> ErrorView(
                    message = currentState.message,
                    onRetry = { viewModel.reloadData() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 32.dp)
                )

                is HistoryViewModel.UiState.Success -> SuccessContent(
                    state = currentState,
                    colors = colors
                )
            }

            DailyTipsSection()
        }
    }
}

@Composable
private fun SuccessContent(
    state: HistoryViewModel.UiState.Success,
    colors: ColorScheme
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerHighest),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    "This Week's Progress",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.onSurface,
                    modifier = Modifier.padding(bottom = 10.dp)
                        .align(Alignment.CenterHorizontally)
                )
                WeeklyChart(
                    weeklyData = state.weeklyData,
                    goal = state.goal,
                    barColor = colors.primary,
                    goalLineColor = colors.tertiary,
                    textColor = colors.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "Your Hydration Stats",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = colors.onSurface,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            StatCard(
                title = "Weekly Avg",
                value = "${state.avgDailyIntake}ml",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                colors = colors,
                modifier = Modifier.fillMaxWidth()
            )
            StatCard(
                title = "Monthly Avg",
                value = "${state.monthlyAverage}ml",
                icon = Icons.Default.CalendarMonth,
                colors = colors,
                modifier = Modifier.fillMaxWidth()
            )
            StatCard(
                title = "Goal Met",
                value = "${(state.completionRate * 100).toInt()}%",
                icon = Icons.Default.DoneAll,
                colors = colors,
                modifier = Modifier.fillMaxWidth()
            )
            StatCard(
                title = "Frequency",
                value = "%.1f/day".format(state.drinkFrequency),
                icon = Icons.Default.Schedule,
                colors = colors,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DailyTipsSection() {
    val tips = listOf(
        "Drink a glass of water before every meal.",
        "Keep a water bottle with you at all times.",
        "Set reminders to drink water throughout the day.",
        "Try adding slices of lemon or cucumber for flavor.",
        "Drink water after every bathroom break."
    )
    val randomTip = remember { tips[Random.nextInt(tips.size)] }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            PixelatedBackground(
                color = Color(0xFFFFF9C4).copy(alpha = 0.2f), // subtle yellow highlight
                blockSize = 8.dp,
                modifier = Modifier.matchParentSize()
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💡 Daily Tip",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant // previous color scheme
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = randomTip,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // previous color scheme
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PixelatedBackground(
    color: Color,
    blockSize: Dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val blockPx = blockSize.toPx()
        val cols = (size.width / blockPx).toInt() + 1
        val rows = (size.height / blockPx).toInt() + 1

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                // Draw blocks with some spacing to create pixelated effect
                val left = col * blockPx
                val top = row * blockPx
                drawRect(
                    color = color,
                    topLeft = Offset(left, top),
                    size = Size(blockPx * 0.8f, blockPx * 0.8f)
                )
            }
        }
    }
}

@Composable
private fun WeeklyChart(
    weeklyData: List<DailyData>,
    goal: Int,
    barColor: Color,
    goalLineColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    if (weeklyData.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No weekly data to display.",
                color = textColor.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    val density = LocalDensity.current
    val goalTextPaintSize = density.run { 13.sp.toPx() }
    val goalTextTopPadding = density.run { 4.dp.toPx() }
    val goalTextBottomPadding = density.run { 6.dp.toPx() }
    val goalTextSectionHeight = goalTextPaintSize + goalTextTopPadding + goalTextBottomPadding

    val dataMaxAmount = weeklyData.maxOfOrNull { it.amount }?.toFloat() ?: 0f
    val goalBuffer = if (dataMaxAmount >= goal.toFloat() * 0.95f) goal.toFloat() * 0.20f else 0f
    val maxValue = max(dataMaxAmount, goal.toFloat() + goalBuffer).coerceAtLeast(1f)

    val nativeTextPaint = remember(textColor, density) {
        android.graphics.Paint().apply {
            isAntiAlias = true; textAlign = android.graphics.Paint.Align.CENTER
            color = textColor.toArgb(); textSize = density.run { 12.sp.toPx() }
        }
    }
    val nativeGoalTextPaint = remember(goalLineColor, density, goalTextPaintSize) {
        android.graphics.Paint().apply {
            isAntiAlias = true; textAlign = android.graphics.Paint.Align.RIGHT
            color = goalLineColor.toArgb(); textSize = goalTextPaintSize
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
    }
    val composeGoalLinePaint = remember(goalLineColor, density) {
        androidx.compose.ui.graphics.Paint().apply {
            style = PaintingStyle.Stroke; strokeWidth = density.run { 2.5.dp.toPx() }
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f); color = goalLineColor
        }
    }

    Canvas(modifier = modifier) {
        val barCount = weeklyData.size
        val barAreaWidth = if (barCount > 0) size.width / barCount else size.width
        val chartTopPaddingForGoalDisplay = goalTextSectionHeight
        val chartBottomPaddingForLabels = density.run { 28.dp.toPx() }
        val availableHeightForBars = (size.height - chartTopPaddingForGoalDisplay - chartBottomPaddingForLabels).coerceAtLeast(0f)
        val chartDrawingAreaTopY = chartTopPaddingForGoalDisplay
        val chartDrawingAreaBottomY = chartTopPaddingForGoalDisplay + availableHeightForBars
        val goalYRatio = (goal.toFloat() / maxValue).coerceIn(0f, 1f)
        val goalYAbsolute = chartDrawingAreaTopY + (availableHeightForBars * (1 - goalYRatio)).coerceIn(0f, availableHeightForBars)

        drawIntoCanvas { canvas -> canvas.nativeCanvas.drawLine(0f, goalYAbsolute, size.width, goalYAbsolute, composeGoalLinePaint.asFrameworkPaint()) }

        if (barCount == 0) return@Canvas

        weeklyData.forEachIndexed { index, day ->
            val barVisualWidthRatio = 0.6f
            val barActualWidth = barAreaWidth * barVisualWidthRatio
            val barLeftOffset = barAreaWidth * (1 - barVisualWidthRatio) / 2
            val left = index * barAreaWidth + barLeftOffset
            val barHeightRatio = (day.amount.toFloat() / maxValue).coerceIn(0f, 1f)
            val barActualHeight = availableHeightForBars * barHeightRatio
            val barTopY = (chartDrawingAreaBottomY - barActualHeight).coerceAtLeast(chartDrawingAreaTopY)
            val correctedBarActualHeight = chartDrawingAreaBottomY - barTopY

            if (correctedBarActualHeight > 0) {
                drawRect(
                    color = barColor.copy(alpha = if (day.amount >= goal) 1f else 0.7f),
                    topLeft = Offset(left, barTopY),
                    size = Size(barActualWidth, correctedBarActualHeight)
                )
            }
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(
                    "${day.amount}ml", left + barActualWidth / 2,
                    barTopY - 4.dp.toPx(), nativeTextPaint // Display amount above the bar
                )
                canvas.nativeCanvas.drawText(
                    day.dayName.take(3), left + barActualWidth / 2,
                    chartDrawingAreaBottomY + density.run { 18.dp.toPx() }, nativeTextPaint
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    colors: ColorScheme,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(min = 60.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = colors.primary,
                modifier = Modifier
                    .size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    title, style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurfaceVariant, fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    value, style = MaterialTheme.typography.headlineSmall,
                    color = colors.onSurface, fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun LoadingView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 5.dp
            )
            Text(
                "Loading your hydration history...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Icon(
                Icons.Filled.WaterDrop, contentDescription = "No data available",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Text(
                "No Hydration Data Yet", style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center
            )
            Text(
                "Start logging your water intake to see your progress and stats here.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Icon(
                Icons.Filled.CloudOff, contentDescription = "Error loading data",
                tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(80.dp)
            )
            Text(
                "Oops! Something Went Wrong", style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center
            )
            Text(
                message, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "Retry", modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Try Again")
            }
        }
    }
}
