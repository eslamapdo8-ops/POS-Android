package com.pos.clothingstore.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.pos.clothingstore.data.local.entity.Product

/**
 * عمليات CRUD للمنتجات
 */
@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): LiveData<List<Product>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllProductsOnce(): List<Product>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): Product?

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Product?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): LiveData<List<Product>>

    @Query("UPDATE products SET quantity = quantity - :qty WHERE barcode = :barcode AND quantity >= :qty")
    suspend fun decreaseQuantity(barcode: String, qty: Int): Int

    @Query("UPDATE products SET quantity = quantity + :qty WHERE barcode = :barcode")
    suspend fun increaseQuantity(barcode: String, qty: Int)

    @Query("UPDATE products SET quantity = :newQty WHERE barcode = :barcode")
    suspend fun setQuantity(barcode: String, newQty: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getCount(): Int
}
