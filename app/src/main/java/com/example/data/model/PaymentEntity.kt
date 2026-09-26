package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val payeeUpiId: String = "",
    val payeeName: String = "",
    val amount: Double,
    val frequency: String = PaymentFrequency.MONTHLY.name,
    val category: String = PaymentCategory.BILLS_UTILITY.name,
    val calculationType: String = CalculationType.MANUAL.name,
    val calcDetails: String = "",
    val dueTimestamp: Long,
    val isAutopayEnabled: Boolean = false,
    val isPaid: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    val frequencyEnum: PaymentFrequency
        get() = try {
            PaymentFrequency.valueOf(frequency)
        } catch (_: Exception) {
            PaymentFrequency.MONTHLY
        }

    val categoryEnum: PaymentCategory
        get() = try {
            PaymentCategory.valueOf(category)
        } catch (_: Exception) {
            PaymentCategory.OTHER
        }

    val calcTypeEnum: CalculationType
        get() = try {
            CalculationType.valueOf(calculationType)
        } catch (_: Exception) {
            CalculationType.MANUAL
        }
}
