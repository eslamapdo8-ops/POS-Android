package com.pos.clothingstore.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pos.clothingstore.data.local.dao.ProductDao
import com.pos.clothingstore.data.local.dao.SaleDao
import com.pos.clothingstore.data.local.dao.UserDao
import com.pos.clothingstore.data.local.entity.Product
import com.pos.clothingstore.data.local.entity.Sale
import com.pos.clothingstore.data.local.entity.SaleItem
import com.pos.clothingstore.data.local.entity.User

/**
 * قاعدة البيانات المحلية - المصدر الوحيد للحقيقة
 */
@Database(
    entities = [Product::class, Sale::class, SaleItem::class, User::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "pos_database"
            )
                .addCallback(PrepopulateCallback())
                .fallbackToDestructiveMigration()
                .build()
        }
    }

    /**
     * ملء البيانات الأولية عند أول تشغيل
     */
    private class PrepopulateCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // سيتم الملء من Application class
        }
    }
}
