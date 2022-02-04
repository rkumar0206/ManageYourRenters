package com.rohitthebest.manageyourrenters.data

data class RenterElectricityBillInfo(
    var previousReading: Double,
    var currentReading: Double,
    var rate: Double,
    var differenceInReading: Double,
    var totalElectricBill: Double
) {

    constructor() : this(
        0.0,
        0.0,
        0.0,
        0.0,
        0.0
    )
}