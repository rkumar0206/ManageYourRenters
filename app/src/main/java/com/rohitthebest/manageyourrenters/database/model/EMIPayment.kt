package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.rohitthebest.manageyourrenters.data.SupportingDocument

@IgnoreExtraProperties
@Entity(tableName = "emi_payment_table")
data class EMIPayment(
    @Exclude @PrimaryKey(autoGenerate = true) var id: Int? = null,
    var created: Long = 0L,
    var modified: Long = 0L,
    var key: String = "",
    var emiKey: String = "",
    var amountPaid: Double = 0.0,
    var fromMonth: Int = 0,
    var tillMonth: Int = 0,
    var isSupportingDocAdded: Boolean = false,
    var supportingDocument: SupportingDocument? = null,
    var isSynced: Boolean = false,
    var uid: String = "",
    var message: String = ""
) {
    constructor() : this(
        null,
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        "",
        "",
        0.0,
        0,
        0,
        false,
        null,
        false,
        "",
        ""
    )
}
