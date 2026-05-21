package com.voicepay.alert.ui.settings

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.voicepay.alert.R
import com.voicepay.alert.VoicePayApplication
import com.voicepay.alert.data.repository.AnnouncementLanguage
import com.voicepay.alert.data.repository.SettingsRepository
import com.voicepay.alert.databinding.ActivitySettingsBinding
import com.voicepay.alert.ui.ViewModelFactory
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val app get() = application as VoicePayApplication
    private val viewModel: SettingsViewModel by viewModels { ViewModelFactory(app) }
    private var appToggleAdapter: AppToggleAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        setupLanguageSpinner()
        setupListeners()
        observeSettings()
    }

    private fun setupLanguageSpinner() {
        val languages = AnnouncementLanguage.entries.map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLanguage.adapter = adapter
    }

    private fun setupListeners() {
        binding.switchVoiceEnabled.setOnCheckedChangeListener { _, checked ->
            viewModel.setVoiceEnabled(checked)
        }
        binding.switchSpeakSender.setOnCheckedChangeListener { _, checked ->
            viewModel.setSpeakSenderName(checked)
        }
        binding.switchDarkMode.setOnCheckedChangeListener { _, checked ->
            viewModel.setDarkMode(checked)
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        binding.switchAutoStart.setOnCheckedChangeListener { _, checked ->
            viewModel.setAutoStartOnBoot(checked)
        }
        binding.switchKeepAlive.setOnCheckedChangeListener { _, checked ->
            viewModel.setKeepAliveService(checked)
        }
        binding.sliderVolume.addOnChangeListener { _, value, fromUser ->
            if (fromUser) viewModel.setVolume(value)
        }
        binding.btnTestVoice.setOnClickListener { viewModel.testVoice() }
        binding.btnEditTemplates.setOnClickListener { showTemplateDialog() }
        binding.spinnerLanguage.setOnItemClickListener { _, _, position, _ ->
            viewModel.setLanguage(AnnouncementLanguage.entries[position])
        }
    }

    private fun observeSettings() {
        viewModel.settings.observe(this) { settings ->
            binding.switchVoiceEnabled.isChecked = settings.voiceEnabled
            binding.switchSpeakSender.isChecked = settings.speakSenderName
            binding.switchDarkMode.isChecked = settings.darkMode
            binding.switchAutoStart.isChecked = settings.autoStartOnBoot
            binding.switchKeepAlive.isChecked = settings.keepAliveService
            binding.sliderVolume.value = settings.volume

            val langIndex = AnnouncementLanguage.entries.indexOf(settings.language)
            if (langIndex >= 0 && binding.spinnerLanguage.text.toString() != settings.language.displayName) {
                binding.spinnerLanguage.setText(settings.language.displayName, false)
            }

            val toggles = viewModel.getAppToggles(settings)
            if (appToggleAdapter == null) {
                appToggleAdapter = AppToggleAdapter(toggles) { pkg, enabled ->
                    viewModel.setAppEnabled(pkg, enabled)
                }
                binding.recyclerAppToggles.layoutManager = LinearLayoutManager(this)
                binding.recyclerAppToggles.adapter = appToggleAdapter
            } else {
                appToggleAdapter?.updateItems(toggles)
            }
        }
    }

    private fun showTemplateDialog() {
        val settings = viewModel.settings.value ?: return
        val withSenderInput = android.widget.EditText(this).apply {
            setText(settings.templateWithSender)
            hint = SettingsRepository.DEFAULT_TEMPLATE_WITH_SENDER
        }
        val withoutSenderInput = android.widget.EditText(this).apply {
            setText(settings.templateWithoutSender)
            hint = SettingsRepository.DEFAULT_TEMPLATE_WITHOUT_SENDER
        }
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(android.widget.TextView(this@SettingsActivity).apply {
                text = getString(R.string.template_with_sender_label)
            })
            addView(withSenderInput)
            addView(android.widget.TextView(this@SettingsActivity).apply {
                text = getString(R.string.template_without_sender_label)
            })
            addView(withoutSenderInput)
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.custom_templates)
            .setView(layout)
            .setPositiveButton(R.string.save) { _, _ ->
                viewModel.setTemplates(
                    withSenderInput.text.toString().ifBlank { SettingsRepository.DEFAULT_TEMPLATE_WITH_SENDER },
                    withoutSenderInput.text.toString().ifBlank { SettingsRepository.DEFAULT_TEMPLATE_WITHOUT_SENDER }
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
