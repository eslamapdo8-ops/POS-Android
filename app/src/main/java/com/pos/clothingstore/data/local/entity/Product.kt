package com.pos.clothingstore.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * كيان المنتج - الجدول الرئيسي في المخزون
 */
@Entity(
    tableName = "products",
    indices = [Index(value = ["barcode"], unique = true)]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** باركود المنتج (فريد) */
    val barcode: String,

    /** اسم المنتج */
    val name: String,

    /** سعر البيع */
    val price: Double,

    /** الكمية المتوفرة */
    val quantity: Int,

    /** اسم المورد (اختياري) */
    val supplier: String? = null
)
