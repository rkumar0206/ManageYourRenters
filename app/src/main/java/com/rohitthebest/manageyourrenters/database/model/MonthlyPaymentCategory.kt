package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_payment_category_table")
data class MonthlyPaymentCategory(
    @PrimaryKey(autoGenerate = false) var key: String,
    var categoryDescription: String = "",
    var categoryName: String,
    var created: Long,
    var id: Int,
    var imageUrl: String = "",
    var modified: Long,
    var uid: String,
    var isSynced: Boolean = true
) {

    constructor() : this(
        "",
        "",
        "",
        0,
        0,
        "",
        0L,
        "",
        true
    )
}