package com.rohitthebest.manageyourrenters.database.entity.dataClasses

data class ElectricityBillInfo(
    var isTakingElectricBill: String = "false",
    var previousReading: Double?,
    var currentReading: Double?,
    var rate: Double?,
    var differenceInReading: Double?,
    var totalElectricBill: String?
) {

    constructor() : this(
        "false",
        0.0,
        0.0,
        0.0,
        0.0,
        ""
    )
}