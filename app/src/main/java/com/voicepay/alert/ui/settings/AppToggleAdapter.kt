package com.voicepay.alert.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.voicepay.alert.data.repository.AppToggleItem
import com.voicepay.alert.databinding.ItemAppToggleBinding

class AppToggleAdapter(
    private var items: List<AppToggleItem>,
    private val onToggle: (packageName: String, enabled: Boolean) -> Unit
) : RecyclerView.Adapter<AppToggleAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemAppToggleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppToggleItem) {
            binding.tvAppName.text = item.displayName
            binding.switchAppEnabled.setOnCheckedChangeListener(null)
            binding.switchAppEnabled.isChecked = item.enabled
            binding.switchAppEnabled.setOnCheckedChangeListener { _, checked ->
                onToggle(item.packageName, checked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppToggleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<AppToggleItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
