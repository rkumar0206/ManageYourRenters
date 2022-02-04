package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rohitthebest.manageyourrenters.data.RenterBillPeriodInfo
import com.rohitthebest.manageyourrenters.data.RenterElectricityBillInfo
import com.rohitthebest.manageyourrenters.data.RenterPaymentExtras

@Entity(tableName = "renter_payment_table")
data class RenterPayment(
    @PrimaryKey(autoGenerate = false) var key: String,
    var created: Long,
    var modified: Long,
    var renterKey: String,
    var currencySymbol: String,
    var billPeriodInfo: RenterBillPeriodInfo,
    var isElectricityBillIncluded: Boolean = false,
    var electricityBillInfo: RenterElectricityBillInfo?,
    var houseRent: Double,
    var parkingRent: Double,
    var extras: RenterPaymentExtras?,
    var netDemand: Double,
    var amountPaid: Double,
    var note: String = "",
    var uid: String,
    var isSynced: Boolean
) {

    constructor() : this(
        "",
        0L,
        0L,
        "",
        "₹",
        RenterBillPeriodInfo(),
        false,
        null,
        0.0,
        0.0,
        null,
        0.0,
        0.0,
        "",
        "",
        false
    )
}