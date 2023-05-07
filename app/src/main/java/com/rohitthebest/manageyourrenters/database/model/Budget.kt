package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@Entity(tableName = "budget_table")
@IgnoreExtraProperties
data class Budget(
    @PrimaryKey(autoGenerate = false) var key: String,
    var created: Long,
    var modified: Long,
    var budgetLimit: Double,
    var month: Int,
    var year: Int,
    var categoryKey: String,
    var monthYearString: String,
    var isSynced: Boolean,
    var uid: String
) {

    @Exclude
    @Ignore
    var categoryName: String = ""

    @Exclude
    @Ignore
    var categoryImageUrl: String = ""

    constructor() : this(
        "",
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        0.0,
        0,
        0,
        "",
        "",
        false,
        ""
    )

    fun generateMonthYearString(): String {
        return "${this.month}_${this.year}"
    }
}
