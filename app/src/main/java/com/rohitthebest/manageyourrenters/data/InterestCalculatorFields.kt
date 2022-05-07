package com.rohitthebest.manageyourrenters.data

data class InterestCalculatorFields(
    var startDate: Long,
    var principalAmount: Double,
    var interest: Interest
) {
    constructor() : this(
        0L,
        0.0,
        Interest(InterestType.SIMPLE_INTEREST, 0.0, InterestTimeSchedule.ANNUALLY)
    )
}
