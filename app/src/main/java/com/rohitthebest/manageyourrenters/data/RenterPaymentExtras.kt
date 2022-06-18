package com.rohitthebest.manageyourrenters.data

import java.io.Serializable

data class RenterPaymentExtras(
    var fieldName: String,
    var fieldAmount: Double
) : Serializable {
    constructor() : this(
        "",
        0.0
    )
}
