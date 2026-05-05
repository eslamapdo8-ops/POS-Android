package com.pos.clothingstore.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.pos.clothingstore.data.local.entity.User

/**
 * عمليات CRUD للمستخدمين
 */
@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): User?

    @Query("SELECT * FROM users ORDER BY email ASC")
    fun getAllUsers(): LiveData<List<User>>

    @Query("SELECT * FROM users WHERE role = 'admin' LIMIT 1")
    suspend fun getAdmin(): User?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getCount(): Int

    @Delete
    suspend fun delete(user: User)
}
