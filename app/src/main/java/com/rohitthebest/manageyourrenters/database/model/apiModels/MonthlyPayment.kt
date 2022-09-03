package com.rohitthebest.manageyourrenters.database.model.apiModels

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rohitthebest.manageyourrenters.data.BillPeriodType
import java.io.Serializable

@Entity(tableName = "monthly_payment_table")
data class MonthlyPayment(
    @PrimaryKey(autoGenerate = false) var key: String,
    var amount: Double,
    var categoryKey: String,
    var created: Long,
    var id: Int,
    var message: String = "",
    var modified: Long,
    var monthlyPaymentDateTimeInfo: MonthlyPaymentDateTimeInfo? = null,
    var uid: String,
    var isSynced: Boolean = true
) : Serializable {

    constructor() : this(
        "",
        0.0,
        "",
        0L,
        0,
        "",
        0L,
        null,
        "",
        true
    )
}

data class MonthlyPaymentDateTimeInfo(
    var id: Int,
    var paymentPeriodType: BillPeriodType,
    var forBillMonth: Int,
    var forBillYear: Int,
    var toBillMonth: Int,
    var toBillYear: Int,
    var numberOfMonths: Int,
    var fromBillDate: Long,
    var toBillDate: Long,
    var numberOfDays: Int

) : Serializable {

    constructor() : this(
        0,
        BillPeriodType.BY_MONTH,
        0,
        0,
        0,
        0,
        0,
        0L,
        0L,
        0
    )
}