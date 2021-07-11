package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties
import com.rohitthebest.manageyourrenters.data.Interest

@IgnoreExtraProperties
@Entity(tableName = "borrower_payment_table")
data class BorrowerPayment(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    var created: Long = System.currentTimeMillis(),
    var modified: Long = System.currentTimeMillis(),
    var borrowerId: String,
    var borrowerKey: String,
    var amountTakenOnRent: Double = 0.0,
    var dueLeftAmount: Double = 0.0,   // will be updated if the borrower does the partial payments
    var isDueCleared: Boolean = false,
    var isSupportingDocAdded: Boolean = false,
    var supportingDocumentUrl: String? = null,
    var supportingDocumentType: String? = null,  //select from pdf, image, and url
    var isInterestAdded: Boolean = false,
    var interest: Interest? = null,
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
        0.0,
        false,
        false,
        "",
        "",
        false,
        null,
        "",
        "",
        false,
        ""
    )
}
