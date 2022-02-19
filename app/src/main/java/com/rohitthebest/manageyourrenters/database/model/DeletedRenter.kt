package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.IgnoreExtraProperties

@Entity(tableName = "deleted_renter_table")
@IgnoreExtraProperties
data class DeletedRenter(
    @PrimaryKey(autoGenerate = false) val key: String,
    var created: Long,
    var renterInfo: Renter,
    var lastPaymentInfo: RenterPayment
) {

    constructor() : this(
        "",
        System.currentTimeMillis(),
        Renter(),
        RenterPayment()
    )
}
