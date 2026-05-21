package com.voicepay.alert.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.voicepay.alert.VoicePayApplication
import com.voicepay.alert.data.entity.PaymentNotificationEntity
import com.voicepay.alert.data.repository.AppSettings
import com.voicepay.alert.service.ListenerKeepAliveService
import com.voicepay.alert.util.NotificationAccessHelper
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class MainUiState(
    val notificationAccessEnabled: Boolean = false,
    val batteryOptimizationIgnored: Boolean = false,
    val voiceEnabled: Boolean = true,
    val latestPayment: PaymentNotificationEntity? = null,
    val isSpeaking: Boolean = false
)

class MainViewModel(application: VoicePayApplication) : ViewModel() {

    private val settingsRepository = application.settingsRepository
    private val paymentRepository = application.paymentRepository
    private val ttsManager = application.ttsManager

    private val _accessEnabled = MutableLiveData(false)
    private val _batteryIgnored = MutableLiveData(false)
    private val _isSpeaking = MutableLiveData(false)

    val uiState: LiveData<MainUiState> = combine(
        settingsRepository.settingsFlow,
        paymentRepository.latestPayment
    ) { settings, latest ->
        MainUiState(
            notificationAccessEnabled = _accessEnabled.value ?: false,
            batteryOptimizationIgnored = _batteryIgnored.value ?: false,
            voiceEnabled = settings.voiceEnabled,
            latestPayment = latest,
            isSpeaking = _isSpeaking.value ?: false
        )
    }.asLiveData()

    val settings: LiveData<AppSettings> = settingsRepository.settingsFlow.asLiveData()

    init {
        ttsManager.setOnSpeakingListener { speaking ->
            _isSpeaking.postValue(speaking)
        }
    }

    fun refreshStatus(context: Context) {
        _accessEnabled.value = NotificationAccessHelper.isNotificationListenerEnabled(context)
        _batteryIgnored.value = NotificationAccessHelper.isIgnoringBatteryOptimizations(context)
    }

    fun startKeepAliveIfNeeded(context: Context) {
        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            if (settings.keepAliveService) {
                ListenerKeepAliveService.startIfNeeded(context)
            }
        }
    }
}
