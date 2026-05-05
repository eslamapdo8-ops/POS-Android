package com.pos.clothingstore.ui.pos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pos.clothingstore.databinding.ItemCartItemBinding

/**
 * عنصر في سلة المشتريات
 */
data class CartItem(
    val product: com.pos.clothingstore.data.local.entity.Product,
    var quantity: Int
) {
    val subtotal: Double get() = product.price * quantity
}

/**
 * محوّل سلة المشتريات
 */
class CartAdapter(
    private val onQuantityChanged: () -> Unit,
    private val onItemRemoved: (Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    private val items = mutableListOf<CartItem>()

    inner class ViewHolder(val binding: ItemCartItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvItemName.text = item.product.name
            tvItemPrice.text = String.format("%.2f %s", item.product.price, "ر.س")
            tvItemQty.text = item.quantity.toString()

            btnPlus.setOnClickListener {
                if (item.quantity < item.product.quantity) {
                    item.quantity++
                    tvItemQty.text = item.quantity.toString()
                    onQuantityChanged()
                }
            }

            btnMinus.setOnClickListener {
                if (item.quantity > 1) {
                    item.quantity--
                    tvItemQty.text = item.quantity.toString()
                    onQuantityChanged()
                }
            }
        }
    }

    override fun getItemCount() = items.size

    fun getItems(): List<CartItem> = items.toList()

    fun addItem(product: com.pos.clothingstore.data.local.entity.Product) {
        val existing = items.find { it.product.barcode == product.barcode }
        if (existing != null) {
            if (existing.quantity < product.quantity) {
                existing.quantity++
            }
        } else {
            items.add(CartItem(product, 1))
        }
        notifyDataSetChanged()
        onQuantityChanged()
    }

    fun removeItem(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyDataSetChanged()
            onQuantityChanged()
        }
    }

    fun clearCart() {
        items.clear()
        notifyDataSetChanged()
        onQuantityChanged()
    }

    fun getTotal(): Double = items.sumOf { it.subtotal }

    fun isEmpty(): Boolean = items.isEmpty()
}
