package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "borrower_table")
data class BorrowerPayment(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    var created: Long = System.currentTimeMillis(),
    var modified: Long = System.currentTimeMillis(),
    var borrowerId: String,
    var borrowerKey: String,
    var amountTakenOnRent: Double = 0.0,
    var key: String,
    var uid: String,
    var isSynced: Boolean = false,
    var messageOrNote: String = ""
) {
    constructor() : this(
        null,
        0L,
        0L,
        "",
        "",
        0.0,
        "",
        "",
        false,
        ""
    )
}
