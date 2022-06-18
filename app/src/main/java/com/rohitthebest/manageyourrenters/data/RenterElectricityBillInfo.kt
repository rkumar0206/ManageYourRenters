package com.rohitthebest.manageyourrenters.data

import java.io.Serializable

data class RenterElectricityBillInfo(
    var previousReading: Double,
    var currentReading: Double,
    var rate: Double,
    var differenceInReading: Double,
    var totalElectricBill: Double
) : Serializable {

    constructor() : this(
        0.0,
        0.0,
        0.0,
        0.0,
        0.0
    )
}