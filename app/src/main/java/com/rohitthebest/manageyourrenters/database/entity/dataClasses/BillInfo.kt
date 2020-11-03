package com.rohitthebest.manageyourrenters.database.entity.dataClasses

data class BillInfo(
    var billPeriodType : String,
    var billDateFrom : Long?,
    var billDateTill : Long?,
    var billMonth : String?,
    var billYear : String?,
    var currencySymbol : String?
) {

    constructor() : this(
        "",
        0L,
        0L,
        "",
        "",
        ""
    )
}