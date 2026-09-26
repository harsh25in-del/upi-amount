package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments ORDER BY isPaid ASC, dueTimestamp ASC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE id = :id LIMIT 1")
    fun getPaymentById(id: Long): Flow<PaymentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePaymentById(id: Long)

    @Query("UPDATE payments SET isPaid = :isPaid WHERE id = :id")
    suspend fun setPaymentPaidStatus(id: Long, isPaid: Boolean)

    @Query("UPDATE payments SET dueTimestamp = :nextDue, isPaid = :isPaid WHERE id = :id")
    suspend fun advanceDueDate(id: Long, nextDue: Long, isPaid: Boolean)

    @Query("SELECT COUNT(*) FROM payments")
    suspend fun getCount(): Int
}
