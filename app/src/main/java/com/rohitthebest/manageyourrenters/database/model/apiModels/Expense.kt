package com.rohitthebest.manageyourrenters.database.model.apiModels

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_table")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    val amount: Double,
    val created: Long,
    val modified: Long,
    val spentOn: String,
    val uid: String,
    val key: String,
    val categoryKey: String = ""
) {

    constructor() : this(
        null,
        0.0,
        0L,
        0L,
        "",
        "",
        "",
        ""
    )
}