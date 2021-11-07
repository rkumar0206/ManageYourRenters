package com.rohitthebest.manageyourrenters.database.model.apiModels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_category_table")
data class ExpenseCategory(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    var categoryDescription: String?,
    var categoryName: String,
    var created: Long,
    var imageUrl: String? = null,
    var modified: Long,
    var uid: String,
    var key: String,
    var isSynced: Boolean = false
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