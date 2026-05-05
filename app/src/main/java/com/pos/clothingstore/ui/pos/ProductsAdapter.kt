package com.pos.clothingstore.ui.pos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pos.clothingstore.data.local.entity.Product
import com.pos.clothingstore.databinding.ItemProductCardBinding

/**
 * محوّل شبكة المنتجات في شاشة البيع
 */
class ProductsAdapter(
    private val onProductClick: (Product) -> Unit
) : ListAdapter<Product, ProductsAdapter.ViewHolder>(ProductDiffCallback()) {

    inner class ViewHolder(val binding: ItemProductCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = getItem(position)
        with(holder.binding) {
            tvName.text = product.name
            tvBarcode.text = product.barcode
            tvPrice.text = String.format("%.2f %s", product.price, "ر.س")
            tvQuantity.text = "الكمية: ${product.quantity}"

            if (product.quantity <= 0) {
                tvQuantity.setTextColor(tvQuantity.context.getColor(android.R.color.holo_red_dark))
            }

            cardRoot.setOnClickListener { onProductClick(product) }
        }
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Product, newItem: Product) = oldItem == newItem
    }
}
