package com.example.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CalculationType
import com.example.data.model.PaymentCategory
import com.example.ui.components.BalanceEditorDialog
import com.example.ui.screens.AddEditPaymentDialog
import com.example.ui.screens.CalculatorsScreen
import com.example.ui.screens.CashFlowScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.viewmodel.PaymentViewModel
import com.example.util.UpiHelper

enum class AppDestination(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Upcoming", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_upcoming"),
    CALCULATORS("Calculators", Icons.Filled.Calculate, Icons.Outlined.Calculate, "nav_calculators"),
    FORECAST("Cash Flow", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "nav_cash_flow"),
    HISTORY("History", Icons.Filled.History, Icons.Outlined.History, "nav_history")
}

@Composable
fun AppNavigation(
    viewModel: PaymentViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }

    // Dialog state for adding a payment
    var showAddPaymentDialog by remember { mutableStateOf(false) }
    var prefillAmount by remember { mutableStateOf<Double?>(null) }
    var prefillTitle by remember { mutableStateOf<String?>(null) }
    var prefillCalcType by remember { mutableStateOf<CalculationType?>(null) }
    var prefillCalcDetails by remember { mutableStateOf<String?>(null) }
    var prefillCategory by remember { mutableStateOf<PaymentCategory?>(null) }

    // Dialog state for editing bank balance
    var showBalanceDialog by remember { mutableStateOf(false) }
    val currentBalance by viewModel.userBankBalance.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                AppDestination.values().forEach { destination ->
                    val isSelected = currentDestination == destination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = destination },
                        modifier = Modifier.testTag(destination.testTag),
                        icon = {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.label
                            )
                        },
                        label = {
                            Text(
                                text = destination.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentDestination, label = "ScreenTransition") { destination ->
                when (destination) {
                    AppDestination.HOME -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onAddNewPayment = {
                                prefillAmount = null
                                prefillTitle = null
                                prefillCalcType = null
                                prefillCalcDetails = null
                                prefillCategory = null
                                showAddPaymentDialog = true
                            },
                            onOpenCalculators = {
                                currentDestination = AppDestination.CALCULATORS
                            },
                            onViewForecast = {
                                currentDestination = AppDestination.FORECAST
                            },
                            onEditBalance = {
                                showBalanceDialog = true
                            }
                        )
                    }

                    AppDestination.CALCULATORS -> {
                        CalculatorsScreen(
                            viewModel = viewModel,
                            onSchedulePayment = { amount, title, calcType, details, category ->
                                prefillAmount = amount
                                prefillTitle = title
                                prefillCalcType = calcType
                                prefillCalcDetails = details
                                prefillCategory = category
                                showAddPaymentDialog = true
                            }
                        )
                    }

                    AppDestination.FORECAST -> {
                        CashFlowScreen(
                            viewModel = viewModel,
                            onEditBalance = {
                                showBalanceDialog = true
                            },
                            onPayUpi = { payment ->
                                UpiHelper.launchUpiPayment(
                                    context = context,
                                    upiId = payment.payeeUpiId,
                                    payeeName = payment.payeeName.ifBlank { payment.title },
                                    amount = payment.amount,
                                    note = payment.title
                                )
                            }
                        )
                    }

                    AppDestination.HISTORY -> {
                        HistoryScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // Add Payment Dialog (opened either from FAB or from one of the Calculators)
    if (showAddPaymentDialog) {
        AddEditPaymentDialog(
            initialPayment = null,
            prefilledAmount = prefillAmount,
            prefilledTitle = prefillTitle,
            prefilledCalcType = prefillCalcType,
            prefilledCalcDetails = prefillCalcDetails,
            prefilledCategory = prefillCategory,
            onDismiss = {
                showAddPaymentDialog = false
            },
            onSave = { newPayment ->
                viewModel.savePayment(newPayment)
                showAddPaymentDialog = false
                currentDestination = AppDestination.HOME
            }
        )
    }

    // Bank Balance Simulation Dialog
    if (showBalanceDialog) {
        BalanceEditorDialog(
            currentBalance = currentBalance,
            onDismiss = { showBalanceDialog = false },
            onSave = { updatedBalance ->
                viewModel.updateBankBalance(updatedBalance)
                showBalanceDialog = false
            }
        )
    }
}
