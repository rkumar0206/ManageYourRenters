package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import com.rohitthebest.manageyourrenters.data.SupportingDocument

@IgnoreExtraProperties
@Entity(tableName = "emi_table")
data class EMI(
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    var created: Long = 0L,
    var modified: Long = 0L,
    var key: String = "",
    var emiName: String = "",
    var startDate: Long = 0L,
    var totalMonths: Int = 0,
    var monthsCompleted: Int = 0,
    var currencySymbol: String = "₹",
    var amountPaidPerMonth: Double = 0.0,
    var amountPaid: Double = 0.0,
    var isSupportingDocAdded: Boolean = false,
    var supportingDocument: SupportingDocument? = null,
    var isSynced: Boolean = false,
    var uid: String = ""
) {

    constructor() : this(
        null,
        0L,
        0L,
        "",
        "",
        0L,
        0,
        0,
        "₹",
        0.0,
        0.0,
        false,
        null,
        false,
        ""
    )
}
