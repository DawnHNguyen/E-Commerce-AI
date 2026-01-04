package com.ptit.core.discount.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.ptit.common.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    label: String,
    value: String, // ISO format: "2025-12-28T01:45:02.664Z"
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Parse ISO date to display format
    val displayValue = remember(value) {
        try {
            if (value.isBlank()) return@remember ""
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = isoFormat.parse(value)
            val displayFormat = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
            displayFormat.format(date ?: Date())
        } catch (e: Exception) {
            value
        }
    }

    // Current selected date and time
    var selectedCalendar by remember(value) {
        mutableStateOf(try {
            if (value.isBlank()) Calendar.getInstance() else {
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                Calendar.getInstance().apply {
                    time = isoFormat.parse(value) ?: Date()
                }
            }
        } catch (e: Exception) {
            Calendar.getInstance()
        })
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = { showTimePicker = true }) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = "Chọn giờ",
                            tint = colorResource(R.color.colorSystem_heading_button)
                        )
                    }
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Chọn ngày",
                            tint = colorResource(R.color.colorSystem_heading_button)
                        )
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorResource(R.color.colorSystem_heading_button),
                focusedLabelColor = colorResource(R.color.colorSystem_heading_button),
                cursorColor = colorResource(R.color.colorSystem_heading_button)
            )
        )

        // Date Picker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedCalendar.timeInMillis
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedCalendar.timeInMillis = millis
                            updateIsoValue(selectedCalendar, onValueChange)
                        }
                        showDatePicker = false
                    }) {
                        Text("OK", color = colorResource(R.color.colorSystem_heading_button))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Hủy")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        selectedDayContainerColor = colorResource(R.color.colorSystem_heading_button)
                    )
                )
            }
        }

        // Time Picker Dialog
        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = selectedCalendar.get(Calendar.HOUR_OF_DAY),
                initialMinute = selectedCalendar.get(Calendar.MINUTE)
            )

            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        selectedCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        selectedCalendar.set(Calendar.MINUTE, timePickerState.minute)
                        updateIsoValue(selectedCalendar, onValueChange)
                        showTimePicker = false
                    }) {
                        Text("OK", color = colorResource(R.color.colorSystem_heading_button))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text("Hủy")
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Chọn giờ", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        TimePicker(
                            state = timePickerState,
                            colors = TimePickerDefaults.colors(
                                selectorColor = colorResource(R.color.colorSystem_heading_button)
                            )
                        )
                    }
                }
            )
        }
    }
}

private fun updateIsoValue(calendar: Calendar, onValueChange: (String) -> Unit) {
    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val isoString = isoFormat.format(calendar.time)
    onValueChange(isoString)
}

