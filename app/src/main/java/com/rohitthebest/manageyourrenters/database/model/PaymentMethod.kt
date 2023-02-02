package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@Entity(tableName = "payment_method_table")
@IgnoreExtraProperties
data class PaymentMethod(
    @PrimaryKey(autoGenerate = false) var key: String,
    var paymentMethod: String,
    var uid: String,
    var isSynced: Boolean,
    @Exclude @Ignore var isSelected: Boolean = false,
) {

    constructor() : this(
        "",
        "",
        "",
        true,
        false
    )
}
