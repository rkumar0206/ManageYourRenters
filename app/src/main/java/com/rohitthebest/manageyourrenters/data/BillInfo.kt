package com.rohitthebest.manageyourrenters.data

data class BillInfo(
    var billPeriodType : String,
    var billDateFrom : Long?,
    var billDateTill : Long?,
    var numberOfDays : String? = "",
    var billMonth : String?,
    var billMonthNumber : Int?,
    var billYear : Int?,
    var currencySymbol : String?
) {

    constructor() : this(
        "",
        0L,
        0L,
        "",
        "",
        0,
        0,
        ""
    )
}