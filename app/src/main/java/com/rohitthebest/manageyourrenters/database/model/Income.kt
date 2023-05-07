package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

@Entity(tableName = "income_table")
@IgnoreExtraProperties
data class Income(
    @PrimaryKey(autoGenerate = false) var key: String,
    var created: Long,
    var modified: Long,
    var source: String,
    var income: Double,
    var month: Int,
    var year: Int,
    var monthYearString: String,
    var isSynced: Boolean,
    var uid: String
) {
    constructor() : this(
        "",
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        "",
        0.0,
        0,
        0,
        "",
        false,
        ""
    )

    fun generateMonthYearString(): String {
        return "${this.month}_${this.year}"
    }
}
