package com.rohitthebest.manageyourrenters.data

data class RenterPaymentExtras(
    var fieldName: String,
    var fieldAmount: Double
) {
    constructor() : this(
        "",
        0.0
    )
}
