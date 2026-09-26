package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PaymentHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentHistoryDao {
    @Query("SELECT * FROM payment_history ORDER BY paidTimestamp DESC")
    fun getAllHistory(): Flow<List<PaymentHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: PaymentHistoryEntity): Long

    @Query("DELETE FROM payment_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM payment_history")
    suspend fun clearAllHistory()
}
