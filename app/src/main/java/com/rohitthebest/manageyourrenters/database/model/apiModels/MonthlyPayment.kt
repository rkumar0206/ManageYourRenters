package com.rohitthebest.manageyourrenters.database.model.apiModels

data class MonthlyPayment(
    val amount: Double,
    val categoryKey: String,
    val created: Long,
    val id: Int,
    val key: String,
    val message: Any,
    val modified: Long,
    val uid: String
)