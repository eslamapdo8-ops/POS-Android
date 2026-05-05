package com.pos.clothingstore.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * كيان بند الفاتورة - تفاصيل عناصر البيع
 */
@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("saleId")]
)
data class SaleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /** رقم الفاتورة المرتبطة */
    val saleId: Int,

    /** باركود المنتج */
    val productBarcode: String,

    /** الكمية المباعة */
    val quantity: Int,

    /** سعر الوحدة وقت البيع */
    val unitPrice: Double
)
