package com.rohitthebest.manageyourrenters.data

data class RenterBillPeriodInfo(
    var billPeriodType: BillPeriodType,
    var renterBillMonthType: RenterBillMonthType?,
    var renterBillDateType: RenterBillDateType?,
    var billYear: Int
) {

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
    var toBillMonth: Int,
    var numberOfMonths: Int
) {
    constructor() : this(
        0,
        0,
        0
    )
}

data class RenterBillDateType(
    var fromBillDate: Long,
    var toBillDate: Long,
    var numberOfDays: Int
) {
    constructor() : this(
        0L,
        0L,
        0
    )
}