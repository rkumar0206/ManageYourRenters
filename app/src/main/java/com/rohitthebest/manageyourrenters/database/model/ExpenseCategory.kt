package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude

@Entity(tableName = "expense_category_table")
data class ExpenseCategory(
    @Exclude @PrimaryKey(autoGenerate = true) val id: Long? = null,
    var categoryDescription: String?,
    var categoryName: String,
    var imageUrl: String? = null,
    var created: Long,
    var modified: Long,
    var uid: String,
    var key: String,
    var isSynced: Boolean = true,
    @Exclude var isSelected: Boolean = false  // will be used for selecting category in deep analyse expense
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
        true,
        false
    )
}