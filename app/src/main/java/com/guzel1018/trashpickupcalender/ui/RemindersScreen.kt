package com.guzel1018.trashpickupcalender.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.guzel1018.trashpickupcalender.data.ReminderPreferences
import com.guzel1018.trashpickupcalender.data.UserAddress
import com.guzel1018.trashpickupcalender.model.DatedCalendarItem
import com.guzel1018.trashpickupcalender.utils.ReminderScheduler
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

enum class ReminderDayOption(val displayText: String) {
    DAY_BEFORE("Am Vortag"),
    TWO_DAYS_BEFORE("Zwei Tage Vorher"),
    THREE_DAYS_BEFORE("Drei Tage Vorher"),
    PREVIOUS_SUNDAY("Am vorherigen Sonntag")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RemindersScreen(
    savedAddress: UserAddress,
    navController: NavHostController,
    events: List<DatedCalendarItem>?,
    reminderPreferences: ReminderPreferences,
    onSaveReminderPreferences: (String, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Convert saved dayOption string to enum
    val initialDayOption = try {
        ReminderDayOption.valueOf(reminderPreferences.dayOption)
    } catch (e: Exception) {
        ReminderDayOption.DAY_BEFORE
    }
    
    var selectedDayOption by remember(reminderPreferences.dayOption) { mutableStateOf(initialDayOption) }
    var selectedTime by remember(reminderPreferences.hour, reminderPreferences.minute) { 
        mutableStateOf(LocalTime.of(reminderPreferences.hour, reminderPreferences.minute)) 
    }
    var showDayBottomSheet by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showExactAlarmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteSuccessDialog by remember { mutableStateOf(false) }
    
    // Notification permission for Android 13+
    val notificationPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.POST_NOTIFICATIONS
    )
    
    // Helper function to check if exact alarm permission is granted
    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // No permission needed for older versions
        }
    }
    
    // Helper function to open exact alarm settings
    fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    }
    
    // Create notification channel on first composition
    LaunchedEffect(Unit) {
        ReminderScheduler.createNotificationChannel(context)
    }

    Scaffold { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Title with back button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        navController.navigate(FilterScreen.Details.name) {
                            popUpTo(FilterScreen.Details.name) {
                                inclusive = true
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Zurück",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Erinnerungen",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Subtitle
            Text(
                text = "Ihr Standort",
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Town name - bold and bigger
            Text(
                text = savedAddress.townName ?: "",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Street name if available
            if (!savedAddress.streetName.isNullOrBlank()) {
                Text(
                    text = savedAddress.streetName,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Reminder configuration section
            Text(
                text = "Zeitpunkt der Erinnerung",
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Day selection dropdown
            ReminderDropdown(
                label = "Zeitpunkt",
                value = selectedDayOption.displayText,
                onClick = { showDayBottomSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Time selection dropdown
            ReminderTimeDropdown(
                time = selectedTime,
                onClick = { showTimePicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            // Space for future buttons
            Spacer(modifier = Modifier.weight(1f))
            
            // Create reminders button
            Button(
                onClick = {
                    // First check if we can schedule exact alarms (Android 12+)
                    if (!canScheduleExactAlarms()) {
                        showExactAlarmDialog = true
                        return@Button
                    }
                    
                    // Then check notification permission (Android 13+)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        if (notificationPermissionState.status.isGranted) {
                            // All permissions granted, schedule reminders
                            events?.let { eventList ->
                                ReminderScheduler.scheduleReminders(
                                    context,
                                    eventList,
                                    selectedDayOption,
                                    selectedTime
                                )
                                showSuccessDialog = true
                            }
                        } else {
                            // Request notification permission
                            notificationPermissionState.launchPermissionRequest()
                        }
                    } else {
                        // No notification permission needed for older Android versions
                        events?.let { eventList ->
                            ReminderScheduler.scheduleReminders(
                                context,
                                eventList,
                                selectedDayOption,
                                selectedTime
                            )
                            showSuccessDialog = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(text = "Erstellen", fontSize = 18.sp)
            }
            
            // Delete existing reminders button
            Button(
                onClick = {
                    showDeleteConfirmDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            ) {
                Text(text = "Vorhandene Erinnerungen löschen", fontSize = 18.sp)
            }
        }
    }
    
    // Exact Alarm permission dialog
    if (showExactAlarmDialog) {
        AlertDialog(
            onDismissRequest = { showExactAlarmDialog = false },
            title = {
                Text(text = "Genaue Alarme erforderlich")
            },
            text = {
                Text(text = "Um zuverlässige Erinnerungen zu erhalten, benötigt die App die Berechtigung, genaue Alarme zu planen. Bitte aktivieren Sie diese in den Einstellungen.")
            },
            confirmButton = {
                Button(onClick = {
                    showExactAlarmDialog = false
                    openExactAlarmSettings()
                }) {
                    Text("Zu Einstellungen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExactAlarmDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
    
    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = {
                Text(text = "Erinnerungen erstellt!")
            },
            text = {
                Text(text = "Ihre Erinnerungen wurden erfolgreich eingerichtet. Sie werden benachrichtigt, wenn es Zeit ist, die Mülltonnen rauszustellen.")
            },
            confirmButton = {
                Button(onClick = {
                    showSuccessDialog = false
                    navController.navigate(FilterScreen.Details.name) {
                        popUpTo(FilterScreen.Details.name) {
                            inclusive = true
                        }
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Delete confirmation dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(text = "Erinnerungen löschen?")
            },
            text = {
                Text(text = "Möchten Sie wirklich alle bestehenden Erinnerungen löschen? Diese Aktion kann nicht rückgängig gemacht werden.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        // Delete all reminders
                        events?.let { eventList ->
                            ReminderScheduler.cancelAllReminders(context, eventList.size)
                            showDeleteSuccessDialog = true
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
    
    // Delete success dialog
    if (showDeleteSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSuccessDialog = false },
            title = {
                Text(text = "Erinnerungen gelöscht")
            },
            text = {
                Text(text = "Alle Erinnerungen wurden erfolgreich gelöscht.")
            },
            confirmButton = {
                Button(onClick = {
                    showDeleteSuccessDialog = false
                }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Check if permission was just granted
    LaunchedEffect(notificationPermissionState.status) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (notificationPermissionState.status.isGranted) {
                // Permission was just granted, schedule reminders if we have events
                events?.let { eventList ->
                    if (eventList.isNotEmpty()) {
                        // Only schedule if button was pressed (add a flag if needed)
                        // For now we skip auto-scheduling on permission grant
                    }
                }
            }
        }
    }

    // Day selection dialog
    if (showDayBottomSheet) {
        AlertDialog(
            onDismissRequest = { showDayBottomSheet = false },
            title = {
                Text(text = "Zeitpunkt wählen")
            },
            text = {
                Column {
                    ReminderDayOption.values().forEach { option ->
                        ReminderDayOptionItem(
                            text = option.displayText,
                            isSelected = option == selectedDayOption,
                            onClick = {
                                selectedDayOption = option
                                showDayBottomSheet = false
                                // Save the preference
                                onSaveReminderPreferences(
                                    option.name,
                                    selectedTime.hour,
                                    selectedTime.minute
                                )
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDayBottomSheet = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    // Time picker dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialTime = selectedTime,
            onTimeSelected = { time ->
                selectedTime = time
                showTimePicker = false
                // Save the preference
                onSaveReminderPreferences(
                    selectedDayOption.name,
                    time.hour,
                    time.minute
                )
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
private fun ReminderDropdown(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = value,
                    fontSize = 16.sp
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun ReminderTimeDropdown(
    time: LocalTime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = time.format(DateTimeFormatter.ofPattern("HH:mm")),
                fontSize = 16.sp
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun ReminderDayOptionItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Wählen Sie die Uhrzeit für die Erinnerung")
        },
        text = {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                }
            ) {
                Text("Übernehmen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

