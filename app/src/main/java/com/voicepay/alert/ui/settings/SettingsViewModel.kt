package com.voicepay.alert.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.voicepay.alert.VoicePayApplication
import com.voicepay.alert.data.repository.AnnouncementLanguage
import com.voicepay.alert.data.repository.AppSettings
import com.voicepay.alert.data.repository.AppToggleItem
import kotlinx.coroutines.launch

class SettingsViewModel(application: VoicePayApplication) : ViewModel() {

    private val settingsRepository = application.settingsRepository
    private val ttsManager = application.ttsManager

    val settings: LiveData<AppSettings> = settingsRepository.settingsFlow.asLiveData()

    fun setVoiceEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setVoiceEnabled(enabled) }
    }

    fun setLanguage(language: AnnouncementLanguage) {
        viewModelScope.launch { settingsRepository.setLanguage(language) }
    }

    fun setVolume(volume: Float) {
        viewModelScope.launch { settingsRepository.setVolume(volume) }
    }

    fun setSpeakSenderName(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSpeakSenderName(enabled) }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDarkMode(enabled) }
    }

    fun setAutoStartOnBoot(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoStartOnBoot(enabled) }
    }

    fun setKeepAliveService(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setKeepAliveService(enabled) }
    }

    fun setTemplates(withSender: String, withoutSender: String) {
        viewModelScope.launch {
            settingsRepository.setTemplates(withSender, withoutSender)
        }
    }

    fun setAppEnabled(packageName: String, enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAppEnabled(packageName, enabled) }
    }

    fun getAppToggles(settings: AppSettings): List<AppToggleItem> {
        return settingsRepository.supportedAppsWithState(settings)
    }

    fun testVoice() {
        ttsManager.speakTestMessage()
    }
}
