package com.voicepay.alert.ui.history

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.voicepay.alert.R
import com.voicepay.alert.VoicePayApplication
import com.voicepay.alert.databinding.ActivityHistoryBinding
import com.voicepay.alert.ui.ViewModelFactory
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val app get() = application as VoicePayApplication
    private val viewModel: HistoryViewModel by viewModels { ViewModelFactory(app) }
    private lateinit var adapter: PaymentHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        adapter = PaymentHistoryAdapter()
        binding.recyclerHistory.layoutManager = LinearLayoutManager(this)
        binding.recyclerHistory.adapter = adapter

        binding.btnExport.setOnClickListener { exportHistory() }
        binding.btnClear.setOnClickListener { confirmClear() }

        viewModel.payments.observe(this) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
        }
    }

    private fun exportHistory() {
        lifecycleScope.launch {
            val text = viewModel.getExportText()
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_subject))
                putExtra(Intent.EXTRA_TEXT, text)
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.export_history)))
        }
    }

    private fun confirmClear() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.clear_history_title)
            .setMessage(R.string.clear_history_message)
            .setPositiveButton(R.string.clear) { _, _ ->
                viewModel.clearHistory()
                Snackbar.make(binding.root, R.string.history_cleared, Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
