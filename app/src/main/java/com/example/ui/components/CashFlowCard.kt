package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.PaymentSummaryMetrics
import com.example.util.UpiHelper

@Composable
fun CashFlowCard(
    metrics: PaymentSummaryMetrics,
    bankBalance: Double,
    onEditBalance: () -> Unit,
    onViewForecast: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDeficit = metrics.safeBalanceAfter30Days < 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("cash_flow_hero_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F264A),
                            Color(0xFF0B192E)
                        )
                    )
                )
                .border(
                    1.dp,
                    Color(0xFF1E3A8A).copy(alpha = 0.6f),
                    RoundedCornerShape(22.dp)
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header: Bank Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7).copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Current Bank Balance",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = UpiHelper.formatInr(bankBalance),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Edit Balance Pill
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E293B),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onEditBalance)
                            .testTag("edit_balance_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Edit Balance",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Adjust",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF38BDF8)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Stats Grid: 7-Day Due, 30-Day Due, Safe Remaining
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Next 7 Days
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Next 7 Days",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = UpiHelper.formatInr(metrics.totalUpcoming7Days),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                        Text(
                            text = "${metrics.count7Days} upcoming",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Next 30 Days
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Total Monthly",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = UpiHelper.formatInr(metrics.totalUpcoming30Days),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24)
                        )
                        Text(
                            text = "Autopay: ${UpiHelper.formatInr(metrics.monthlyAutopayTotal)}",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Safe to Spend
                    Column(modifier = Modifier.weight(1.1f)) {
                        Text(
                            text = "Safe to Spend",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = UpiHelper.formatInr(metrics.safeBalanceAfter30Days),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDeficit) Color(0xFFEF4444) else Color(0xFF34D399)
                        )
                        Text(
                            text = if (isDeficit) "⚠️ Deficit alert!" else "After 30d debits",
                            fontSize = 10.sp,
                            fontWeight = if (isDeficit) FontWeight.Bold else FontWeight.Normal,
                            color = if (isDeficit) Color(0xFFEF4444) else Color(0xFF64748B)
                        )
                    }
                }

                // Deficit Alert Banner if applicable
                if (isDeficit) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEF4444).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.WarningAmber,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Upcoming UPI debits exceed your current balance by ${UpiHelper.formatInr(-metrics.safeBalanceAfter30Days)}. Add funds to avoid autopay bounce.",
                                fontSize = 11.sp,
                                color = Color(0xFFFCA5A5)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Forecast CTA Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onViewForecast)
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View 30-Day Day-by-Day Forecast",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF38BDF8)
                    )
                    Icon(
                        Icons.Filled.ArrowForward,
                        contentDescription = "View forecast",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
