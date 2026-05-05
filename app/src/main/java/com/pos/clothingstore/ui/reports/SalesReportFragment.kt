package com.pos.clothingstore.ui.reports

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pos.clothingstore.POSApplication
import com.pos.clothingstore.R
import com.pos.clothingstore.databinding.FragmentSalesReportBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * شاشة تقرير المبيعات اليومية
 * تعرض إجمالي مبيعات اليوم وآخر الفواتير
 */
class SalesReportFragment : Fragment() {

    private var _binding: FragmentSalesReportBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: SalesAdapter
    private val db by lazy { POSApplication.getInstance().database }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSalesReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SalesAdapter()
        binding.rvSales.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSales.adapter = adapter

        loadTodaySales()
    }

    /**
     * تحميل مبيعات اليوم من قاعدة البيانات
     */
    private fun loadTodaySales() {
        lifecycleScope.launch(Dispatchers.IO) {
            // حساب بداية ونهاية اليوم
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.timeInMillis

            val sales = db.saleDao().getSalesForDay(startOfDay, endOfDay)
            val total = db.saleDao().getDailyTotal(startOfDay, endOfDay)

            launch(Dispatchers.Main) {
                adapter.submitList(sales)
                binding.tvTodayTotal.text = String.format("%.2f %s", total, "ر.س")
                binding.tvTodayCount.text = String.format("%d عملية بيع", sales.size)

                if (sales.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
