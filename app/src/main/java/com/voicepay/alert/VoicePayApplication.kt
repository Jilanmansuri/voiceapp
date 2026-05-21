package com.voicepay.alert

import android.app.Application
import com.voicepay.alert.data.database.AppDatabase
import com.voicepay.alert.data.repository.PaymentRepository
import com.voicepay.alert.data.repository.SettingsRepository
import com.voicepay.alert.tts.TtsManager

/**
 * Application entry point. Provides singletons for database, repositories, and TTS.
 */
class VoicePayApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(applicationContext)
    }

    val paymentRepository: PaymentRepository by lazy {
        PaymentRepository(database.paymentNotificationDao())
    }

    val ttsManager: TtsManager by lazy {
        TtsManager(applicationContext, settingsRepository)
    }

    override fun onCreate() {
        super.onCreate()
        ttsManager.initialize()
    }
}
