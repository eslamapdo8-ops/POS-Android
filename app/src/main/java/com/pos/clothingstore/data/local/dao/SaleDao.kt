package com.pos.clothingstore.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.pos.clothingstore.data.local.entity.Sale
import com.pos.clothingstore.data.local.entity.SaleItem

/**
 * عمليات CRUD لفواتير البيع
 */
@Dao
interface SaleDao {

    @Insert
    suspend fun insertSale(sale: Sale): Long

    @Insert
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Transaction
    suspend fun insertSaleWithItems(sale: Sale, items: List<SaleItem>): Long {
        val saleId = insertSale(sale)
        val itemsWithSaleId = items.map { it.copy(saleId = saleId.toInt()) }
        insertSaleItems(itemsWithSaleId)
        return saleId
    }

    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSales(): LiveData<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Int): Sale?

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItems(saleId: Int): List<SaleItem>

    @Query("SELECT * FROM sales WHERE date >= :startOfDay AND date < :endOfDay ORDER BY date DESC")
    suspend fun getSalesForDay(startOfDay: Long, endOfDay: Long): List<Sale>

    @Query("SELECT COALESCE(SUM(total), 0) FROM sales WHERE date >= :startOfDay AND date < :endOfDay")
    suspend fun getDailyTotal(startOfDay: Long, endOfDay: Long): Double

    @Query("SELECT COUNT(*) FROM sales")
    suspend fun getSaleCount(): Int
}
