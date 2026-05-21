package com.voicepay.alert.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.voicepay.alert.data.entity.PaymentNotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentNotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PaymentNotificationEntity): Long

    @Query("SELECT * FROM payment_notifications ORDER BY timestamp DESC")
    fun getAllFlow(): Flow<List<PaymentNotificationEntity>>

    @Query("SELECT * FROM payment_notifications ORDER BY timestamp DESC LIMIT 1")
    fun getLatestFlow(): Flow<PaymentNotificationEntity?>

    @Query("SELECT * FROM payment_notifications ORDER BY timestamp DESC")
    suspend fun getAll(): List<PaymentNotificationEntity>

    @Query("DELETE FROM payment_notifications")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM payment_notifications")
    suspend fun count(): Int
}
