package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_table")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    var amount: Double,
    var created: Long,
    var modified: Long,
    var spentOn: String,
    var uid: String,
    var key: String,
    var categoryKey: String = "",
    var isSynced: Boolean = true
) {

    constructor() : this(
        null,
        0.0,
        0L,
        0L,
        "",
        "",
        "",
        "",
        true
    )
}