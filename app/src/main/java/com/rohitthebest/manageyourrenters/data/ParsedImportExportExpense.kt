package com.rohitthebest.manageyourrenters.data

data class ParsedImportExportExpense(
    val date: String?,
    val amount: Double,
    val category: String,
    val spentOn: String?,
    val paymentMethod: String?
)
