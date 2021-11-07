package com.rohitthebest.manageyourrenters.data.apiModels

data class ExpenseCategory(
    val categoryDescription: String?,
    val categoryName: String,
    val created: Long,
    val id: Long,
    val imageUrl: String? = null,
    val modified: Long,
    val uid: String,
    val key: String
) {

    constructor() : this(
        "",
        "",
        0L,
        0,
        "",
        0L,
        "",
        ""
    )
}