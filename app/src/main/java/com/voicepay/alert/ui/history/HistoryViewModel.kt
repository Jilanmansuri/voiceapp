package com.voicepay.alert.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.voicepay.alert.VoicePayApplication
import com.voicepay.alert.data.entity.PaymentNotificationEntity
import kotlinx.coroutines.launch

class HistoryViewModel(application: VoicePayApplication) : ViewModel() {

    private val paymentRepository = application.paymentRepository

    val payments: LiveData<List<PaymentNotificationEntity>> =
        paymentRepository.allPayments.asLiveData()

    fun clearHistory() {
        viewModelScope.launch { paymentRepository.clearHistory() }
    }

    suspend fun getExportText(): String {
        val items = paymentRepository.getAllForExport()
        val header = "VoicePay Alert - Payment History Export\n\n"
        val body = items.joinToString("\n") { item ->
            "${item.timestampFormatted()} | ${item.appName} | ₹${item.amount} | ${item.sender ?: "Unknown"}"
        }
        return header + if (body.isEmpty()) "No payments recorded." else body
    }
}

private fun PaymentNotificationEntity.timestampFormatted(): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy HH:mm:ss", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
