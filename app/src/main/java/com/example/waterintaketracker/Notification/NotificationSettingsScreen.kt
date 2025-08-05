package com.example.waterintaketracker.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.res.stringResource // No longer needed for direct strings
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
// import com.example.waterintaketracker.R // No longer needed if all strings are direct
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    navController: NavHostController,
    viewModel: NotificationSettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }
    var shouldDirectToSettings by remember { mutableStateOf(false) }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) true
            else ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val currentScheduleState by viewModel.notificationSchedule.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            println("NotificationSettingsScreen: Permission GRANTED by user.")
            if (currentScheduleState?.notificationsEnabled == true) {
                viewModel.ensureRemindersAreScheduledIfNeeded()
            }
            shouldDirectToSettings = false
        } else {
            println("NotificationSettingsScreen: Permission DENIED by user.")
            if (currentScheduleState?.notificationsEnabled == true) {
                showPermissionRationaleDialog = true
            }
        }
    }

    LaunchedEffect(key1 = hasNotificationPermission, key2 = currentScheduleState?.notificationsEnabled) {
        val schedule = currentScheduleState
        if (schedule != null) {
            if (schedule.notificationsEnabled) {
                if (hasNotificationPermission) {
                    println("NotificationSettingsScreen: State check - Perm OK & Notifs Enabled. Ensuring schedule.")
                    viewModel.ensureRemindersAreScheduledIfNeeded()
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        println("NotificationSettingsScreen: State check - Notifs Enabled but Perm MISSING. Requesting.")
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            } else {
                println("NotificationSettingsScreen: State check - Notifications are disabled. Ensuring work is cancelled.")
                viewModel.ensureRemindersAreScheduledIfNeeded()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Settings") }, // Direct String
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back") // Direct String
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val schedule = currentScheduleState

            if (schedule == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                    Text("Loading settings...", modifier = Modifier.padding(start = 8.dp)) // Direct String
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Enable Water Reminders", // Direct String
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = schedule.notificationsEnabled && hasNotificationPermission,
                        onCheckedChange = { userWantsToEnable ->
                            if (userWantsToEnable) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.updateNotificationsEnabled(true)
                                }
                            } else {
                                viewModel.updateNotificationsEnabled(false)
                            }
                        }
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission && schedule.notificationsEnabled) {
                    Text(
                        "Notification permission is required to send reminders. Tap here to grant permission.", // Direct String
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clickable {
                            shouldDirectToSettings = true
                            showPermissionRationaleDialog = true
                        }
                    )
                }

                TimeSelectorRow(
                    label = "Wake Up Time", // Direct String
                    selectedTime = schedule.wakeUpTime,
                    onTimeSelected = { newTime -> viewModel.updateUserWakeUpTime(newTime) },
                    enabled = schedule.notificationsEnabled && hasNotificationPermission
                )

                TimeSelectorRow(
                    label = "Sleep Time", // Direct String
                    selectedTime = schedule.sleepTime,
                    onTimeSelected = { newTime -> viewModel.updateUserSleepTime(newTime) },
                    enabled = schedule.notificationsEnabled && hasNotificationPermission
                )

                Spacer(modifier = Modifier.height(16.dp))

                val wakeUpFormatted = schedule.wakeUpTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
                val sleepFormatted = schedule.sleepTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
                Text(
                    "Reminders will be sent periodically between your wake up time ($wakeUpFormatted) and sleep time ($sleepFormatted).", // Direct String with formatting
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showPermissionRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionRationaleDialog = false },
            title = { Text("Permission Needed") }, // Direct String
            text = { Text("To send you water reminders, this app needs permission to post notifications. Please grant this permission.") }, // Direct String
            confirmButton = {
                Button(onClick = {
                    showPermissionRationaleDialog = false
                    if (shouldDirectToSettings) {
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
                            it.data = Uri.fromParts("package", context.packageName, null)
                            context.startActivity(it)
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }) {
                    Text(if (shouldDirectToSettings) "Open Settings" else "Grant Permission") // Direct Strings
                }
            },
            dismissButton = {
                Button(onClick = { showPermissionRationaleDialog = false }) {
                    Text("Cancel") // Direct String
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TimeSelectorRow(
    label: String, // Kept as parameter for flexibility, could be direct string here too
    selectedTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    enabled: Boolean
) {
    val context = LocalContext.current
    val timeFormatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) {
                if (enabled) {
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            onTimeSelected(LocalTime.of(hourOfDay, minute))
                        },
                        selectedTime.hour,
                        selectedTime.minute,
                        false // Use system default (usually 12-hour if locale supports it)
                    ).show()
                }
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        Text(
            text = selectedTime.format(timeFormatter),
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}
