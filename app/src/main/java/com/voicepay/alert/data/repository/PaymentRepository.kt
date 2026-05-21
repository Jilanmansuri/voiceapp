package com.voicepay.alert.data.repository

import com.voicepay.alert.data.dao.PaymentNotificationDao
import com.voicepay.alert.data.entity.PaymentNotificationEntity
import com.voicepay.alert.util.PaymentApps
import com.voicepay.alert.util.PaymentParser
import kotlinx.coroutines.flow.Flow

class PaymentRepository(
    private val dao: PaymentNotificationDao
) {

    val allPayments: Flow<List<PaymentNotificationEntity>> = dao.getAllFlow()
    val latestPayment: Flow<PaymentNotificationEntity?> = dao.getLatestFlow()

    suspend fun saveAndReturn(
        packageName: String,
        title: String?,
        text: String?
    ): PaymentNotificationEntity? {
        val parsed = PaymentParser.parse(title, text) ?: return null
        val entity = PaymentNotificationEntity(
            amount = parsed.amount,
            sender = parsed.sender,
            appName = PaymentApps.displayName(packageName),
            packageName = packageName,
            rawMessage = parsed.rawText
        )
        dao.insert(entity)
        return entity
    }

    suspend fun getAllForExport(): List<PaymentNotificationEntity> = dao.getAll()

    suspend fun clearHistory() {
        dao.clearAll()
    }
}
