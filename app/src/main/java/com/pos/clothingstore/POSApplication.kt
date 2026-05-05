package com.pos.clothingstore

import android.app.Application
import com.pos.clothingstore.data.local.AppDatabase
import com.pos.clothingstore.data.local.entity.Product
import com.pos.clothingstore.data.local.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * فئة التطبيق الرئيسية
 */
class POSApplication : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        prefillSampleData()
    }

    /**
     * ملء بيانات تجريبية عند أول تشغيل
     */
    private fun prefillSampleData() {
        CoroutineScope(Dispatchers.IO).launch {
            // إنشاء مدير افتراضي إذا لم يوجد مستخدمين
            if (database.userDao().getCount() == 0) {
                database.userDao().insert(
                    User(
                        email = "admin@store.com",
                        role = "admin",
                        displayName = "مدير المتجر"
                    )
                )
            }
            // إضافة منتجات تجريبية إذا كانت قاعدة البيانات فارغة
            if (database.productDao().getCount() == 0) {
                val sampleProducts = listOf(
                    Product(barcode = "6281001000011", name = "قميص أبيض قطني", price = 120.0, quantity = 50, supplier = "مصنع الأنوار"),
                    Product(barcode = "6281001000028", name = "بنطلون جينز رجالي", price = 250.0, quantity = 30, supplier = "مصنع الأنوار"),
                    Product(barcode = "6281001000035", name = "تيشيرت أسود قطني", price = 85.0, quantity = 100, supplier = "شركة القطن"),
                    Product(barcode = "6281001000042", name = "جاكبة شتوية", price = 450.0, quantity = 20, supplier = "الأزياء الحديثة"),
                    Product(barcode = "6281001000059", name = "حذاء رياضي", price = 320.0, quantity = 40, supplier = "الرياض للأحذية"),
                    Product(barcode = "6281001000066", name = "غترة بيضاء", price = 35.0, quantity = 200, supplier = "الأزياء الحديثة")
                )
                sampleProducts.forEach { database.productDao().insert(it) }
            }
        }
    }

    companion object {
        @Volatile
        private lateinit var instance: POSApplication

        fun getInstance(): POSApplication = instance
    }
}
