package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PaymentCategory
import com.example.data.model.PaymentEntity
import com.example.ui.components.CashFlowCard
import com.example.ui.components.PaymentCard
import com.example.ui.components.UpiQrDialog
import com.example.ui.viewmodel.PaymentViewModel
import com.example.util.UpiHelper

@Composable
fun HomeScreen(
    viewModel: PaymentViewModel,
    onAddNewPayment: () -> Unit,
    onOpenCalculators: () -> Unit,
    onViewForecast: () -> Unit,
    onEditBalance: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val payments by viewModel.filteredPayments.collectAsStateWithLifecycle()
    val metrics by viewModel.summaryMetrics.collectAsStateWithLifecycle()
    val bankBalance by viewModel.userBankBalance.collectAsStateWithLifecycle()
    val currentTab by viewModel.selectedTabFilter.collectAsStateWithLifecycle()
    val currentCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var isSearchVisible by remember { mutableStateOf(false) }

    // Dialog States
    var paymentForQr by remember { mutableStateOf<PaymentEntity?>(null) }
    var paymentToEdit by remember { mutableStateOf<PaymentEntity?>(null) }
    var paymentToDelete by remember { mutableStateOf<PaymentEntity?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("home_screen")
        ) {
            // App Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "₹",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }

                    Column {
                        Text(
                            text = "PayCal UPI",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Upcoming Bills & Autopay Calculator",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { isSearchVisible = !isSearchVisible },
                        modifier = Modifier.testTag("search_toggle_button")
                    ) {
                        Icon(
                            if (isSearchVisible) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    }
                }
            }

            // Search Bar (Expandable)
            AnimatedVisibility(visible = isSearchVisible) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search by bill, merchant or UPI ID...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("search_input_field"),
                    singleLine = true
                )
            }

            // Main Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Cash Flow Hero Card
                item {
                    CashFlowCard(
                        metrics = metrics,
                        bankBalance = bankBalance,
                        onEditBalance = onEditBalance,
                        onViewForecast = onViewForecast
                    )
                }

                // Filter Tabs (All, Soon, Autopay, Completed)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "ALL" to "All Upcoming",
                            "SOON" to "Due Soon (≤7d)",
                            "AUTOPAY" to "Autopay Mandates",
                            "COMPLETED" to "Paid / Completed"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = currentTab == key,
                                onClick = { viewModel.selectedTabFilter.value = key },
                                label = { Text(label, fontSize = 12.sp, fontWeight = if (currentTab == key) FontWeight.Bold else FontWeight.Normal) },
                                modifier = Modifier.testTag("tab_filter_$key")
                            )
                        }
                    }
                }

                // Category Chips Filter
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = currentCategory == null,
                            onClick = { viewModel.selectedCategoryFilter.value = null },
                            label = { Text("All Categories", fontSize = 11.sp) }
                        )

                        PaymentCategory.values().forEach { cat ->
                            FilterChip(
                                selected = currentCategory == cat,
                                onClick = {
                                    viewModel.selectedCategoryFilter.value =
                                        if (currentCategory == cat) null else cat
                                },
                                label = { Text(cat.displayName, fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        cat.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                // Empty State
                if (payments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Payment,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "No Matching Payments Found" else "No Upcoming Payments in this Filter",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Calculate and schedule upcoming bills or autopay mandates with 1 tap.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onAddNewPayment,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Upcoming Payment")
                                }
                            }
                        }
                    }
                } else {
                    // Payment List Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Scheduled Payments (${payments.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Total: ${UpiHelper.formatInr(payments.sumOf { it.amount })}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Payment Cards
                    items(payments, key = { it.id }) { payment ->
                        PaymentCard(
                            payment = payment,
                            onPayUpi = {
                                UpiHelper.launchUpiPayment(
                                    context = context,
                                    upiId = payment.payeeUpiId,
                                    payeeName = payment.payeeName.ifBlank { payment.title },
                                    amount = payment.amount,
                                    note = payment.title,
                                    onSuccessLaunched = {
                                        Toast.makeText(context, "Opening UPI payment...", Toast.LENGTH_SHORT).show()
                                    },
                                    onFailure = { err ->
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            onShowQr = {
                                paymentForQr = payment
                            },
                            onMarkPaid = {
                                if (payment.isPaid) {
                                    viewModel.unmarkPaid(payment.id)
                                    Toast.makeText(context, "Marked unpaid", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.markPaid(payment)
                                    Toast.makeText(context, "Marked as paid! Added to history", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onEdit = {
                                paymentToEdit = payment
                            },
                            onDelete = {
                                paymentToDelete = payment
                            },
                            onToggleAutopay = {
                                viewModel.toggleAutopay(payment)
                                val state = if (!payment.isAutopayEnabled) "enabled" else "disabled"
                                Toast.makeText(context, "Autopay mandate $state", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onAddNewPayment,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_payment"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add Payment", modifier = Modifier.size(24.dp))
        }
    }

    // QR Code Dialog
    paymentForQr?.let { p ->
        UpiQrDialog(
            payment = p,
            onDismiss = { paymentForQr = null },
            onPayClicked = {
                paymentForQr = null
                UpiHelper.launchUpiPayment(
                    context = context,
                    upiId = p.payeeUpiId,
                    payeeName = p.payeeName.ifBlank { p.title },
                    amount = p.amount,
                    note = p.title
                )
            }
        )
    }

    // Edit Payment Dialog
    paymentToEdit?.let { p ->
        AddEditPaymentDialog(
            initialPayment = p,
            onDismiss = { paymentToEdit = null },
            onSave = { updated ->
                viewModel.savePayment(updated)
                paymentToEdit = null
                Toast.makeText(context, "Payment updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Delete Confirmation Dialog
    paymentToDelete?.let { p ->
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            title = { Text("Delete Payment?") },
            text = { Text("Are you sure you want to remove \"${p.title}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePayment(p.id)
                        paymentToDelete = null
                        Toast.makeText(context, "Payment deleted", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { paymentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
