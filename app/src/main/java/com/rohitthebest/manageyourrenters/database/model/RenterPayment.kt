package com.rohitthebest.manageyourrenters.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rohitthebest.manageyourrenters.data.RenterBillPeriodInfo
import com.rohitthebest.manageyourrenters.data.RenterElectricityBillInfo
import com.rohitthebest.manageyourrenters.data.RenterPaymentExtras
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import java.io.Serializable

@Entity(tableName = "renter_payment_table")
data class RenterPayment(
    @PrimaryKey(autoGenerate = false) var key: String = "",
    var created: Long = 0L,
    var modified: Long = 0L,
    var renterKey: String = "",
    var currencySymbol: String = "",
    var billPeriodInfo: RenterBillPeriodInfo = RenterBillPeriodInfo(),
    var isElectricityBillIncluded: Boolean = false,
    var electricityBillInfo: RenterElectricityBillInfo? = null,
    var houseRent: Double = 0.0,
    var parkingRent: Double = 0.0,
    var extras: RenterPaymentExtras? = null,
    var netDemand: Double = 0.0,
    var amountPaid: Double = 0.0,
    var note: String = "",
    var uid: String = "",
    var isSupportingDocAdded: Boolean = false,
    var supportingDocument: SupportingDocument? = null,
    var isSynced: Boolean = false
) : Serializable {

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
        false,
        null,
        false
    )
}