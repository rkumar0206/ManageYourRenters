package com.rohitthebest.manageyourrenters.data

enum class InterestType {

    SIMPLE_INTEREST,
    COMPOUND_INTEREST
}

enum class InterestTimeSchedule {

    DAILY,
    MONTHLY,
    ANNUALLY
}

data class Interest(
    var type: InterestType,
    var ratePercent: Double = 0.0,
    var timeSchedule: InterestTimeSchedule
) {

    constructor() : this(
        InterestType.SIMPLE_INTEREST,
        0.0,
        InterestTimeSchedule.ANNUALLY
    )
}
