package com.rohitthebest.manageyourrenters.data.filter

import java.io.Serializable

data class BudgetAndIncomeExpenseFilter(
    var categoryKeys: List<String>,
    var paymentMethods: List<String> = emptyList()  // if empty then show all the expenses
) : Serializable {

    constructor() : this(
        emptyList(),
        emptyList()
    )
}
