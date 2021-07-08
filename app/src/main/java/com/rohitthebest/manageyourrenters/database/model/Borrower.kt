package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
@Entity(tableName = "borrower_table")
data class Borrower(
    @PrimaryKey(autoGenerate = true) var id: Int?,
    var created: Long = System.currentTimeMillis(),
    var modified: Long = System.currentTimeMillis(),
    var borrowerId: String,  // it is the id which will be used by the borrower to access his account
    var borrowerPassword: String, // it is the password which will act as a key to the borrower id
    var key: String,  // it will be the firestore document id
    var name: String,
    var mobileNumber: String,
    var emailId: String?,
    var otherDocumentName: String?,
    var otherDocumentNumber: String?,
    var isSynced: Boolean = false,
    var totalDueAmount: Double = 0.0,
    var uid: String
) {

    constructor() : this(
        null,
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        false,
        0.0,
        ""
    )
}
