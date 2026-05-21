package com.voicepay.alert.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.voicepay.alert.R
import com.voicepay.alert.VoicePayApplication
import com.voicepay.alert.databinding.ActivityMainBinding
import com.voicepay.alert.ui.ViewModelFactory
import com.voicepay.alert.ui.history.HistoryActivity
import com.voicepay.alert.ui.settings.SettingsActivity
import com.voicepay.alert.util.NotificationAccessHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val app get() = application as VoicePayApplication
    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory(app)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        runBlocking {
            val settings = app.settingsRepository.settingsFlow.first()
            AppCompatDelegate.setDefaultNightMode(
                if (settings.darkMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        requestNotificationPermissionIfNeeded()
        viewModel.refreshStatus(this)
        viewModel.startKeepAliveIfNeeded(this)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun setupClickListeners() {
        binding.btnNotificationAccess.setOnClickListener {
            NotificationAccessHelper.openNotificationAccessSettings(this)
        }
        binding.btnBatteryOptimization.setOnClickListener {
            if (!NotificationAccessHelper.isIgnoringBatteryOptimizations(this)) {
                NotificationAccessHelper.requestIgnoreBatteryOptimizations(this)
            } else {
                NotificationAccessHelper.openBatteryOptimizationSettings(this)
            }
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            binding.chipServiceStatus.text = if (state.notificationAccessEnabled) {
                getString(R.string.status_active)
            } else {
                getString(R.string.status_inactive)
            }
            binding.chipServiceStatus.setChipBackgroundColorResource(
                if (state.notificationAccessEnabled) R.color.status_active
                else R.color.status_inactive
            )

            binding.chipVoiceStatus.text = if (state.voiceEnabled) {
                if (state.isSpeaking) getString(R.string.status_speaking)
                else getString(R.string.status_voice_on)
            } else {
                getString(R.string.status_voice_off)
            }

            binding.chipBatteryStatus.text = if (state.batteryOptimizationIgnored) {
                getString(R.string.battery_unrestricted)
            } else {
                getString(R.string.battery_restricted)
            }

            val payment = state.latestPayment
            if (payment != null) {
                binding.cardLastPayment.visibility = android.view.View.VISIBLE
                binding.tvLastAmount.text = getString(R.string.amount_format, payment.amount)
                binding.tvLastSender.text = payment.sender?.let {
                    getString(R.string.sender_format, it)
                } ?: getString(R.string.sender_unknown)
                binding.tvLastApp.text = payment.appName
                binding.tvLastTime.text = formatTime(payment.timestamp)
            } else {
                binding.cardLastPayment.visibility = android.view.View.VISIBLE
                binding.tvLastAmount.text = getString(R.string.no_payments_yet)
                binding.tvLastSender.text = ""
                binding.tvLastApp.text = ""
                binding.tvLastTime.text = ""
            }
        }
    }

    private fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
