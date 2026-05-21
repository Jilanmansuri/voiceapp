package com.voicepay.alert.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.voicepay.alert.VoicePayApplication
import com.voicepay.alert.ui.history.HistoryViewModel
import com.voicepay.alert.ui.main.MainViewModel
import com.voicepay.alert.ui.settings.SettingsViewModel

class ViewModelFactory(
    private val application: VoicePayApplication
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) ->
                MainViewModel(application) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(application) as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(application) as T
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
