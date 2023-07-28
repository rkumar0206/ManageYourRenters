package com.rohitthebest.manageyourrenters.data

data class ExpenseCategoryAndTheirTotalExpenseAmounts(
    var expenseCategoryKey: String,
    var categoryName: String,
    var totalAmount: Double
)