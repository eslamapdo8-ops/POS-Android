package com.pos.clothingstore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * كيان الفاتورة - رأس عملية البيع
 */
@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** تاريخ البيع (بالملي ثانية) */
    val date: Long = System.currentTimeMillis(),

    /** إجمالي المبلغ */
    val total: Double,

    /** طريقة الدفع: "cash" أو "card" */
    val paymentMethod: String = "cash"
)
