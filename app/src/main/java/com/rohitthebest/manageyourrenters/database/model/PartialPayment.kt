package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties


/**
 * This model will be used for adding payments in chunks.
 * It will be used by the borrower payment table
 */

@IgnoreExtraProperties
@Entity(tableName = "partial_payment_table")
data class PartialPayment(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    var created: Long = System.currentTimeMillis(),
    var borrowerId: String,
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
        "",
        0.0,
        "",
        "",
        false
    )
}