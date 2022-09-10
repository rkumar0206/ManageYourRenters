package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.rohitthebest.manageyourrenters.data.StatusEnum
import com.rohitthebest.manageyourrenters.data.SupportingDocument

@Entity(tableName = "renter_table")
@IgnoreExtraProperties
data class Renter(
    var timeStamp: Long? = System.currentTimeMillis(),
    var modified: Long = System.currentTimeMillis(),
    var name: String,
    var mobileNumber: String,
    var emailId: String?,
    var otherDocumentName: String?,
    var otherDocumentNumber: String?,
    var roomNumber: String,
    var address: String,
    var dueOrAdvanceAmount: Double = 0.0,
    var uid: String,
    var renterId: String,
    var renterPassword: String,
    var key: String?,
    var isSupportingDocAdded: Boolean = false,
    var supportingDocument: SupportingDocument? = null,
    var isSynced: String = "false",
    var status: StatusEnum = StatusEnum.ACTIVE
) {

    @Exclude
    @PrimaryKey(autoGenerate = true)
    var id : Int? = null

    constructor() : this(
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        0.0,
        "",
        "",
        "",
        "",
        false,
        null,
        "false",
        StatusEnum.ACTIVE
    )

}