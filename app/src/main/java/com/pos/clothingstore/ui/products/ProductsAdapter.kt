package com.pos.clothingstore.ui.products

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pos.clothingstore.data.local.entity.Product
import com.pos.clothingstore.databinding.ItemProductRowBinding

/**
 * محوّل قائمة المنتجات في شاشة الإدارة
 */
class ProductsAdapter(
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : ListAdapter<Product, ProductsAdapter.ViewHolder>(ProductDiffCallback()) {

    inner class ViewHolder(val binding: ItemProductRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = getItem(position)
        with(holder.binding) {
            tvName.text = product.name
            tvBarcode.text = product.barcode
            tvPrice.text = String.format("%.2f %s", product.price, "ر.س")
            tvQuantity.text = product.quantity.toString()

            if (product.quantity <= 5) {
                tvQuantity.setTextColor(tvQuantity.context.getColor(android.R.color.holo_orange_dark))
            }

            btnEdit.setOnClickListener { onEditClick(product) }
            btnDelete.setOnClickListener { onDeleteClick(product) }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
    }
}
