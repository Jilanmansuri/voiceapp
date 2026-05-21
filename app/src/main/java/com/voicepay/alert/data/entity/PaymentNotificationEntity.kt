package com.voicepay.alert.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payment_notifications")
data class PaymentNotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: String,
    val sender: String?,
    val appName: String,
    val packageName: String,
    val rawMessage: String,
    val timestamp: Long = System.currentTimeMillis()
)
