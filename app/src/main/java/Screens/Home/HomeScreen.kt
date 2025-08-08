package Screens.Home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.waterintaketracker.Models.PredefinedWaterSize
import com.example.waterintaketracker.Models.WaterLogEntry
import com.example.waterintaketracker.R
import com.example.waterintaketracker.ViewModels.HomeViewModel

val defaultWaterSizes = listOf(
    PredefinedWaterSize("Cup", 250, R.drawable.ic_water_cup),
    PredefinedWaterSize("Bottle", 500, R.drawable.ic_water_bottle),
    PredefinedWaterSize("Flask", 750, R.drawable.ic_water_flask)
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val todaysLogEntries = homeViewModel.todaysLog
    val totalIntakeToday by homeViewModel.totalIntakeToday.collectAsState()
    val dailyGoal by homeViewModel.dailyGoalMl.collectAsState()
    val currentStreak by homeViewModel.currentStreak.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    val isFabVisible = !showAddDialog

    // Navigate to celebration if goal is newly achieved
    LaunchedEffect(Unit) {
        homeViewModel.triggerCelebrate.collect {
            navController.navigate("celebration")
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (isFabVisible) {
                ExtendedFloatingActionButton(
                    onClick = { showAddDialog = true },
                    icon = {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Add water intake",
                            tint = colorScheme.onPrimaryContainer
                        )
                    },
                    text = {
                        Text(
                            "ADD WATER",
                            fontWeight = FontWeight.Medium,
                            color = colorScheme.onPrimaryContainer
                        )
                    },
                    containerColor = colorScheme.primaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorScheme.background)
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                StreakDisplay(
                    currentStreak = currentStreak,
                    iconResId = R.drawable.ic_blue_flame,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                TotalIntakePanel(
                    currentIntake = totalIntakeToday,
                    dailyGoal = dailyGoal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp, bottom = 16.dp)
                )

                Text(
                    "TODAY'S LOG",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                val bottomPaddingForList = if (isFabVisible) 88.dp else 16.dp

                if (todaysLogEntries.isEmpty()) {
                    EmptyLogView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(bottom = bottomPaddingForList)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = bottomPaddingForList)
                    ) {
                        items(todaysLogEntries, key = { it.id }) { entry ->
                            LogEntryCard(
                                entry = entry,
                                onRemoveClick = {
                                    homeViewModel.removeLogEntry(entry)
                                }
                            )
                        }
                    }
                }
            }

            if (showAddDialog) {
                AddWaterDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { amountToAdd ->
                        homeViewModel.addWater(amountToAdd)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun StreakDisplay(currentStreak: Int, iconResId: Int, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1E1E), RoundedCornerShape(16.dp))
            .border(BorderStroke(2.dp, colorScheme.primaryContainer.copy(alpha = 0.8f)), RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = "Streak Icon",
                tint = Color(0xFF00BCD4),
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Streak: $currentStreak days",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                color = Color(0xFFE0F7FA),
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
fun EmptyLogView(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_empty_log),
                contentDescription = "Empty log",
                modifier = Modifier.size(64.dp),
                tint = colorScheme.primary.copy(alpha = 0.7f)
            )
            Text(
                "No water logged yet for today.\nTap 'ADD WATER' to get started!",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun TotalIntakePanel(
    currentIntake: Int,
    dailyGoal: Int,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier.shadow(elevation = 4.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.primaryContainer.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 20.dp)
        ) {
            Text(
                "Daily Goal Progress",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Normal),
                color = colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            val progress = if (dailyGoal > 0) (currentIntake.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f) else 0f
            val circularProgressSize = 140.dp
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(circularProgressSize),
                    color = colorScheme.primary,
                    strokeWidth = 8.dp,
                    trackColor = colorScheme.primary.copy(alpha = 0.2f)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.widthIn(max = circularProgressSize - 20.dp)
                ) {
                    Text(
                        text = "$currentIntake ml",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        "of $dailyGoal ml",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                        color = colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            val (progressText, progressTextColor) = when {
                dailyGoal <= 0 -> "Set your daily goal!" to colorScheme.errorContainer
                currentIntake >= dailyGoal -> "Goal Achieved! Keep it up!" to colorScheme.tertiary
                currentIntake == 0 -> "Let's start hydrating!" to colorScheme.onPrimaryContainer
                else -> "You're doing great!" to colorScheme.primary
            }
            Text(
                text = progressText,
                style = MaterialTheme.typography.labelLarge,
                color = progressTextColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun LogEntryCard(
    entry: WaterLogEntry,
    onRemoveClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_water_drop_log),
                contentDescription = "Water log entry",
                tint = colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "${entry.amountMl} ml",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    entry.timeFormatted,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Remove entry",
                    tint = colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWaterDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var customAmountInput by rememberSaveable { mutableStateOf("") }
    var selectedPredefinedAmount by rememberSaveable { mutableStateOf<Int?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val colorScheme = MaterialTheme.colorScheme
    val updateAmountAndSelection: (String, Int?) -> Unit = { text, predefined ->
        customAmountInput = text
        selectedPredefinedAmount = predefined
    }
    val isAddEnabled = (customAmountInput.toIntOrNull() ?: 0) > 0
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = colorScheme.surface,
        title = {
            Text(
                "Add Hydration",
                style = MaterialTheme.typography.headlineSmall,
                color = colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Quick Add:",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    defaultWaterSizes.forEach { predefinedSize ->
                        val isSelected = selectedPredefinedAmount == predefinedSize.amountMl &&
                                customAmountInput == predefinedSize.amountMl.toString()
                        OutlinedButton(
                            onClick = { updateAmountAndSelection(predefinedSize.amountMl.toString(), predefinedSize.amountMl) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).heightIn(min = 72.dp),
                            border = BorderStroke(
                                1.5.dp,
                                if (isSelected) colorScheme.primary else colorScheme.outline.copy(alpha = 0.7f)
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                                contentColor = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant
                            ),
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = predefinedSize.iconResId),
                                    contentDescription = predefinedSize.label,
                                    modifier = Modifier.size(28.dp),
                                    tint = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                                Text(predefinedSize.label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                                Text("(${predefinedSize.amountMl}ml)", style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 12.dp), color = colorScheme.outline.copy(alpha = 0.5f))
                Text(
                    "Or Custom Amount (ml):",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = customAmountInput,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }.take(4)
                        updateAmountAndSelection(filtered, null)
                    },
                    placeholder = {
                        Text(
                            "e.g., 300",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.bodyLarge.copy(color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (isAddEnabled) {
                            customAmountInput.toIntOrNull()?.let { onConfirm(it) }
                        }
                        keyboardController?.hide()
                    }),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.outline.copy(alpha = 0.7f),
                        cursorColor = colorScheme.primary,
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth(0.65f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    customAmountInput.toIntOrNull()?.let { if (it > 0) onConfirm(it) }
                    keyboardController?.hide()
                },
                enabled = isAddEnabled,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
            ) { Text("ADD", color = colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
            ) { Text("CANCEL", color = colorScheme.primary, fontWeight = FontWeight.Medium) }
        }
    )
}