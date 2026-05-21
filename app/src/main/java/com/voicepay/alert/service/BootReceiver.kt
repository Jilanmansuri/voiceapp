package com.voicepay.alert.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.voicepay.alert.VoicePayApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Restarts keep-alive service after device boot when auto-start is enabled.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED &&
            intent?.action != "android.intent.action.QUICKBOOT_POWERON"
        ) {
            return
        }

        val app = context.applicationContext as VoicePayApplication
        val settings = runBlocking { app.settingsRepository.settingsFlow.first() }
        if (!settings.autoStartOnBoot) return

        app.ttsManager.initialize()
        ListenerKeepAliveService.startIfNeeded(context)
    }
}
