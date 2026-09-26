package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class PaymentCategory(
    val displayName: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    BILLS_UTILITY("Bills & Utility", Icons.Filled.Bolt, Color(0xFFEAB308)),
    SUBSCRIPTION("Subscriptions", Icons.Filled.Subscriptions, Color(0xFFEF4444)),
    EMI_LOAN("EMI & Loans", Icons.Filled.AccountBalance, Color(0xFF3B82F6)),
    RENT_HOUSING("Rent & Society", Icons.Filled.Home, Color(0xFF8B5CF6)),
    SERVICES_DOMESTIC("Daily Services", Icons.Filled.CleaningServices, Color(0xFF10B981)),
    SIP_INVESTMENT("SIP & Investment", Icons.Filled.TrendingUp, Color(0xFF14B8A6)),
    OTHER("Other Payments", Icons.Filled.Payment, Color(0xFF6B7280))
}

enum class PaymentFrequency(val displayName: String, val daysStep: Int) {
    ONCE("One-time", 0),
    DAILY("Daily", 1),
    WEEKLY("Weekly", 7),
    MONTHLY("Monthly", 30),
    QUARTERLY("Quarterly", 90),
    YEARLY("Yearly", 365)
}

enum class CalculationType(val displayName: String, val shortDesc: String) {
    MANUAL("Direct / Fixed", "Standard fixed amount"),
    EMI_LOAN("EMI Auto-Calc", "Principal, Rate %, Tenure"),
    UTILITY_SLABS("Utility Slabs", "Units consumed & tier rates"),
    DAILY_RATE("Daily Rate Calc", "Per-day rate × days − leaves"),
    BILL_SPLIT("Split Bill Share", "Total bill ÷ friends + tips"),
    PRORATED_PLAN("Plan Proration", "Cost prorated per day/month")
}
