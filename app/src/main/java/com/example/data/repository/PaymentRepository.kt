package com.example.data.repository

import com.example.data.db.PaymentDao
import com.example.data.db.PaymentHistoryDao
import com.example.data.model.PaymentEntity
import com.example.data.model.PaymentFrequency
import com.example.data.model.PaymentHistoryEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class PaymentRepository(
    private val paymentDao: PaymentDao,
    private val historyDao: PaymentHistoryDao
) {
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()
    val allHistory: Flow<List<PaymentHistoryEntity>> = historyDao.getAllHistory()

    fun getPaymentById(id: Long): Flow<PaymentEntity?> = paymentDao.getPaymentById(id)

    suspend fun insertOrUpdatePayment(payment: PaymentEntity): Long {
        return paymentDao.insertPayment(payment)
    }

    suspend fun deletePayment(id: Long) {
        paymentDao.deletePaymentById(id)
    }

    suspend fun markPaymentAsPaid(payment: PaymentEntity, upiRef: String = "") {
        // Record in history
        historyDao.insertHistory(
            PaymentHistoryEntity(
                paymentId = payment.id,
                title = payment.title,
                amountPaid = payment.amount,
                paidTimestamp = System.currentTimeMillis(),
                upiReference = if (upiRef.isBlank()) "UPI-REC-${System.currentTimeMillis() % 1000000}" else upiRef,
                category = payment.category
            )
        )

        val freq = payment.frequencyEnum
        if (freq == PaymentFrequency.ONCE) {
            // Mark as paid
            paymentDao.setPaymentPaidStatus(payment.id, true)
        } else {
            // Advance to next cycle based on frequency!
            val nextCal = Calendar.getInstance().apply {
                timeInMillis = payment.dueTimestamp
            }
            when (freq) {
                PaymentFrequency.DAILY -> nextCal.add(Calendar.DAY_OF_YEAR, 1)
                PaymentFrequency.WEEKLY -> nextCal.add(Calendar.DAY_OF_YEAR, 7)
                PaymentFrequency.MONTHLY -> nextCal.add(Calendar.MONTH, 1)
                PaymentFrequency.QUARTERLY -> nextCal.add(Calendar.MONTH, 3)
                PaymentFrequency.YEARLY -> nextCal.add(Calendar.YEAR, 1)
                PaymentFrequency.ONCE -> {}
            }
            // If the advanced date is still in the past, roll forward to future
            val now = System.currentTimeMillis()
            while (nextCal.timeInMillis <= now) {
                when (freq) {
                    PaymentFrequency.DAILY -> nextCal.add(Calendar.DAY_OF_YEAR, 1)
                    PaymentFrequency.WEEKLY -> nextCal.add(Calendar.DAY_OF_YEAR, 7)
                    PaymentFrequency.MONTHLY -> nextCal.add(Calendar.MONTH, 1)
                    PaymentFrequency.QUARTERLY -> nextCal.add(Calendar.MONTH, 3)
                    PaymentFrequency.YEARLY -> nextCal.add(Calendar.YEAR, 1)
                    PaymentFrequency.ONCE -> break
                }
            }
            paymentDao.advanceDueDate(payment.id, nextCal.timeInMillis, isPaid = false)
        }
    }

    suspend fun unmarkPaid(paymentId: Long) {
        paymentDao.setPaymentPaidStatus(paymentId, false)
    }

    suspend fun toggleAutopay(payment: PaymentEntity) {
        paymentDao.updatePayment(payment.copy(isAutopayEnabled = !payment.isAutopayEnabled))
    }

    suspend fun deleteHistory(id: Long) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        historyDao.clearAllHistory()
    }
}
