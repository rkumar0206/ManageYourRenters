package com.rohitthebest.manageyourrenters.database.model.apiModels

data class MonthlyPaymentCategory(
    val categoryDescription: String,
    val categoryName: String,
    val created: Long,
    val id: Int,
    val imageUrl: Any,
    val key: String,
    val modified: Long,
    val uid: String
)