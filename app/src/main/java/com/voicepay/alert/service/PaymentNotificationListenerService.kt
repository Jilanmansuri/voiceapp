package com.voicepay.alert.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.voicepay.alert.VoicePayApplication
import com.voicepay.alert.util.PaymentApps
import com.voicepay.alert.util.PaymentParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Listens for payment app notifications and triggers voice announcements.
 */
class PaymentNotificationListenerService : NotificationListenerService() {

    companion object {
        private const val TAG = "PaymentListener"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val app: VoicePayApplication
        get() = application as VoicePayApplication

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "Notification listener connected")
        ListenerKeepAliveService.startIfNeeded(this)
        ListenerKeepAliveService.updateNotification(this, true)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.w(TAG, "Notification listener disconnected")
        ListenerKeepAliveService.updateNotification(this, false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        val packageName = sbn.packageName ?: return

        if (packageName !in PaymentApps.SUPPORTED_PACKAGES) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString()
        val text = extras.getCharSequence("android.text")?.toString()
            ?: extras.getCharSequence("android.bigText")?.toString()

        if (!PaymentParser.isPaymentNotification(title, text)) return

        scope.launch {
            try {
                val settings = app.settingsRepository.settingsFlow.first()
                if (!app.settingsRepository.isAppEnabled(settings, packageName)) {
                    Log.d(TAG, "App disabled in settings: $packageName")
                    return@launch
                }

                val entity = app.paymentRepository.saveAndReturn(packageName, title, text)
                if (entity != null) {
                    Log.i(TAG, "Payment detected: ${entity.amount} from ${entity.sender}")
                    app.ttsManager.announcePayment(entity)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // No action needed
    }
}
