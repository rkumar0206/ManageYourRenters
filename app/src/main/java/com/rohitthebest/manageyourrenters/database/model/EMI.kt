package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import com.rohitthebest.manageyourrenters.data.SupportingDocument

@IgnoreExtraProperties
@Entity(tableName = "emi_table")
data class EMI(
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    var created: Long,
    var modified: Long,
    var key: String,
    var emiName: String,
    var startDate: Long,
    var totalMonths: Int,
    var monthsCompleted: Int,
    var amountPaidPerMonth: Double,
    var amountPaid: Double,
    var isSupportingDocumentAdded: Boolean,
    var supportingDocument: SupportingDocument,
    var isSynced: Boolean,
    var uid: String
) {

    constructor() : this(
        null,
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        "",
        "",
        System.currentTimeMillis(),
        0,
        0,
        0.0,
        0.0,
        false,
        SupportingDocument(),
        false,
        ""
    )
}
