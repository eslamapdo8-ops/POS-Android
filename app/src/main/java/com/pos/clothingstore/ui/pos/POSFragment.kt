package com.pos.clothingstore.ui.pos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pos.clothingstore.POSApplication
import com.pos.clothingstore.data.local.entity.Sale
import com.pos.clothingstore.data.local.entity.SaleItem
import com.pos.clothingstore.databinding.FragmentPosBinding
import com.pos.clothingstore.ui.scanner.BarcodeScannerActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * شاشة البيع الرئيسية (POS)
 * تعرض المنتجات في شبكة، سلة المشتريات، وأزرار الدفع
 */
class POSFragment : Fragment() {

    private var _binding: FragmentPosBinding? = null
    private val binding get() = _binding!!

    private lateinit var productsAdapter: ProductsAdapter
    private lateinit var cartAdapter: CartAdapter

    private val db by lazy { POSApplication.getInstance().database }

    /** نتيجة مسح الباركود */
    private val scanLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val barcode = result.data?.getStringExtra(BarcodeScannerActivity.EXTRA_SCANNED_BARCODE)
            if (!barcode.isNullOrEmpty()) {
                addProductToCartByBarcode(barcode)
            }
        }
    }

    /** طلب إذن الكاميرا */
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startScanner() else {
            Toast.makeText(requireContext(), "إذن الكاميرا مطلوب", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupProductGrid()
        setupCart()
        setupSearch()
        setupPaymentButtons()
        observeProducts()
    }

    /**
     * إعداد شبكة المنتجات
     */
    private fun setupProductGrid() {
        productsAdapter = ProductsAdapter { product ->
            if (product.quantity > 0) {
                cartAdapter.addItem(product)
            } else {
                Toast.makeText(requireContext(), "المنتج غير متوفر (الكمية صفر)", Toast.LENGTH_SHORT).show()
            }
        }
        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvProducts.adapter = productsAdapter
    }

    /**
     * إعداد سلة المشتريات
     */
    private fun setupCart() {
        cartAdapter = CartAdapter(
            onQuantityChanged = { updateCartUI() },
            onItemRemoved = { position -> cartAdapter.removeItem(position) }
        )
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = cartAdapter
    }

    /**
     * إعداد البحث
     */
    private fun setupSearch() {
        val searchInput = binding.etSearch
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString() ?: ""
                if (query.length >= 2) {
                    searchProducts(query)
                } else if (query.isEmpty) {
                    observeProducts()
                }
            }
        })

        binding.btnScan.setOnClickListener { openScanner() }
    }

    /**
     * إعداد أزرار الدفع (كاش وبطاقة)
     */
    private fun setupPaymentButtons() {
        binding.btnCash.setOnClickListener { completeSale("cash") }
        binding.btnCard.setOnClickListener { completeSale("card") }
    }

    /**
     * مراقبة المنتجات من قاعدة البيانات
     */
    private fun observeProducts() {
        db.productDao().getAllProducts().observe(viewLifecycleOwner) { products ->
            productsAdapter.submitList(products)
            updateCartUI()
        }
    }

    /**
     * البحث عن منتجات
     */
    private fun searchProducts(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val results = db.productDao().getAllProductsOnce().filter {
                it.name.contains(query, ignoreCase = true) || it.barcode.contains(query, ignoreCase = true)
            }
            launch(Dispatchers.Main) {
                productsAdapter.submitList(results)
            }
        }
    }

    /**
     * فتح شاشة المسح
     */
    private fun openScanner() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startScanner()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startScanner() {
        val intent = Intent(requireContext(), BarcodeScannerActivity::class.java)
        scanLauncher.launch(intent)
    }

    /**
     * إضافة منتج للسلة عن طريق الباركود
     */
    private fun addProductToCartByBarcode(barcode: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val product = db.productDao().getByBarcode(barcode)
            launch(Dispatchers.Main) {
                if (product != null) {
                    if (product.quantity > 0) {
                        cartAdapter.addItem(product)
                    } else {
                        Toast.makeText(requireContext(), "المنتج غير متوفر", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "منتج غير معروف: $barcode", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * تحديث واجهة السلة
     */
    private fun updateCartUI() {
        val total = cartAdapter.getTotal()
        val itemCount = cartAdapter.itemCount

        if (cartAdapter.isEmpty()) {
            binding.cartSection.visibility = View.GONE
        } else {
            binding.cartSection.visibility = View.VISIBLE
            binding.tvCartTotal.text = String.format("%.2f %s", total, "ر.س")
        }
    }

    /**
     * إتمام عملية البيع
     */
    private fun completeSale(paymentMethod: String) {
        if (cartAdapter.isEmpty()) {
            Toast.makeText(requireContext(), "السلة فارغة", Toast.LENGTH_SHORT).show()
            return
        }

        val cartItems = cartAdapter.getItems()
        val total = cartAdapter.getTotal()

        // تأكيد البيع
        val methodName = if (paymentMethod == "cash") "نقدي" else "بطاقة"
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("تأكيد البيع")
            .setMessage("الإجمالي: ${String.format("%.2f", total)} ر.س\nطريقة الدفع: $methodName")
            .setPositiveButton("تأكيد") { _, _ ->
                performSale(cartItems, total, paymentMethod)
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    /**
     * تنفيذ عملية البيع في قاعدة البيانات
     */
    private fun performSale(cartItems: List<CartItem>, total: Double, paymentMethod: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. إنشاء الفاتورة
                val sale = Sale(total = total, paymentMethod = paymentMethod)
                val saleId = db.saleDao().insertSale(sale)

                // 2. إنشاء بنود الفاتورة
                val saleItems = cartItems.map { item ->
                    SaleItem(
                        saleId = saleId.toInt(),
                        productBarcode = item.product.barcode,
                        quantity = item.quantity,
                        unitPrice = item.product.price
                    )
                }
                db.saleDao().insertSaleItems(saleItems)

                // 3. خصم الكميات من المخزون
                cartItems.forEach { item ->
                    db.productDao().decreaseQuantity(item.product.barcode, item.quantity)
                }

                launch(Dispatchers.Main) {
                    cartAdapter.clearCart()
                    updateCartUI()
                    Toast.makeText(requireContext(), "تمت عملية البيع بنجاح ✅", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "فشل البيع: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
