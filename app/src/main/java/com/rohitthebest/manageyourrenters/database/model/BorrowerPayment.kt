package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.rohitthebest.manageyourrenters.data.Interest
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import java.io.Serializable

@IgnoreExtraProperties
@Entity(tableName = "borrower_payment_table")
data class BorrowerPayment(
    @Exclude @PrimaryKey(autoGenerate = true) val id: Int?,
    var created: Long = System.currentTimeMillis(),
    var modified: Long = System.currentTimeMillis(),
    var borrowerId: String,
    var borrowerKey: String,
    var currencySymbol: String = "₹",
    var amountTakenOnRent: Double = 0.0,
    var dueLeftAmount: Double = 0.0,   // will be updated if the borrower does the partial payments
    var isDueCleared: Boolean = false,
    var isSupportingDocAdded: Boolean = false,
    var supportingDocument: SupportingDocument? = null,
    var isInterestAdded: Boolean = false,
    var interest: Interest? = null,
    var key: String,
    var uid: String,
    var isSynced: Boolean = false,
    var messageOrNote: String = ""
) : Serializable {

    @Exclude
    @Ignore
    var totalAmountPaid = 0.0
    @Exclude
    @Ignore
    var totalInterestTillNow = 0.0

    constructor() : this(
        null,
        0L,
        0L,
        "",
        "",
        "₹",
        0.0,
        0.0,
        false,
        false,
        null,
        false,
        null,
        "",
        "",
        false,
        ""
    )
}
