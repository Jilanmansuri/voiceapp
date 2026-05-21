package com.voicepay.alert.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.voicepay.alert.data.entity.PaymentNotificationEntity
import com.voicepay.alert.databinding.ItemPaymentHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentHistoryAdapter :
    ListAdapter<PaymentNotificationEntity, PaymentHistoryAdapter.ViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    inner class ViewHolder(private val binding: ItemPaymentHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PaymentNotificationEntity) {
            binding.tvAmount.text = "₹${item.amount}"
            binding.tvSender.text = item.sender ?: binding.root.context.getString(
                com.voicepay.alert.R.string.sender_unknown
            )
            binding.tvAppName.text = item.appName
            binding.tvTimestamp.text = dateFormat.format(Date(item.timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPaymentHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<PaymentNotificationEntity>() {
        override fun areItemsTheSame(
            old: PaymentNotificationEntity,
            new: PaymentNotificationEntity
        ) = old.id == new.id

        override fun areContentsTheSame(
            old: PaymentNotificationEntity,
            new: PaymentNotificationEntity
        ) = old == new
    }
}
