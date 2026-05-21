package com.voicepay.alert.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.voicepay.alert.util.PaymentApps
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "voicepay_settings")

enum class AnnouncementLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    HINDI("hi", "Hindi"),
    GUJARATI("gu", "Gujarati")
}

class SettingsRepository(private val context: Context) {

    companion object {
        private val KEY_VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_VOLUME = floatPreferencesKey("volume")
        private val KEY_SPEAK_SENDER = booleanPreferencesKey("speak_sender")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_AUTO_START = booleanPreferencesKey("auto_start")
        private val KEY_KEEP_ALIVE = booleanPreferencesKey("keep_alive")
        private val KEY_TEMPLATE_WITH_SENDER = stringPreferencesKey("template_with_sender")
        private val KEY_TEMPLATE_WITHOUT_SENDER = stringPreferencesKey("template_without_sender")
        private val KEY_DISABLED_APPS = stringSetPreferencesKey("disabled_apps")

        const val DEFAULT_TEMPLATE_WITH_SENDER = "{sender} sent {amount} rupees"
        const val DEFAULT_TEMPLATE_WITHOUT_SENDER = "Received {amount} rupees"
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            voiceEnabled = prefs[KEY_VOICE_ENABLED] ?: true,
            language = AnnouncementLanguage.entries.find {
                it.code == prefs[KEY_LANGUAGE]
            } ?: AnnouncementLanguage.ENGLISH,
            volume = prefs[KEY_VOLUME] ?: 1.0f,
            speakSenderName = prefs[KEY_SPEAK_SENDER] ?: true,
            darkMode = prefs[KEY_DARK_MODE] ?: false,
            autoStartOnBoot = prefs[KEY_AUTO_START] ?: true,
            keepAliveService = prefs[KEY_KEEP_ALIVE] ?: true,
            templateWithSender = prefs[KEY_TEMPLATE_WITH_SENDER] ?: DEFAULT_TEMPLATE_WITH_SENDER,
            templateWithoutSender = prefs[KEY_TEMPLATE_WITHOUT_SENDER] ?: DEFAULT_TEMPLATE_WITHOUT_SENDER,
            disabledAppPackages = prefs[KEY_DISABLED_APPS] ?: emptySet()
        )
    }

    fun getSettingsBlocking(): AppSettings = runBlocking {
        settingsFlow.first()
    }

    suspend fun setVoiceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_VOICE_ENABLED] = enabled }
    }

    suspend fun setLanguage(language: AnnouncementLanguage) {
        context.dataStore.edit { it[KEY_LANGUAGE] = language.code }
    }

    suspend fun setVolume(volume: Float) {
        context.dataStore.edit { it[KEY_VOLUME] = volume.coerceIn(0f, 1f) }
    }

    suspend fun setSpeakSenderName(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SPEAK_SENDER] = enabled }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    suspend fun setAutoStartOnBoot(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_START] = enabled }
    }

    suspend fun setKeepAliveService(enabled: Boolean) {
        context.dataStore.edit { it[KEY_KEEP_ALIVE] = enabled }
    }

    suspend fun setTemplates(withSender: String, withoutSender: String) {
        context.dataStore.edit {
            it[KEY_TEMPLATE_WITH_SENDER] = withSender
            it[KEY_TEMPLATE_WITHOUT_SENDER] = withoutSender
        }
    }

    suspend fun setAppEnabled(packageName: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_DISABLED_APPS]?.toMutableSet() ?: mutableSetOf()
            if (enabled) {
                current.remove(packageName)
            } else {
                current.add(packageName)
            }
            prefs[KEY_DISABLED_APPS] = current
        }
    }

    fun isAppEnabled(settings: AppSettings, packageName: String): Boolean {
        return packageName !in settings.disabledAppPackages
    }

    fun supportedAppsWithState(settings: AppSettings): List<AppToggleItem> {
        return PaymentApps.SUPPORTED_PACKAGES.map { (pkg, name) ->
            AppToggleItem(pkg, name, isAppEnabled(settings, pkg))
        }
    }
}

data class AppSettings(
    val voiceEnabled: Boolean,
    val language: AnnouncementLanguage,
    val volume: Float,
    val speakSenderName: Boolean,
    val darkMode: Boolean,
    val autoStartOnBoot: Boolean,
    val keepAliveService: Boolean,
    val templateWithSender: String,
    val templateWithoutSender: String,
    val disabledAppPackages: Set<String>
)

data class AppToggleItem(
    val packageName: String,
    val displayName: String,
    val enabled: Boolean
)
