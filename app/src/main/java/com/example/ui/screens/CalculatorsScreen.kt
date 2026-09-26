package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calc.Calculators
import com.example.data.model.CalculationType
import com.example.data.model.PaymentCategory
import com.example.ui.viewmodel.PaymentViewModel
import com.example.util.UpiHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CalculatorsScreen(
    viewModel: PaymentViewModel,
    onSchedulePayment: (amount: Double, title: String, calcType: CalculationType, calcDetails: String, category: PaymentCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabTitles = listOf(
        "EMI & Loan",
        "Utility Slabs",
        "Daily Rate",
        "Split Bill",
        "Plan Prorate"
    )

    val tabIcons = listOf(
        Icons.Filled.AccountBalance,
        Icons.Filled.Bolt,
        Icons.Filled.CleaningServices,
        Icons.Filled.Group,
        Icons.Filled.Wifi
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("calculators_screen")
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Auto-Calculators",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Calculate exact amounts and schedule directly as upcoming UPI payments",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Scrollable Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            },
            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)) }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.testTag("calc_tab_$index"),
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                tabIcons[index],
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedTab == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                )
            }
        }

        // Active Tab Screen Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> EmiCalculatorTab(viewModel, onSchedulePayment)
                1 -> UtilitySlabCalculatorTab(viewModel, onSchedulePayment)
                2 -> DailyRateCalculatorTab(viewModel, onSchedulePayment)
                3 -> SplitBillCalculatorTab(viewModel, onSchedulePayment)
                4 -> PlanProrationCalculatorTab(viewModel, onSchedulePayment)
            }
        }
    }
}

