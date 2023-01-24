package com.rohitthebest.manageyourrenters.data

import java.io.Serializable

data class RenterBillPeriodInfo(
    var billPeriodType: BillPeriodType,
    var renterBillMonthType: RenterBillMonthType?,
    var renterBillDateType: RenterBillDateType?,
    var billYear: Int
) : Serializable {

    constructor() : this(
        BillPeriodType.BY_MONTH,
        null,
        null,
        0
    )
}

enum class BillPeriodType {

    BY_MONTH,
    BY_DATE
}

data class RenterBillMonthType(
    var forBillMonth: Int,
    var forBillYear: Int,
    var toBillMonth: Int,
    var toBillYear: Int,
    var numberOfMonths: Int
) : Serializable {
    constructor() : this(
        0,
        0,
        0,
        0,
        0
    )
}

data class RenterBillDateType(
    var fromBillDate: Long,
    var toBillDate: Long,
    var numberOfDays: Int
) : Serializable {
    constructor() : this(
        0L,
        0L,
        0
    )
}