package Screens.Profile

import android.app.TimePickerDialog
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.waterintaketracker.ui.theme.PixelShapes
import com.example.waterintaketracker.ui.theme.PixelTypography
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val colors = MaterialTheme.colorScheme

    // Dialog state holders
    var showGenderDialog by remember { mutableStateOf(false) }
    var showAgeDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var showWakeupDialog by remember { mutableStateOf(false) }
    var showSleepDialog by remember { mutableStateOf(false) }
    var showExerciseDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "My Profile",
                        fontWeight = FontWeight.SemiBold,
                        style = PixelTypography.headlineMedium,
                        color = colors.onSurface
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
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp), color = colors.primary)
                Text("Loading profile...", style = PixelTypography.bodyMedium, color = colors.onSurfaceVariant)
            } else if (errorMessage != null) {
                Text(errorMessage ?: "An error occurred", color = colors.error, style = PixelTypography.bodyMedium)
                Button(onClick = { /* Reload logic if needed */ }) {
                    Text("Retry")
                }
            } else if (userProfile != null) {
                val user = userProfile!!

                // Profile Image and Name
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    if (user.profileImage.isNotEmpty()) {
                        val decodedString = Base64.decode(user.profileImage, Base64.DEFAULT)
                        val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                        Image(
                            bitmap = decodedByte.asImageBitmap(),
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceVariant)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(colors.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                user.username.firstOrNull()?.uppercase() ?: "U",
                                style = PixelTypography.displayMedium.copy(fontSize = 48.sp),
                                color = colors.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        user.username,
                        style = PixelTypography.headlineLarge,
                        color = colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        user.emailid,
                        style = PixelTypography.bodyMedium,
                        color = colors.onSurfaceVariant
                    )
                }

                // Profile Details Section
                Card(
                    shape = PixelShapes.medium,
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileDetailRow(
                            label = "Gender",
                            value = user.gender.ifEmpty { "Not set" },
                            onClick = { showGenderDialog = true },
                            colors = colors
                        )
                        Divider(color = colors.outline.copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ProfileDetailRow(
                            label = "Age",
                            value = if (user.age > 0) "${user.age} years" else "Not set",
                            onClick = { showAgeDialog = true },
                            colors = colors
                        )
                        Divider(color = colors.outline.copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ProfileDetailRow(
                            label = "Weight",
                            value = if (user.weight > 0) "${user.weight} kg" else "Not set",
                            onClick = { showWeightDialog = true },
                            colors = colors
                        )
                        Divider(color = colors.outline.copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ProfileDetailRow(
                            label = "Wakeup Time",
                            value = user.wakeupTime.ifEmpty { "Not set" },
                            onClick = { showWakeupDialog = true },
                            colors = colors
                        )
                        Divider(color = colors.outline.copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ProfileDetailRow(
                            label = "Sleep Time",
                            value = user.sleepTime.ifEmpty { "Not set" },
                            onClick = { showSleepDialog = true },
                            colors = colors
                        )
                        Divider(color = colors.outline.copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                        ProfileDetailRow(
                            label = "Exercise Level",
                            value = user.exerciseLevel.ifEmpty { "Not set" },
                            onClick = { showExerciseDialog = true },
                            colors = colors
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Logout Button
                Button(
                    onClick = {
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp),
                    shape = PixelShapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error,
                        contentColor = colors.onError
                    )
                ) {
                    Text("LOGOUT", style = PixelTypography.titleLarge)
                }
            } else {
                Text("No profile data available.", style = PixelTypography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }
    }

    // --- Dialogs for editing fields ---

    if (showGenderDialog) {
        EditGenderDialog(
            currentGender = userProfile?.gender ?: "",
            onDismiss = { showGenderDialog = false },
            onSave = { newGender ->
                viewModel.updateProfileField("gender", newGender)
                showGenderDialog = false
            }
        )
    }

    if (showAgeDialog) {
        EditAgeDialog(
            currentAge = userProfile?.age ?: 0,
            onDismiss = { showAgeDialog = false },
            onSave = { newAge ->
                viewModel.updateProfileField("age", newAge)
                showAgeDialog = false
            }
        )
    }

    if (showWeightDialog) {
        EditWeightDialog(
            currentWeight = userProfile?.weight ?: 0,
            onDismiss = { showWeightDialog = false },
            onSave = { newWeight ->
                viewModel.updateProfileField("weight", newWeight)
                showWeightDialog = false
            }
        )
    }

    if (showWakeupDialog) {
        LaunchedEffect(showWakeupDialog) {
            if (showWakeupDialog) {
                val timeParts = userProfile?.wakeupTime?.split(" ", ":") ?: listOf()
                val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
                val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
                val is24Hour = false

                val picker = TimePickerDialog(
                    context,
                    { _, selectedHour, selectedMinute ->
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, selectedHour)
                        cal.set(Calendar.MINUTE, selectedMinute)
                        val formattedTime = android.text.format.DateFormat.format("hh:mm a", cal).toString()
                        viewModel.updateProfileField("wakeupTime", formattedTime)
                        showWakeupDialog = false
                    },
                    hour,
                    minute,
                    is24Hour
                )
                picker.setOnCancelListener { showWakeupDialog = false }
                picker.show()
            }
        }
    }

    if (showSleepDialog) {
        LaunchedEffect(showSleepDialog) {
            if (showSleepDialog) {
                val timeParts = userProfile?.sleepTime?.split(" ", ":") ?: listOf()
                val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 22
                val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
                val is24Hour = false

                val picker = TimePickerDialog(
                    context,
                    { _, selectedHour, selectedMinute ->
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, selectedHour)
                        cal.set(Calendar.MINUTE, selectedMinute)
                        val formattedTime = android.text.format.DateFormat.format("hh:mm a", cal).toString()
                        viewModel.updateProfileField("sleepTime", formattedTime)
                        showSleepDialog = false
                    },
                    hour,
                    minute,
                    is24Hour
                )
                picker.setOnCancelListener { showSleepDialog = false }
                picker.show()
            }
        }
    }

    if (showExerciseDialog) {
        EditExerciseDialog(
            currentExercise = userProfile?.exerciseLevel ?: "",
            onDismiss = { showExerciseDialog = false },
            onSave = { newExercise ->
                viewModel.updateProfileField("exerciseLevel", newExercise)
                showExerciseDialog = false
            }
        )
    }
}

@Composable
fun ProfileDetailRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    colors: ColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                label,
                style = PixelTypography.labelLarge,
                color = colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = PixelTypography.titleMedium,
                color = colors.onSurface
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Edit $label",
            tint = colors.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun EditGenderDialog(
    currentGender: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var selectedGender by remember { mutableStateOf(currentGender) }
    val options = listOf("Male", "Female", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Gender") },
        text = {
            Column {
                options.forEach { gender ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { selectedGender = gender }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedGender == gender,
                            onClick = { selectedGender = gender }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(gender)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedGender.isNotBlank()) onSave(selectedGender)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditAgeDialog(
    currentAge: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var ageInput by remember { mutableStateOf(if (currentAge > 0) currentAge.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Age") },
        text = {
            TextField(
                value = ageInput,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) ageInput = input
                },
                label = { Text("Age") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val age = ageInput.toIntOrNull()
                    if (age != null && age > 0) onSave(age)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditWeightDialog(
    currentWeight: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var weightInput by remember { mutableStateOf(if (currentWeight > 0) currentWeight.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Weight (kg)") },
        text = {
            TextField(
                value = weightInput,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) weightInput = input
                },
                label = { Text("Weight") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val weight = weightInput.toIntOrNull()
                    if (weight != null && weight > 0) onSave(weight)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditExerciseDialog(
    currentExercise: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var selectedExercise by remember { mutableStateOf(currentExercise) }
    val options = listOf("None", "Occasionally", "Regularly")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Exercise Level") },
        text = {
            Column {
                options.forEach { level ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { selectedExercise = level }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedExercise == level,
                            onClick = { selectedExercise = level }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(level)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedExercise.isNotBlank()) onSave(selectedExercise)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
