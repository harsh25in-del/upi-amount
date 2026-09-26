package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.calc.Calculators
import com.example.data.model.CalculationType
import com.example.data.model.PaymentCategory
import com.example.data.model.PaymentEntity
import com.example.data.model.PaymentFrequency
import com.example.util.UpiHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditPaymentDialog(
    initialPayment: PaymentEntity? = null,
    prefilledAmount: Double? = null,
    prefilledTitle: String? = null,
    prefilledCalcType: CalculationType? = null,
    prefilledCalcDetails: String? = null,
    prefilledCategory: PaymentCategory? = null,
    onDismiss: () -> Unit,
    onSave: (PaymentEntity) -> Unit
) {
    val context = LocalContext.current

    var title by remember {
        mutableStateOf(
            prefilledTitle ?: initialPayment?.title ?: ""
        )
    }
    var payeeName by remember { mutableStateOf(initialPayment?.payeeName ?: "") }
    var payeeUpiId by remember { mutableStateOf(initialPayment?.payeeUpiId ?: "") }

    var amountText by remember {
        val amt = prefilledAmount ?: initialPayment?.amount ?: 0.0
        mutableStateOf(if (amt > 0) String.format(Locale.US, "%.2f", amt) else "")
    }

    var selectedCategory by remember {
        mutableStateOf(
            prefilledCategory ?: initialPayment?.categoryEnum ?: PaymentCategory.BILLS_UTILITY
        )
    }

    var selectedFrequency by remember {
        mutableStateOf(initialPayment?.frequencyEnum ?: PaymentFrequency.MONTHLY)
    }

    var selectedCalcType by remember {
        mutableStateOf(
            prefilledCalcType ?: initialPayment?.calcTypeEnum ?: CalculationType.MANUAL
        )
    }

    var calcDetails by remember {
        mutableStateOf(
            prefilledCalcDetails ?: initialPayment?.calcDetails ?: ""
        )
    }

    val defaultDueDate = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 3) }.timeInMillis
    }
    var dueTimestamp by remember {
        mutableLongStateOf(initialPayment?.dueTimestamp ?: defaultDueDate)
    }

    var isAutopay by remember {
        mutableStateOf(initialPayment?.isAutopayEnabled ?: false)
    }

    var notes by remember { mutableStateOf(initialPayment?.notes ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 680.dp)
                .testTag("add_edit_payment_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (initialPayment == null) "Schedule UPI Payment" else "Edit Payment",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Auto-calculate amount & set UPI recurring mandate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_sheet_button")) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title Input
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Payment Title *") },
                        placeholder = { Text("e.g., Tata Power Electricity, Netflix, Flat Rent") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_title"),
                        singleLine = true
                    )

                    // Amount & Quick Calculator Tag
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Amount (₹) *") },
                            placeholder = { Text("0.00") },
                            leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_amount"),
                            singleLine = true
                        )

                        // Quick Calculation Mode Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Mode", fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(
                                    selectedCalcType.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    if (calcDetails.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Calculate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = calcDetails,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    // Payee UPI ID & Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = payeeUpiId,
                            onValueChange = { payeeUpiId = it },
                            label = { Text("Payee UPI ID") },
                            placeholder = { Text("username@okhdfcbank") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_upi_id"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = payeeName,
                            onValueChange = { payeeName = it },
                            label = { Text("Payee Name") },
                            placeholder = { Text("Merchant / Person") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_payee_name"),
                            singleLine = true
                        )
                    }

                    // Category Selection Chips
                    Column {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PaymentCategory.values().forEach { cat ->
                                val isSelected = cat == selectedCategory
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat.displayName, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            cat.icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier.testTag("category_chip_${cat.name}")
                                )
                            }
                        }
                    }

                    // Frequency Selection
                    Column {
                        Text(
                            text = "Recurring Frequency",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PaymentFrequency.values().forEach { freq ->
                                val isSelected = freq == selectedFrequency
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedFrequency = freq },
                                    label = { Text(freq.displayName, fontSize = 12.sp) },
                                    modifier = Modifier.testTag("frequency_chip_${freq.name}")
                                )
                            }
                        }
                    }

                    // Due Date Selection
                    Column {
                        Text(
                            text = "Next Due Date",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showDatePicker = true }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("date_picker_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val dateStr = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
                                        .format(Date(dueTimestamp))
                                    Text(dateStr, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Icon(
                                        Icons.Filled.CalendarMonth,
                                        contentDescription = "Pick date",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Quick preset: In 7 Days
                            FilterChip(
                                selected = false,
                                onClick = {
                                    dueTimestamp = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 7) }.timeInMillis
                                },
                                label = { Text("+7d") }
                            )

                            // Quick preset: Month 1st
                            FilterChip(
                                selected = false,
                                onClick = {
                                    val c = Calendar.getInstance()
                                    c.add(Calendar.MONTH, 1)
                                    c.set(Calendar.DAY_OF_MONTH, 1)
                                    dueTimestamp = c.timeInMillis
                                },
                                label = { Text("1st") }
                            )
                        }
                    }

                    // Autopay Toggle
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Autorenew,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        "UPI Autopay Mandate Active",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Will auto-deduct from bank on due date",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = isAutopay,
                                onCheckedChange = { isAutopay = it },
                                modifier = Modifier.testTag("autopay_switch")
                            )
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Consumer ID (Optional)") },
                        placeholder = { Text("e.g. Meter # 893201") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_notes"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom CTA Button
                Button(
                    onClick = {
                        val parsedAmount = amountText.toDoubleOrNull()
                        if (title.isBlank()) {
                            Toast.makeText(context, "Please enter a payment title", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (parsedAmount == null || parsedAmount <= 0.0) {
                            Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val payment = PaymentEntity(
                            id = initialPayment?.id ?: 0,
                            title = title.trim(),
                            payeeUpiId = payeeUpiId.trim(),
                            payeeName = payeeName.trim(),
                            amount = parsedAmount,
                            frequency = selectedFrequency.name,
                            category = selectedCategory.name,
                            calculationType = selectedCalcType.name,
                            calcDetails = calcDetails,
                            dueTimestamp = dueTimestamp,
                            isAutopayEnabled = isAutopay,
                            isPaid = initialPayment?.isPaid ?: false,
                            notes = notes.trim()
                        )
                        onSave(payment)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_payment_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (initialPayment == null) "Schedule Payment" else "Save Changes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueTimestamp
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            dueTimestamp = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
