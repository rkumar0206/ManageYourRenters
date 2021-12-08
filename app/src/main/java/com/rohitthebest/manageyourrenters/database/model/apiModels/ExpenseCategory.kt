package com.rohitthebest.manageyourrenters.database.model.apiModels

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Model for API ManageYourRenters - ExpenseCategory
 */
@Entity(tableName = "expense_category_table")
data class ExpenseCategory(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    var categoryDescription: String?,
    var categoryName: String,
    var imageUrl: String? = null,
    var created: Long,
    var modified: Long,
    var uid: String,
    var key: String,
    var isSynced: Boolean = true,  // will be used only in this app
    var isSelected: Boolean = false  // will be used only in this app
) {

    constructor() : this(
        null,
        "",
        "",
        "",
        0L,
        0L,
        "",
        "",
        true
    )
}