// -------------------------------------------------------------
// 1. EMI & Loan Calculator Tab
// -------------------------------------------------------------
@Composable
fun EmiCalculatorTab(
    viewModel: PaymentViewModel,
    onSchedulePayment: (amount: Double, title: String, calcType: CalculationType, calcDetails: String, category: PaymentCategory) -> Unit
) {
    val principal by viewModel.emiPrincipal.collectAsStateWithLifecycle()
    val rate by viewModel.emiRate.collectAsStateWithLifecycle()
    val tenure by viewModel.emiTenureMonths.collectAsStateWithLifecycle()
    val result by viewModel.emiResult.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Result Hero Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Calculated Monthly EMI",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = UpiHelper.formatInr(result.monthlyEmi),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Principal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(UpiHelper.formatInr(result.principal), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Interest", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(UpiHelper.formatInr(result.totalInterest), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Payable", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(UpiHelper.formatInr(result.totalPayment), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Inputs Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Principal
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Loan Principal Amount", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(UpiHelper.formatInr(principal), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = principal.toFloat(),
                        onValueChange = { viewModel.emiPrincipal.value = it.toDouble() },
                        valueRange = 10000f..2000000f,
                        steps = 199,
                        modifier = Modifier.testTag("emi_principal_slider")
                    )
                }

                // Interest Rate
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Annual Interest Rate", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("${String.format(Locale.US, "%.1f", rate)}% p.a.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = rate.toFloat(),
                        onValueChange = { viewModel.emiRate.value = it.toDouble() },
                        valueRange = 5f..25f,
                        steps = 40,
                        modifier = Modifier.testTag("emi_rate_slider")
                    )
                }

                // Tenure Months
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tenure Duration", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("$tenure Months (${tenure / 12}y ${tenure % 12}m)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = tenure.toFloat(),
                        onValueChange = { viewModel.emiTenureMonths.value = it.toInt() },
                        valueRange = 3f..84f,
                        steps = 81,
                        modifier = Modifier.testTag("emi_tenure_slider")
                    )
                }
            }
        }

        // Schedule CTA
        Button(
            onClick = {
                val details = "Loan ${UpiHelper.formatInr(principal)} @ ${rate}% for ${tenure}m"
                onSchedulePayment(result.monthlyEmi, "Loan EMI Payment", CalculationType.EMI_LOAN, details, PaymentCategory.EMI_LOAN)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("schedule_emi_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Schedule as Monthly UPI Autopay", fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------
// 2. Utility & Electricity Slab Calculator Tab
// -------------------------------------------------------------
@Composable
fun UtilitySlabCalculatorTab(
    viewModel: PaymentViewModel,
    onSchedulePayment: (amount: Double, title: String, calcType: CalculationType, calcDetails: String, category: PaymentCategory) -> Unit
) {
    val units by viewModel.utilityUnits.collectAsStateWithLifecycle()
    val fixed by viewModel.utilityFixedCharge.collectAsStateWithLifecycle()
    val tax by viewModel.utilityTaxRate.collectAsStateWithLifecycle()
    val result by viewModel.utilityResult.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Result Hero
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Estimated Utility Bill Amount",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = UpiHelper.formatInr(result.totalPayable),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "${units.toInt()} units consumed • Energy: ${UpiHelper.formatInr(result.energyCharges)} • Fixed/Duty: ${UpiHelper.formatInr(result.fixedCharges + result.taxDutyAmount)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                )
            }
        }

        // Slab Breakdown List
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Tiered Slab Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                result.slabCosts.forEach { slab ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(slab.slabDescription, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("${slab.unitsInSlab.toInt()} units @ ₹${slab.ratePerUnit}/unit", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(UpiHelper.formatInr(slab.cost), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Fixed Meter Charge", fontSize = 12.sp)
                    Text(UpiHelper.formatInr(result.fixedCharges), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Electricity Duty / Surcharge (${tax}%)", fontSize = 12.sp)
                    Text(UpiHelper.formatInr(result.taxDutyAmount), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Controls
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Units Consumed (kWh)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("${units.toInt()} Units", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                    Slider(
                        value = units.toFloat(),
                        onValueChange = { viewModel.utilityUnits.value = it.toDouble() },
                        valueRange = 10f..1000f,
                        steps = 99,
                        modifier = Modifier.testTag("utility_units_slider")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = fixed.toInt().toString(),
                        onValueChange = { it.toDoubleOrNull()?.let { f -> viewModel.utilityFixedCharge.value = f } },
                        label = { Text("Fixed (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tax.toString(),
                        onValueChange = { it.toDoubleOrNull()?.let { t -> viewModel.utilityTaxRate.value = t } },
                        label = { Text("Tax %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        // Schedule CTA
        Button(
            onClick = {
                val details = "${units.toInt()} units slab calc (Energy: ${UpiHelper.formatInr(result.energyCharges)} + Fixed/Tax: ${UpiHelper.formatInr(result.fixedCharges + result.taxDutyAmount)})"
                onSchedulePayment(result.totalPayable, "Electricity Bill", CalculationType.UTILITY_SLABS, details, PaymentCategory.BILLS_UTILITY)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("schedule_utility_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Schedule as Upcoming Utility Bill", fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------
// 3. Daily Rate Multiplier Tab (Services / Milk / Maid / Cook)
// -------------------------------------------------------------
@Composable
fun DailyRateCalculatorTab(
    viewModel: PaymentViewModel,
    onSchedulePayment: (amount: Double, title: String, calcType: CalculationType, calcDetails: String, category: PaymentCategory) -> Unit
) {
    val rate by viewModel.dailyRate.collectAsStateWithLifecycle()
    val totalDays by viewModel.dailyTotalDays.collectAsStateWithLifecycle()
    val leaves by viewModel.dailyLeaves.collectAsStateWithLifecycle()
    val extra by viewModel.dailyExtra.collectAsStateWithLifecycle()
    val result by viewModel.dailyResult.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Result Hero
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Calculated Month-End Payable",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = UpiHelper.formatInr(result.netPayable),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Formula: (${totalDays}d - ${leaves} leaves) × ${UpiHelper.formatInr(rate)}/d + ${UpiHelper.formatInr(extra)} extra",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                )
            }
        }

        // Inputs Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Daily Rate (₹/day or ₹/litre)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(UpiHelper.formatInr(rate), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = rate.toFloat(),
                        onValueChange = { viewModel.dailyRate.value = it.toDouble() },
                        valueRange = 20f..500f,
                        steps = 96,
                        modifier = Modifier.testTag("daily_rate_slider")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = totalDays.toString(),
                        onValueChange = { it.toIntOrNull()?.let { d -> viewModel.dailyTotalDays.value = d } },
                        label = { Text("Billing Days") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = leaves.toString(),
                        onValueChange = { it.toIntOrNull()?.let { l -> viewModel.dailyLeaves.value = l } },
                        label = { Text("Leave Days") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = extra.toInt().toString(),
                        onValueChange = { it.toDoubleOrNull()?.let { e -> viewModel.dailyExtra.value = e } },
                        label = { Text("Extra (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        // Schedule CTA
        Button(
            onClick = {
                val details = "${result.billableDays} days @ ${UpiHelper.formatInr(rate)}/d (${leaves} leaves deducted) + ${UpiHelper.formatInr(extra)} extra"
                onSchedulePayment(result.netPayable, "Daily Service Payment", CalculationType.DAILY_RATE, details, PaymentCategory.SERVICES_DOMESTIC)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("schedule_daily_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Schedule as Domestic Service Payment", fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------
// 4. Split Bill & Group Share Tab
// -------------------------------------------------------------
@Composable
fun SplitBillCalculatorTab(
    viewModel: PaymentViewModel,
    onSchedulePayment: (amount: Double, title: String, calcType: CalculationType, calcDetails: String, category: PaymentCategory) -> Unit
) {
    val amount by viewModel.splitBillAmount.collectAsStateWithLifecycle()
    val people by viewModel.splitPeople.collectAsStateWithLifecycle()
    val tip by viewModel.splitTipPercent.collectAsStateWithLifecycle()
    val extra by viewModel.splitExtraCharge.collectAsStateWithLifecycle()
    val result by viewModel.splitResult.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Result Hero
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Each Person's Calculated Share",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = UpiHelper.formatInr(result.perPersonShare),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Grand Total: ${UpiHelper.formatInr(result.grandTotal)} divided by $people people",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
            }
        }

        // Inputs Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = amount.toInt().toString(),
                    onValueChange = { it.toDoubleOrNull()?.let { a -> viewModel.splitBillAmount.value = a } },
                    label = { Text("Total Bill Amount (₹)") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Number of People", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("$people People", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = people.toFloat(),
                        onValueChange = { viewModel.splitPeople.value = it.toInt() },
                        valueRange = 2f..20f,
                        steps = 17,
                        modifier = Modifier.testTag("split_people_slider")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = tip.toInt().toString(),
                        onValueChange = { it.toDoubleOrNull()?.let { t -> viewModel.splitTipPercent.value = t } },
                        label = { Text("Tip / Service %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = extra.toInt().toString(),
                        onValueChange = { it.toDoubleOrNull()?.let { e -> viewModel.splitExtraCharge.value = e } },
                        label = { Text("Tax / Delivery (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        // Schedule CTA
        Button(
            onClick = {
                val details = "Total ${UpiHelper.formatInr(result.grandTotal)} split by $people people (${UpiHelper.formatInr(result.perPersonShare)} each)"
                onSchedulePayment(result.perPersonShare, "Split Bill Share", CalculationType.BILL_SPLIT, details, PaymentCategory.OTHER)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("schedule_split_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Schedule My Share as Upcoming UPI", fontWeight = FontWeight.Bold)
        }
    }
}

// -------------------------------------------------------------
// 5. Plan Proration & Recharge Calculator Tab
// -------------------------------------------------------------
@Composable
fun PlanProrationCalculatorTab(
    viewModel: PaymentViewModel,
    onSchedulePayment: (amount: Double, title: String, calcType: CalculationType, calcDetails: String, category: PaymentCategory) -> Unit
) {
    val cost by viewModel.prorateCost.collectAsStateWithLifecycle()
    val days by viewModel.prorateDays.collectAsStateWithLifecycle()
    val result by viewModel.prorateResult.collectAsStateWithLifecycle()

    val renewalDateStr = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault()).format(Date(result.nextRenewalTimestamp))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Result Hero
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Effective Monthly Cost",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
                Text(
                    text = UpiHelper.formatInr(result.effectiveMonthlyCost),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Effective Daily: ${UpiHelper.formatInr(result.effectiveDailyCost)}/day • Next Renewal: $renewalDateStr",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                )
            }
        }

        // Inputs Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = cost.toInt().toString(),
                    onValueChange = { it.toDoubleOrNull()?.let { c -> viewModel.prorateCost.value = c } },
                    label = { Text("Total Plan / Recharge Cost (₹)") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Plan Validity Duration", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("$days Days", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = days.toFloat(),
                        onValueChange = { viewModel.prorateDays.value = it.toInt() },
                        valueRange = 7f..365f,
                        steps = 50,
                        modifier = Modifier.testTag("prorate_days_slider")
                    )
                }

                // Quick validity presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(28, 56, 84, 180, 365).forEach { presetDays ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (days == presetDays) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.prorateDays.value = presetDays }
                        ) {
                            Text(
                                text = "${presetDays}d",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Schedule CTA
        Button(
            onClick = {
                val details = "${UpiHelper.formatInr(cost)} for ${days} days (${UpiHelper.formatInr(result.effectiveDailyCost)}/day, renews $renewalDateStr)"
                onSchedulePayment(cost, "Prepaid / Plan Renewal", CalculationType.PRORATED_PLAN, details, PaymentCategory.SUBSCRIPTION)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("schedule_prorate_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Schedule Next Renewal Autopay", fontWeight = FontWeight.Bold)
        }
    }
}
