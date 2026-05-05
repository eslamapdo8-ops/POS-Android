package com.pos.clothingstore.ui.products

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pos.clothingstore.POSApplication
import com.pos.clothingstore.R
import com.pos.clothingstore.data.local.entity.Product
import com.pos.clothingstore.databinding.DialogProductBinding
import com.pos.clothingstore.databinding.FragmentProductsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * شاشة إدارة المنتجات - إضافة وتعديل وحذف
 */
class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProductsAdapter
    private val db by lazy { POSApplication.getInstance().database }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProductsAdapter(
            onEditClick = { product -> showProductDialog(product) },
            onDeleteClick = { product -> confirmDelete(product) }
        )
        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProducts.adapter = adapter

        binding.fabAdd.setOnClickListener { showProductDialog(null) }

        setupSearch()
        observeProducts()
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                lifecycleScope.launch(Dispatchers.IO) {
                    val results = db.productDao().getAllProductsOnce().filter {
                        it.name.contains(query, ignoreCase = true) || it.barcode.contains(query, ignoreCase = true)
                    }
                    launch(Dispatchers.Main) { adapter.submitList(results) }
                }
            }
        })
    }

    private fun observeProducts() {
        db.productDao().getAllProducts().observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
        }
    }

    /**
     * عرض نافذة إضافة/تعديل منتج
     */
    private fun showProductDialog(existing: Product?) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_product, null)
        val dialogBinding = DialogProductBinding.bind(dialogView)

        val isEdit = existing != null
        dialogBinding.tvDialogTitle.text = if (isEdit) getString(R.string.edit_product_title) else getString(R.string.add_product_title)

        if (isEdit) {
            dialogBinding.etBarcode.setText(existing!!.barcode)
            dialogBinding.etName.setText(existing.name)
            dialogBinding.etPrice.setText(existing.price.toString())
            dialogBinding.etQuantity.setText(existing.quantity.toString())
            dialogBinding.etSupplier.setText(existing.supplier ?: "")
            dialogBinding.etBarcode.isEnabled = false // لا يمكن تغيير الباركود
        }

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .show()
            .let { dialog ->
                dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
                dialogBinding.btnSave.setOnClickListener {
                    val barcode = dialogBinding.etBarcode.text.toString().trim()
                    val name = dialogBinding.etName.text.toString().trim()
                    val priceStr = dialogBinding.etPrice.text.toString().trim()
                    val qtyStr = dialogBinding.etQuantity.text.toString().trim()
                    val supplier = dialogBinding.etSupplier.text.toString().trim()

                    // التحقق من المدخلات
                    if (barcode.isEmpty() || name.isEmpty() || priceStr.isEmpty() || qtyStr.isEmpty()) {
                        Toast.makeText(requireContext(), getString(R.string.required_field), Toast.LENGTH_SHORT).show()
                        return@let
                    }

                    val price = priceStr.toDoubleOrNull()
                    val qty = qtyStr.toIntOrNull()

                    if (price == null || price <= 0) {
                        Toast.makeText(requireContext(), getString(R.string.invalid_price), Toast.LENGTH_SHORT).show()
                        return@let
                    }

                    if (qty == null || qty < 0) {
                        Toast.makeText(requireContext(), getString(R.string.invalid_quantity), Toast.LENGTH_SHORT).show()
                        return@let
                    }

                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            if (isEdit) {
                                val updated = existing!!.copy(
                                    name = name, price = price, quantity = qty, supplier = supplier.ifBlank { null }
                                )
                                db.productDao().update(updated)
                            } else {
                                // التحقق من تكرار الباركود
                                val existingProduct = db.productDao().getByBarcode(barcode)
                                if (existingProduct != null) {
                                    launch(Dispatchers.Main) {
                                        Toast.makeText(requireContext(), getString(R.string.barcode_exists), Toast.LENGTH_SHORT).show()
                                    }
                                    return@launch
                                }
                                db.productDao().insert(Product(
                                    barcode = barcode, name = name, price = price,
                                    quantity = qty, supplier = supplier.ifBlank { null }
                                ))
                            }
                            launch(Dispatchers.Main) {
                                Toast.makeText(requireContext(),
                                    if (isEdit) getString(R.string.product_updated) else getString(R.string.product_added),
                                    Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                        } catch (e: Exception) {
                            launch(Dispatchers.Main) {
                                Toast.makeText(requireContext(), "خطأ: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
    }

    /**
     * تأكيد حذف منتج
     */
    private fun confirmDelete(product: Product) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirm_delete))
            .setMessage("هل تريد حذف '${product.name}'؟")
            .setPositiveButton(getString(R.string.btn_delete)) { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    db.productDao().deleteById(product.id)
                    launch(Dispatchers.Main) {
                        Toast.makeText(requireContext(), getString(R.string.product_deleted), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
