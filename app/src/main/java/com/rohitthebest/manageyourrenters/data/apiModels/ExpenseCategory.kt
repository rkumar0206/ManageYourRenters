package com.rohitthebest.manageyourrenters.data.apiModels

data class ExpenseCategory(
    val categoryDescription: String,
    val categoryName: String,
    val created: Long,
    val id: Int,
    val imageUrl: Any,
    val modified: Long,
    val uid: String
)