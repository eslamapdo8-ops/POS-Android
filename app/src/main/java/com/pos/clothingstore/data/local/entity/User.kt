package com.pos.clothingstore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * كيان المستخدم - دوران فقط: مدير وبائع
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val email: String,

    /** الدور: "admin" أو "salesperson" */
    val role: String = "salesperson",

    /** اسم العرض */
    val displayName: String = ""
)
