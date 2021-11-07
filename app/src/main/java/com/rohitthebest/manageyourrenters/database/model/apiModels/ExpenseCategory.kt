package com.rohitthebest.manageyourrenters.database.model.apiModels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_category_table")
data class ExpenseCategory(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    val categoryDescription: String?,
    val categoryName: String,
    val created: Long,
    val imageUrl: String? = null,
    val modified: Long,
    val uid: String,
    val key: String,
    val isSynced: Boolean = false
) {

    constructor() : this(
        null,
        "",
        "",
        0,
        "",
        0L,
        "",
        "",
        false
    )
}