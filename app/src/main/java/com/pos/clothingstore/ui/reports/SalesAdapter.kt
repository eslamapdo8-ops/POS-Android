package com.pos.clothingstore.ui.reports

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pos.clothingstore.data.local.entity.Sale
import com.pos.clothingstore.databinding.ItemSaleRowBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * محوّل قائمة المبيعات في شاشة التقارير
 */
class SalesAdapter : ListAdapter<Sale, SalesAdapter.ViewHolder>(SaleDiffCallback()) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale("ar"))

    inner class ViewHolder(val binding: ItemSaleRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSaleRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sale = getItem(position)
        with(holder.binding) {
            tvSaleId.text = "فاتورة #${sale.id}"
            tvSaleTime.text = timeFormat.format(Date(sale.date))
            tvSaleTotal.text = String.format("%.2f %s", sale.total, "ر.س")
            tvSaleMethod.text = if (sale.paymentMethod == "cash") "نقدي" else "بطاقة"
        }
    }

    class SaleDiffCallback : DiffUtil.ItemCallback<Sale>() {
        override fun areItemsTheSame(oldItem: Sale, newItem: Sale) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Sale, newItem: Sale) = oldItem == newItem
    }
}
