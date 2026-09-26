package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_history")
data class PaymentHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val paymentId: Long,
    val title: String,
    val amountPaid: Double,
    val paidTimestamp: Long = System.currentTimeMillis(),
    val upiReference: String = "",
    val category: String = PaymentCategory.BILLS_UTILITY.name
) {
    val categoryEnum: PaymentCategory
        get() = try {
            PaymentCategory.valueOf(category)
        } catch (_: Exception) {
            PaymentCategory.OTHER
        }
}
