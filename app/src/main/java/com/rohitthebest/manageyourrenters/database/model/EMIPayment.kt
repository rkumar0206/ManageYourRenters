package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import com.rohitthebest.manageyourrenters.data.SupportingDocument

@IgnoreExtraProperties
@Entity(tableName = "emi_payment_table")
data class EMIPayment(
    var created: Long,
    var modified: Long,
    var key: String,
    var emiKey: String,
    var amountPaid: Double,
    var fromMonth: Int,
    var tillMonth: Int,
    var isSupportingDocumentAdded: Boolean = false,
    var supportingDocument: SupportingDocument?,
    var isSynced: Boolean,
    var uid: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    constructor() : this(
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
        ""
    )
}
