package com.voicepay.alert.tts

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.voicepay.alert.data.entity.PaymentNotificationEntity
import com.voicepay.alert.data.repository.AnnouncementLanguage
import com.voicepay.alert.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages Text-to-Speech announcements with multi-language support.
 */
class TtsManager(
    private val context: Context,
    private val settingsRepository: SettingsRepository
) : TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "TtsManager"
        private const val UTTERANCE_ID = "voicepay_payment"
    }

    private var tts: TextToSpeech? = null
    private val isReady = AtomicBoolean(false)
    private var pendingAnnouncement: String? = null

    fun initialize() {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isReady.set(true)
            applyLanguage(runBlocking { settingsRepository.settingsFlow.first() }.language)
            pendingAnnouncement?.let { speakInternal(it) }
            pendingAnnouncement = null
        } else {
            Log.e(TAG, "TTS initialization failed: $status")
        }
    }

    fun announcePayment(entity: PaymentNotificationEntity) {
        val settings = runBlocking { settingsRepository.settingsFlow.first() }
        if (!settings.voiceEnabled) return

        applyLanguage(settings.language)
        val message = buildAnnouncement(entity, settings)
        speak(message, settings.volume)
    }

    fun speakTestMessage() {
        val settings = runBlocking { settingsRepository.settingsFlow.first() }
        applyLanguage(settings.language)
        val sample = when (settings.language) {
            AnnouncementLanguage.HINDI -> "पांच सौ रुपये प्राप्त हुए"
            AnnouncementLanguage.GUJARATI -> "પાંચ સો રૂપિયા મળ્યા"
            AnnouncementLanguage.ENGLISH -> "Received 500 rupees. VoicePay Alert is working."
        }
        speak(sample, settings.volume)
    }

    fun buildAnnouncement(
        entity: PaymentNotificationEntity,
        settings: com.voicepay.alert.data.repository.AppSettings
    ): String {
        val amount = entity.amount
        val sender = entity.sender

        val useSender = settings.speakSenderName && !sender.isNullOrBlank()
        val template = if (useSender) {
            settings.templateWithSender
        } else {
            settings.templateWithoutSender
        }

        var text = template
            .replace("{amount}", amount)
            .replace("{sender}", sender.orEmpty())
            .replace("{app}", entity.appName)

        if (!useSender) {
            text = localizeWithoutSender(text, amount, settings.language)
        } else {
            text = localizeWithSender(text, sender!!, amount, settings.language)
        }

        return text.trim()
    }

    private fun localizeWithoutSender(
        englishText: String,
        amount: String,
        language: AnnouncementLanguage
    ): String {
        return when (language) {
            AnnouncementLanguage.HINDI -> "$amount रुपये प्राप्त हुए"
            AnnouncementLanguage.GUJARATI -> "$amount રૂપિયા મળ્યા"
            AnnouncementLanguage.ENGLISH -> englishText.ifBlank {
                "Received $amount rupees"
            }
        }
    }

    private fun localizeWithSender(
        englishText: String,
        sender: String,
        amount: String,
        language: AnnouncementLanguage
    ): String {
        return when (language) {
            AnnouncementLanguage.HINDI -> "$sender ने $amount रुपये भेजे"
            AnnouncementLanguage.GUJARATI -> "$sender એ $amount રૂપિયા મોકલ્યા"
            AnnouncementLanguage.ENGLISH -> englishText.ifBlank {
                "$sender sent $amount rupees"
            }
        }
    }

    private fun applyLanguage(language: AnnouncementLanguage) {
        val engine = tts ?: return
        val locale = when (language) {
            AnnouncementLanguage.HINDI -> Locale("hi", "IN")
            AnnouncementLanguage.GUJARATI -> Locale("gu", "IN")
            AnnouncementLanguage.ENGLISH -> Locale.ENGLISH
        }
        val result = engine.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Language ${language.code} not fully supported, falling back to English")
            engine.setLanguage(Locale.ENGLISH)
        }
    }

    fun speak(text: String, volume: Float) {
        if (!isReady.get()) {
            pendingAnnouncement = text
            initialize()
            return
        }
        speakInternal(text, volume)
    }

    private fun speakInternal(text: String, volume: Float = 1f) {
        val engine = tts ?: return
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val target = (maxVolume * volume).toInt().coerceAtLeast(1)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)

            val params = Bundle().apply {
                putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume.coerceIn(0.1f, 1f))
            }
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, params, UTTERANCE_ID)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to speak", e)
        }
    }

    fun setOnSpeakingListener(listener: (Boolean) -> Unit) {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                listener(true)
            }

            override fun onDone(utteranceId: String?) {
                listener(false)
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                listener(false)
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                listener(false)
            }
        })
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady.set(false)
    }
}
