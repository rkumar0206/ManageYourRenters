package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "borrower_partial_fragment")
data class BorrowerPartialPayment(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    var created: Long = System.currentTimeMillis(),
    val borrowerPaymentKey: String,
    val amount: Double,
    var key: String,
    var uid: String,
    var isSynced: Boolean = false,
) {

    constructor() : this(
        null,
        0L,
        "",
        0.0,
        "",
        "",
        false
    )
}